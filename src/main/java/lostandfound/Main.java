package lostandfound;

import com.sun.net.httpserver.*;
import javax.net.ssl.SSLParameters;
import java.net.*;
import java.util.concurrent.Executors;

public class Main {

    public static void main(String[] args) throws Exception {
        // Load configuration first
        AppConfig.load();

        // Initialize database
        Database.init();

        // Build SSL context from auto-generated self-signed certificate
        var sslContext = CertificateGenerator.createSslContext();

        // Create HTTPS server
        HttpsServer server = HttpsServer.create(new InetSocketAddress(AppConfig.serverPort), 0);
        server.setHttpsConfigurator(new HttpsConfigurator(sslContext) {
            @Override
            public void configure(HttpsParameters params) {
                SSLParameters sslParams = getSSLContext().getDefaultSSLParameters();
                params.setSSLParameters(sslParams);
            }
        });

        // Routes
        server.createContext("/", new StaticHandler());
        server.createContext("/api/items", new ItemsHandler());
        server.createContext("/api/search", new SearchHandler());
        server.createContext("/api/upload", new UploadHandler());

        server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
        server.start();

        String localIp = getLocalIp();

        System.out.println("╔══════════════════════════════════════════════════════╗");
        System.out.println("║   🎒  Lost & Found AI Agent — School Edition         ║");
        System.out.println("╠══════════════════════════════════════════════════════╣");
        System.out.println("║   On this computer:                                  ║");
        System.out.printf( "║   👉  https://localhost:%-28d║%n", AppConfig.serverPort);
        System.out.println("║                                                      ║");
        System.out.println("║   On your phone (same WiFi):                         ║");
        System.out.printf( "║   👉  https://%-38s║%n", localIp + ":" + AppConfig.serverPort);
        System.out.println("╠══════════════════════════════════════════════════════╣");
        System.out.println("║   ⚠️  First visit: browser will warn about the cert  ║");
        System.out.println("║   Click 'Advanced' then 'Proceed' to continue        ║");
        System.out.println("║   You only need to do this once per device           ║");
        System.out.println("╚══════════════════════════════════════════════════════╝");
    }

    private static String getLocalIp() {
        try {
            return NetworkInterface.networkInterfaces()
                .filter(ni -> {
                    try { return ni.isUp() && !ni.isLoopback(); } catch (Exception e) { return false; }
                })
                .flatMap(NetworkInterface::inetAddresses)
                .filter(addr -> addr instanceof Inet4Address && !addr.isLoopbackAddress())
                .map(InetAddress::getHostAddress)
                .findFirst()
                .orElse("YOUR_COMPUTER_IP");
        } catch (Exception e) {
            return "YOUR_COMPUTER_IP";
        }
    }
}
