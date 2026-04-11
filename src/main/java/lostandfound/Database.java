package lostandfound;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Simple JSON-file based database.
 * Each item is stored as a JSON file in ./data/items/
 * No external dependencies required.
 */
public class Database {

    private static final Path DATA_DIR = Path.of("data", "items");
    private static final Path IMAGES_DIR = Path.of("data", "images");

    public static void init() throws IOException {
        Files.createDirectories(DATA_DIR);
        Files.createDirectories(IMAGES_DIR);
        System.out.println("📁 Database initialized at: " + DATA_DIR.toAbsolutePath());
    }

    public static LostItem saveItem(LostItem item) throws IOException {
        if (item.id == null || item.id.isBlank()) {
            item.id = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }
        if (item.dateFound == null || item.dateFound.isBlank()) {
            item.dateFound = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        }
        item.status = item.status != null ? item.status : "unclaimed";

        Path file = DATA_DIR.resolve(item.id + ".json");
        Files.writeString(file, item.toJson());
        return item;
    }

    public static List<LostItem> getAllItems() throws IOException {
        if (!Files.exists(DATA_DIR)) return new ArrayList<>();
        try (var stream = Files.list(DATA_DIR)) {
            return stream
                .filter(p -> p.toString().endsWith(".json"))
                .map(p -> {
                    try { return LostItem.fromJson(Files.readString(p)); }
                    catch (Exception e) { return null; }
                })
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing((LostItem i) -> i.dateFound).reversed())
                .collect(Collectors.toList());
        }
    }

    public static List<LostItem> getItemsBySchool(String school) throws IOException {
        if (school == null || school.isBlank()) {
            return new ArrayList<>();
        }
        return getAllItems().stream()
                .filter(item -> school.equalsIgnoreCase(item.school))
                .collect(Collectors.toList());
    }

    public static Optional<LostItem> getItem(String id) throws IOException {
        Path file = DATA_DIR.resolve(id + ".json");
        if (!Files.exists(file)) return Optional.empty();
        return Optional.of(LostItem.fromJson(Files.readString(file)));
    }

    public static boolean updateStatus(String id, String status) throws IOException {
        Optional<LostItem> opt = getItem(id);
        if (opt.isEmpty()) return false;
        LostItem item = opt.get();
        item.status = status;
        saveItem(item);
        return true;
    }

    public static String saveImage(String base64Data, String itemId) throws IOException {
        // base64Data may have data:image/jpeg;base64, prefix - strip it
        String data = base64Data;
        String ext = "jpg";
        if (base64Data.contains(",")) {
            String header = base64Data.substring(0, base64Data.indexOf(","));
            data = base64Data.substring(base64Data.indexOf(",") + 1);
            if (header.contains("png")) ext = "png";
            else if (header.contains("gif")) ext = "gif";
            else if (header.contains("webp")) ext = "webp";
        }
        String filename = itemId + "." + ext;
        Path imgPath = IMAGES_DIR.resolve(filename);
        byte[] bytes = Base64.getDecoder().decode(data);
        Files.write(imgPath, bytes);
        return filename;
    }

    public static byte[] loadImage(String filename) throws IOException {
        Path imgPath = IMAGES_DIR.resolve(filename);
        if (!Files.exists(imgPath)) return null;
        return Files.readAllBytes(imgPath);
    }
}
