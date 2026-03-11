package lostandfound;

import com.sun.net.httpserver.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static lostandfound.HandlerUtils.*;

// ════════════════════════════════════════════════════════════════════
// Static file handler - serves the frontend HTML
// ════════════════════════════════════════════════════════════════════
class StaticHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange ex) throws IOException {
        String path = ex.getRequestURI().getPath();

        if (path.equals("/") || path.equals("/index.html")) {
            byte[] body = Frontend.getHtml().getBytes(StandardCharsets.UTF_8);
            ex.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
            ex.sendResponseHeaders(200, body.length);
            ex.getResponseBody().write(body);
        } else if (path.startsWith("/images/")) {
            String filename = path.substring("/images/".length());
            byte[] img = Database.loadImage(filename);
            if (img == null) {
                ex.sendResponseHeaders(404, -1);
            } else {
                String ct = filename.endsWith(".png") ? "image/png" :
                            filename.endsWith(".gif") ? "image/gif" : "image/jpeg";
                ex.getResponseHeaders().set("Content-Type", ct);
                ex.sendResponseHeaders(200, img.length);
                ex.getResponseBody().write(img);
            }
        } else {
            ex.sendResponseHeaders(404, -1);
        }
        ex.getResponseBody().close();
    }
}

// ════════════════════════════════════════════════════════════════════
// GET /api/items  → list all items
// PATCH /api/items/{id}/status  → mark as claimed
// ════════════════════════════════════════════════════════════════════
class ItemsHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange ex) throws IOException {
        addCors(ex);
        try {
            String path = ex.getRequestURI().getPath();
            String method = ex.getRequestMethod();

            if (method.equals("OPTIONS")) { ex.sendResponseHeaders(204, -1); return; }

            if (method.equals("GET") && path.equals("/api/items")) {
                List<LostItem> items = Database.getAllItems();
                String json = "[" + items.stream().map(LostItem::toApiJson).collect(Collectors.joining(",")) + "]";
                sendJson(ex, 200, json);

            } else if (method.equals("POST") && path.matches("/api/items/[^/]+/status")) {
                String id = path.split("/")[3];
                String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                String status = LostItem.extractField("{" + body + "}", "status");
                String claimedBy = LostItem.extractField("{" + body + "}", "claimedBy");

                Optional<LostItem> opt = Database.getItem(id);
                if (opt.isEmpty()) { sendJson(ex, 404, "{\"error\":\"Not found\"}"); return; }

                LostItem item = opt.get();
                item.status = status != null ? status : "claimed";
                item.claimedBy = claimedBy;
                Database.saveItem(item);
                sendJson(ex, 200, item.toApiJson());

            } else {
                sendJson(ex, 404, "{\"error\":\"Not found\"}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendJson(ex, 500, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
}

// ════════════════════════════════════════════════════════════════════
// POST /api/upload  → add a new found item (with optional image)
// ════════════════════════════════════════════════════════════════════
class UploadHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange ex) throws IOException {
        addCors(ex);
        if (ex.getRequestMethod().equals("OPTIONS")) { ex.sendResponseHeaders(204, -1); return; }
        if (!ex.getRequestMethod().equals("POST")) { sendJson(ex, 405, "{\"error\":\"Method not allowed\"}"); return; }

        try {
            String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);

            LostItem item = new LostItem();
            item.locationFound   = LostItem.extractField(body, "locationFound");
            item.description     = LostItem.extractField(body, "description");
            item.color           = LostItem.extractField(body, "color");
            item.identifyingMarks= LostItem.extractField(body, "identifyingMarks");
            item.category        = LostItem.extractField(body, "category");
            item.dateFound       = LostItem.extractField(body, "dateFound");

            String imageData     = LostItem.extractField(body, "imageData");
            String mediaType     = LostItem.extractField(body, "mediaType");

            // Generate ID first so we can name the image after it
            item.id = UUID.randomUUID().toString().substring(0, 8).toUpperCase();

            // Save image if provided
            if (imageData != null && !imageData.isBlank()) {
                System.out.println("📸 Saving image for item " + item.id);
                String imgFilename = Database.saveImage(imageData, item.id);
                item.imageFilename = imgFilename;

                // Run AI image analysis
                System.out.println("🤖 Sending to Ollama for analysis (" + AppConfig.ollamaModel + ")...");
                try {
                    item.aiDescription = AIService.analyzeImage(imageData);
                    System.out.println("✅ AI analysis complete");
                } catch (Exception aiEx) {
                    System.err.println("⚠️  AI analysis failed: " + aiEx.getMessage());
                    item.aiDescription = "AI analysis unavailable";
                }
            }

            Database.saveItem(item);
            System.out.println("✅ Item saved: " + item.id + " - " + item.description);
            sendJson(ex, 201, item.toApiJson());

        } catch (Exception e) {
            e.printStackTrace();
            sendJson(ex, 500, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
}

// ════════════════════════════════════════════════════════════════════
// POST /api/search  → AI-powered natural language search
// ════════════════════════════════════════════════════════════════════
class SearchHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange ex) throws IOException {
        addCors(ex);
        if (ex.getRequestMethod().equals("OPTIONS")) { ex.sendResponseHeaders(204, -1); return; }
        if (!ex.getRequestMethod().equals("POST")) { sendJson(ex, 405, "{\"error\":\"Method not allowed\"}"); return; }

        try {
            String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            String query = LostItem.extractField(body, "query");

            if (query == null || query.isBlank()) {
                sendJson(ex, 400, "{\"error\":\"Query is required\"}");
                return;
            }

            System.out.println("🔍 Search query: " + query);
            List<LostItem> allItems = Database.getAllItems();
            AIService.SearchResult result = AIService.searchWithAI(query, allItems);
            System.out.println("✅ Found " + result.items().size() + " matches");
            sendJson(ex, 200, result.toJson());

        } catch (Exception e) {
            e.printStackTrace();
            sendJson(ex, 500, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
}

// ════════════════════════════════════════════════════════════════════
// Shared utilities
// ════════════════════════════════════════════════════════════════════
class HandlerUtils {
    static void sendJson(HttpExchange ex, int status, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        ex.sendResponseHeaders(status, bytes.length);
        ex.getResponseBody().write(bytes);
        ex.getResponseBody().close();
    }
    static void addCors(HttpExchange ex) {
        ex.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        ex.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        ex.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
    }
}

// make helpers accessible from the handler classes in same package
// (Java doesn't need to import same-package classes)
