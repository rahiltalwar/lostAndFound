package lostandfound;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class Main {

    public static void main(String[] args) throws Exception {
        // Load configuration first (reads config.properties or writes a default one)
        AppConfig.load();

        // Initialize database
        Database.init();

        HttpServer server = HttpServer.create(new InetSocketAddress(AppConfig.serverPort), 0);

        // Routes
        server.createContext("/", new StaticHandler());
        server.createContext("/api/items", new ItemsHandler());
        server.createContext("/api/search", new SearchHandler());
        server.createContext("/api/upload", new UploadHandler());

        server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
        server.start();

        System.out.println("╔══════════════════════════════════════════════╗");
        System.out.println("║   🎒 Lost & Found AI Agent - School Edition  ║");
        System.out.println("║──────────────────────────────────────────────║");
        System.out.println("║   Open your browser and go to:               ║");
        System.out.printf( "║   👉  http://localhost:%-22d║%n", AppConfig.serverPort);
        System.out.println("╚══════════════════════════════════════════════╝");
    }
}
