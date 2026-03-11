package lostandfound;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles all communication with the Anthropic Claude API.
 * - analyzeImage(): takes a base64 image, returns a structured description
 * - searchItems(): takes a natural language query + all items, returns ranked matches
 */
public class ClaudeService {

    // ⚠️  Replace this with your actual Anthropic API key
    // Get one at: https://console.anthropic.com
    private static final String API_KEY = System.getenv().getOrDefault("ANTHROPIC_API_KEY", "YOUR_API_KEY_HERE");
    private static final String API_URL = "https://api.anthropic.com/v1/messages";
    private static final String MODEL = "claude-opus-4-6";

    private static final HttpClient HTTP = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(30))
        .build();

    // ── Image Analysis ───────────────────────────────────────────────────────

    /**
     * Sends an image to Claude and gets back a detailed description
     * suitable for lost & found cataloguing.
     */
    public static String analyzeImage(String base64Image, String mediaType) throws Exception {
        String cleanBase64 = base64Image;
        if (base64Image.contains(",")) {
            cleanBase64 = base64Image.substring(base64Image.indexOf(",") + 1);
        }
        if (mediaType == null || mediaType.isBlank()) mediaType = "image/jpeg";

        String body = """
        {
          "model": "%s",
          "max_tokens": 600,
          "messages": [{
            "role": "user",
            "content": [
              {
                "type": "image",
                "source": {
                  "type": "base64",
                  "media_type": "%s",
                  "data": "%s"
                }
              },
              {
                "type": "text",
                "text": "You are helping a school's Lost and Found department. Analyze this image of a lost item and provide a structured description. Include:\\n1. What the item is (be specific)\\n2. Color(s)\\n3. Brand or text visible (if any)\\n4. Condition (good/fair/worn)\\n5. Any unique identifiers (names, initials, stickers, damage)\\n6. Approximate size\\nBe concise and factual. Format as plain text, not JSON."
              }
            ]
          }]
        }
        """.formatted(MODEL, mediaType, cleanBase64);

        return callApi(body);
    }

    // ── Natural Language Search ──────────────────────────────────────────────

    /**
     * Uses Claude to match a natural language query against all items in the DB.
     * Returns a JSON array of item IDs sorted by relevance, with reasoning.
     */
    public static SearchResult searchWithAI(String query, List<LostItem> allItems) throws Exception {
        if (allItems.isEmpty()) {
            return new SearchResult("No items in the database yet.", List.of());
        }

        // Build a compact catalogue of items to send to Claude
        StringBuilder catalogue = new StringBuilder();
        for (LostItem item : allItems) {
            catalogue.append("ID:").append(item.id)
                .append(" | Category:").append(nvl(item.category))
                .append(" | Color:").append(nvl(item.color))
                .append(" | Description:").append(nvl(item.description))
                .append(" | Marks:").append(nvl(item.identifyingMarks))
                .append(" | Location:").append(nvl(item.locationFound))
                .append(" | AI-Description:").append(nvl(item.aiDescription))
                .append(" | Status:").append(nvl(item.status))
                .append("\n");
        }

        String body = """
        {
          "model": "%s",
          "max_tokens": 800,
          "messages": [{
            "role": "user",
            "content": "You are an AI assistant for a school Lost and Found department.\\n\\nA student or parent is searching for a lost item with this description:\\n\\\"%s\\\"\\n\\nHere is the current inventory of found items (one per line):\\n%s\\n\\nInstructions:\\n1. Find items that match the search query. Consider color, type, identifying marks, descriptions.\\n2. Return ONLY a JSON object in this exact format (no other text):\\n{\\\"matches\\\": [\\\"ID1\\\", \\\"ID2\\\"], \\\"message\\\": \\\"A helpful message explaining what was found or not found\\\"}\\n3. List best matches first. Include partial matches too.\\n4. If nothing matches, return empty array and explain in message.\\n5. The message should be friendly and helpful for a school setting."
          }]
        }
        """.formatted(MODEL, escJson(query), escJson(catalogue.toString()));

        String response = callApi(body);
        return parseSearchResult(response, allItems);
    }

    // ── Private helpers ──────────────────────────────────────────────────────

    private static String callApi(String body) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(API_URL))
            .header("Content-Type", "application/json")
            .header("x-api-key", API_KEY)
            .header("anthropic-version", "2023-06-01")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .timeout(Duration.ofSeconds(60))
            .build();

        HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());
        String responseBody = response.body();

        if (response.statusCode() != 200) {
            System.err.println("Claude API error " + response.statusCode() + ": " + responseBody);
            throw new RuntimeException("Claude API error: " + response.statusCode());
        }

        // Extract text from: {"content":[{"type":"text","text":"..."}],...}
        return extractApiText(responseBody);
    }

    private static String extractApiText(String apiResponse) {
        // Find "text":"..."  in the response
        String marker = "\"text\":\"";
        int start = apiResponse.indexOf(marker);
        if (start < 0) return "Unable to parse response";
        start += marker.length();

        StringBuilder sb = new StringBuilder();
        int i = start;
        while (i < apiResponse.length()) {
            char c = apiResponse.charAt(i);
            if (c == '\\' && i + 1 < apiResponse.length()) {
                char next = apiResponse.charAt(i + 1);
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
        // Extract the JSON object from Claude's response
        int jsonStart = aiResponse.indexOf('{');
        int jsonEnd = aiResponse.lastIndexOf('}');
        if (jsonStart < 0 || jsonEnd < 0) {
            return new SearchResult(aiResponse, List.of());
        }
        String json = aiResponse.substring(jsonStart, jsonEnd + 1);

        String message = LostItem.extractField(json, "message");
        if (message == null) message = "Search complete.";

        // Extract matches array: ["ID1","ID2"]
        List<String> ids = new java.util.ArrayList<>();
        int arrStart = json.indexOf("\"matches\"");
        if (arrStart >= 0) {
            int bracket = json.indexOf('[', arrStart);
            int bracketEnd = json.indexOf(']', bracket);
            if (bracket >= 0 && bracketEnd >= 0) {
                String arrContent = json.substring(bracket + 1, bracketEnd);
                for (String part : arrContent.split(",")) {
                    String id = part.trim().replace("\"", "").trim();
                    if (!id.isBlank()) ids.add(id);
                }
            }
        }

        // Map IDs back to items
        List<LostItem> matched = ids.stream()
            .map(id -> allItems.stream().filter(it -> id.equals(it.id)).findFirst().orElse(null))
            .filter(it -> it != null)
            .collect(Collectors.toList());

        return new SearchResult(message, matched);
    }

    private static String nvl(String s) { return s != null ? s : ""; }

    private static String escJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");
    }

    // ── Result type ──────────────────────────────────────────────────────────

    public record SearchResult(String message, List<LostItem> items) {
        public String toJson() {
            String itemsJson = items.stream()
                .map(LostItem::toApiJson)
                .collect(Collectors.joining(","));
            return "{\"message\":\"" + escJson(message) + "\",\"items\":[" + itemsJson + "]}";
        }
        private static String escJson(String s) {
            if (s == null) return "";
            return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");
        }
    }
}
