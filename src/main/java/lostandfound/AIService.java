package lostandfound;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

/**
 * AI service backed by a local Ollama instance.
 * <p>
 * Talks to Ollama's /api/generate endpoint.
 * Model and URL are configured via config.properties (AppConfig).
 * <p>
 * Vision models (llava:13b, llava, moondream, bakllava):
 * full image analysis + natural language search
 * Text-only models (llama3, mistral, gemma3):
 * natural language search only; image analysis returns a notice
 */
public class AIService {

    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();

    // ── Image Analysis ───────────────────────────────────────────────────────

    public static String analyzeImage(String base64Image) throws Exception {
        String base64 = stripDataPrefix(base64Image);

        if (!isVisionModel(AppConfig.ollamaModel)) {
            return "Image uploaded successfully. Automatic image analysis requires a vision model " +
                    "(e.g. llava:13b). Current model: " + AppConfig.ollamaModel +
                    ". Update ollama.model in config.properties to enable image analysis.";
        }

        String prompt = "You are helping a school Lost and Found department. " +
                "Analyze this image and provide a structured description including: " +
                "1. What the item is. 2. Color(s). 3. Any brand or visible text. " +
                "4. Condition. 5. Unique identifiers (names, stickers, damage). 6. Approximate size. " +
                "Be concise and factual. Plain text only.";

        String body = """
                {
                  "model": "%s",
                  "prompt": "%s",
                  "images": ["%s"],
                  "stream": false
                }
                """.formatted(AppConfig.ollamaModel, prompt, base64);

        String response = post(AppConfig.ollamaUrl + "/api/generate", body);
        String result = extractField(response, "response");
        checkForModelError(result, "image analysis");
        return result;
    }

    // ── Natural Language Search ──────────────────────────────────────────────

    public static SearchResult searchWithAI(String query, List<LostItem> allItems) throws Exception {
        if (allItems.isEmpty()) {
            return new SearchResult("No items in the database yet.", List.of());
        }

        String catalogue = buildCatalogue(allItems);

        String prompt = "You are an AI assistant for a school Lost and Found department.\\n\\n" +
                "A student or parent is searching for: \\\"" + escJson(query) + "\\\"\\n\\n" +
                "Inventory (one item per line):\\n" + escJson(catalogue) + "\\n\\n" +
                "Instructions:\\n" +
                "1. Find items that match the search query by color, type, description, marks, or AI description.\\n" +
                "2. Copy the ID values EXACTLY as they appear after 'ID:' in the inventory — same uppercase letters and numbers.\\n" +
                "3. Return ONLY valid JSON, no markdown, no explanation outside the JSON:\\n" +
                "{\\\"matches\\\": [\\\"ID1\\\", \\\"ID2\\\"], \\\"message\\\": \\\"friendly message explaining results\\\"}\\n" +
                "4. List best matches first. Include partial matches.\\n" +
                "5. If nothing matches, return empty matches array with an explanation in message.";

        String body = """
                {
                  "model": "%s",
                  "prompt": "%s",
                  "stream": false,
                  "format": "json"
                }
                """.formatted(AppConfig.ollamaModel, prompt);

        String response = post(AppConfig.ollamaUrl + "/api/generate", body);
        String raw = extractField(response, "response");
        checkForModelError(raw, "search");
        return parseSearchResult(raw, allItems);
    }

    // ── HTTP ─────────────────────────────────────────────────────────────────

    private static String post(String url, String body) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofMinutes(6))
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> res = HTTP.send(req, HttpResponse.BodyHandlers.ofString());

        if (res.statusCode() != 200) {
            System.err.println("Ollama error " + res.statusCode() + ": " + res.body());
            throw new RuntimeException(
                    "Ollama returned HTTP " + res.statusCode() + ". " +
                            "Is Ollama running at " + AppConfig.ollamaUrl + "? " +
                            "Have you run:  ollama pull " + AppConfig.ollamaModel + " ?");
        }
        return res.body();
    }

    // ── Parsing ──────────────────────────────────────────────────────────────

    private static String extractField(String json, String field) {
        String marker = "\"" + field + "\":\"";
        int start = json.indexOf(marker);
        if (start < 0) return "";
        start += marker.length();

        StringBuilder sb = new StringBuilder();
        int i = start;
        while (i < json.length()) {
            char c = json.charAt(i);
            if (c == '\\' && i + 1 < json.length()) {
                char next = json.charAt(i + 1);
                switch (next) {
                    case '"' -> sb.append('"');
                    case '\\' -> sb.append('\\');
                    case 'n' -> sb.append('\n');
                    case 'r' -> sb.append('\r');
                    case 't' -> sb.append('\t');
                    default -> sb.append(next);
                }
                i += 2;
            } else if (c == '"') {
                break;
            } else {
                sb.append(c);
                i++;
            }
        }
        return sb.toString();
    }

    private static SearchResult parseSearchResult(String aiResponse, List<LostItem> allItems) {
        aiResponse = aiResponse.replaceAll("ID:", "");
        System.out.println("🔍 Raw AI search response: " + aiResponse);

        int jsonStart = aiResponse.indexOf('{');
        int jsonEnd = aiResponse.lastIndexOf('}');
        if (jsonStart < 0 || jsonEnd < 0) {
            return new SearchResult(aiResponse.isBlank() ? "Search complete." : aiResponse, List.of());
        }
        String json = aiResponse.substring(jsonStart, jsonEnd + 1);

        // Extract message — use our own parser to handle escaped quotes inside the string
        String message = extractField(json, "message");
        if (message == null || message.isBlank()) message = "Search complete.";

        // Extract matches array - find ["ID1","ID2"] and pull out every quoted string inside
        List<String> ids = new java.util.ArrayList<>();
        int arrStart = json.indexOf("\"matches\"");
        if (arrStart >= 0) {
            int bracket = json.indexOf('[', arrStart);
            int bracketEnd = json.indexOf(']', bracket);
            if (bracket >= 0 && bracketEnd >= 0) {
                String arrContent = json.substring(bracket + 1, bracketEnd);
                // Pull every "..." token from inside the array
                int pos = 0;
                while (pos < arrContent.length()) {
                    int q1 = arrContent.indexOf('"', pos);
                    if (q1 < 0) break;
                    int q2 = arrContent.indexOf('"', q1 + 1);
                    if (q2 < 0) break;
                    String id = arrContent.substring(q1 + 1, q2).trim();
                    if (!id.isBlank()) ids.add(id);
                    pos = q2 + 1;
                }
            }
        }

        System.out.println("🔍 Parsed IDs from AI: " + ids);
        System.out.println("🔍 Available item IDs: " + allItems.stream().map(i -> i.id).collect(Collectors.toList()));

        // Match IDs — case-insensitive, ignore extra whitespace
        List<LostItem> matched = ids.stream()
                .map(id -> allItems.stream()
                        .filter(it -> id.trim().equalsIgnoreCase(it.id != null ? it.id.trim() : ""))
                        .findFirst().orElse(null))
                .filter(it -> it != null)
                .collect(Collectors.toList());

        System.out.println("🔍 Matched items: " + matched.stream().map(i -> i.id).collect(Collectors.toList()));

        // If the message says matches were found but our list is empty, something went wrong with
        // ID parsing — fall back to keyword search on the original items as a safety net
        if (matched.isEmpty() && !ids.isEmpty()) {
            System.out.println("⚠️  IDs returned by AI didn't match any items — check ID format. IDs from AI: " + ids);
        }

        return new SearchResult(message, matched);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    /**
     * Detects when the model itself returns an error JSON inside its response text.
     * This happens with restricted/gated models on some networks (HTTP 403 inside the response).
     */
    private static void checkForModelError(String modelResponse, String operation) {
        if (modelResponse == null || modelResponse.isBlank()) return;
        // Check for embedded error object like {"error":{"code":403,"message":"..."}}
        if (modelResponse.contains("\"error\"") && modelResponse.contains("\"code\"")) {
            String msg = extractField(modelResponse, "message");
            int codeStart = modelResponse.indexOf("\"code\":");
            String code = "";
            if (codeStart >= 0) {
                int from = codeStart + 7;
                while (from < modelResponse.length() && Character.isWhitespace(modelResponse.charAt(from))) from++;
                int to = from;
                while (to < modelResponse.length() && Character.isDigit(modelResponse.charAt(to))) to++;
                code = modelResponse.substring(from, to);
            }
            System.err.println("⚠️  Model returned an error during " + operation + ": " + modelResponse);
            String hint = code.equals("403")
                    ? "The model '" + AppConfig.ollamaModel + "' appears to be restricted on this network. " +
                    "Try a different model in config.properties (e.g. ollama.model=llava or ollama.model=moondream)."
                    : "The model returned an error. Check that '" + AppConfig.ollamaModel + "' is fully downloaded: ollama pull " + AppConfig.ollamaModel;
            throw new RuntimeException(hint + (msg != null ? " Detail: " + msg : ""));
        }
    }

    private static boolean isVisionModel(String model) {
        String m = model.toLowerCase();
        return m.startsWith("llava") || m.startsWith("moondream") ||
                m.startsWith("bakllava") || m.startsWith("minicpm") ||
                m.startsWith("llama3.2-vision") || m.startsWith("llama3.2:vision") ||
                m.startsWith("granite3") || m.contains("vision");
    }

    private static String buildCatalogue(List<LostItem> items) {
        StringBuilder sb = new StringBuilder();
        for (LostItem item : items) {
            sb.append("ID:").append(item.id)
                    .append(" | Category:").append(nvl(item.category))
                    .append(" | Color:").append(nvl(item.color))
                    .append(" | Description:").append(nvl(item.description))
                    .append(" | Marks:").append(nvl(item.identifyingMarks))
                    .append(" | Location:").append(nvl(item.locationFound))
                    .append(" | AI-Description:").append(nvl(item.aiDescription))
                    .append(" | Status:").append(nvl(item.status))
                    .append("\n");
        }
        return sb.toString();
    }

    private static String stripDataPrefix(String b64) {
        return b64.contains(",") ? b64.substring(b64.indexOf(",") + 1) : b64;
    }

    private static String nvl(String s) {
        return s != null ? s : "";
    }

    private static String escJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "");
    }

    // ── Result type ──────────────────────────────────────────────────────────

    public record SearchResult(String message, List<LostItem> items) {
        public String toJson() {
            String itemsJson = items.stream()
                    .map(LostItem::toApiJson)
                    .collect(Collectors.joining(","));
            return "{\"message\":\"" + esc(message) + "\",\"items\":[" + itemsJson + "]}";
        }

        private static String esc(String s) {
            if (s == null) return "";
            return s.replace("\\", "\\\\").replace("\"", "\\\"")
                    .replace("\n", "\\n").replace("\r", "");
        }
    }
}
