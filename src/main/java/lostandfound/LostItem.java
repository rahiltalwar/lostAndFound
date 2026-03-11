package lostandfound;

/**
 * Represents a lost item in the system.
 * Uses manual JSON serialization to avoid external dependencies.
 */
public class LostItem {
    public String id;
    public String dateFound;
    public String locationFound;
    public String description;
    public String color;
    public String identifyingMarks;   // name, grade, initials engraved, etc.
    public String category;           // bottle, bag, clothing, electronics, etc.
    public String imageFilename;
    public String aiDescription;      // Claude's analysis of the uploaded image
    public String status;             // unclaimed | claimed
    public String claimedBy;

    // ── JSON helpers (hand-rolled to avoid Gson dependency) ──────────────────

    public String toJson() {
        return "{\n" +
            jsonField("id", id) + ",\n" +
            jsonField("dateFound", dateFound) + ",\n" +
            jsonField("locationFound", locationFound) + ",\n" +
            jsonField("description", description) + ",\n" +
            jsonField("color", color) + ",\n" +
            jsonField("identifyingMarks", identifyingMarks) + ",\n" +
            jsonField("category", category) + ",\n" +
            jsonField("imageFilename", imageFilename) + ",\n" +
            jsonField("aiDescription", aiDescription) + ",\n" +
            jsonField("status", status) + ",\n" +
            jsonField("claimedBy", claimedBy) + "\n" +
            "}";
    }

    private String jsonField(String key, String value) {
        if (value == null) return "  \"" + key + "\": null";
        // Escape special characters
        String escaped = value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t");
        return "  \"" + key + "\": \"" + escaped + "\"";
    }

    public static LostItem fromJson(String json) {
        LostItem item = new LostItem();
        item.id = extractField(json, "id");
        item.dateFound = extractField(json, "dateFound");
        item.locationFound = extractField(json, "locationFound");
        item.description = extractField(json, "description");
        item.color = extractField(json, "color");
        item.identifyingMarks = extractField(json, "identifyingMarks");
        item.category = extractField(json, "category");
        item.imageFilename = extractField(json, "imageFilename");
        item.aiDescription = extractField(json, "aiDescription");
        item.status = extractField(json, "status");
        item.claimedBy = extractField(json, "claimedBy");
        return item;
    }

    /**
     * Minimal JSON string field extractor.
     * Handles escaped characters and null values.
     */
    static String extractField(String json, String key) {
        String search = "\"" + key + "\":";
        int keyIdx = json.indexOf(search);
        if (keyIdx < 0) return null;

        int valueStart = keyIdx + search.length();
        // skip whitespace
        while (valueStart < json.length() && Character.isWhitespace(json.charAt(valueStart))) {
            valueStart++;
        }
        if (valueStart >= json.length()) return null;

        // null value
        if (json.startsWith("null", valueStart)) return null;

        // string value
        if (json.charAt(valueStart) == '"') {
            StringBuilder sb = new StringBuilder();
            int i = valueStart + 1;
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
        return null;
    }

    /** Convert to a JSON object string suitable for API responses (compact) */
    public String toApiJson() {
        return "{" +
            "\"id\":\"" + esc(id) + "\"," +
            "\"dateFound\":\"" + esc(dateFound) + "\"," +
            "\"locationFound\":\"" + esc(locationFound) + "\"," +
            "\"description\":\"" + esc(description) + "\"," +
            "\"color\":\"" + esc(color) + "\"," +
            "\"identifyingMarks\":\"" + esc(identifyingMarks) + "\"," +
            "\"category\":\"" + esc(category) + "\"," +
            "\"imageFilename\":" + (imageFilename != null ? "\"" + esc(imageFilename) + "\"" : "null") + "," +
            "\"aiDescription\":\"" + esc(aiDescription) + "\"," +
            "\"status\":\"" + esc(status) + "\"," +
            "\"claimedBy\":" + (claimedBy != null ? "\"" + esc(claimedBy) + "\"" : "null") +
            "}";
    }

    private String esc(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "").replace("\t", " ");
    }
}
