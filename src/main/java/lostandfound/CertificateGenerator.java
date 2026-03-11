package lostandfound;

import javax.net.ssl.*;
import java.io.*;
import java.nio.file.*;
import java.security.*;

/**
 * Generates (or loads) a self-signed TLS certificate using the keytool
 * command that ships with every JDK. No internal Sun APIs needed.
 *
 * Certificate is saved to data/keystore.jks and reused across restarts
 * so the browser only needs to accept it once per device.
 */
public class CertificateGenerator {

    private static final Path   KEYSTORE_PATH = Path.of("data", "keystore.jks");
    private static final String KEYSTORE_PASS = "lostandfound";
    private static final String KEY_ALIAS     = "lostandfound";

    public static SSLContext createSslContext() throws Exception {
        Files.createDirectories(KEYSTORE_PATH.getParent());

        if (!Files.exists(KEYSTORE_PATH)) {
            generateWithKeytool();
        } else {
            System.out.println("🔐 Loading TLS certificate from " + KEYSTORE_PATH.toAbsolutePath());
        }

        return buildSslContext();
    }

    // ── Generate via keytool ─────────────────────────────────────────────────

    private static void generateWithKeytool() throws Exception {
        System.out.println("🔐 Generating self-signed TLS certificate (valid 10 years)...");

        // Find keytool next to the running JVM so it's always the right version
        String javaHome = System.getProperty("java.home");
        String keytool  = javaHome + File.separator + "bin" + File.separator + "keytool";
        if (!new File(keytool).exists()) keytool = "keytool"; // fall back to PATH

        ProcessBuilder pb = new ProcessBuilder(
            keytool,
            "-genkeypair",
            "-alias",     KEY_ALIAS,
            "-keyalg",    "RSA",
            "-keysize",   "2048",
            "-validity",  "3650",          // 10 years
            "-keystore",  KEYSTORE_PATH.toString(),
            "-storepass", KEYSTORE_PASS,
            "-keypass",   KEYSTORE_PASS,
            "-dname",     "CN=LostAndFound, O=School, C=IN",
            "-noprompt"
        );
        pb.redirectErrorStream(true);
        Process p = pb.start();
        String output = new String(p.getInputStream().readAllBytes());
        int exit = p.waitFor();

        if (exit != 0) {
            throw new RuntimeException("keytool failed (exit " + exit + "):\n" + output);
        }
        System.out.println("✅ Certificate saved to " + KEYSTORE_PATH.toAbsolutePath());
    }

    // ── Load keystore → SSLContext ───────────────────────────────────────────

    private static SSLContext buildSslContext() throws Exception {
        KeyStore ks = KeyStore.getInstance("JKS");
        try (InputStream in = Files.newInputStream(KEYSTORE_PATH)) {
            ks.load(in, KEYSTORE_PASS.toCharArray());
        }

        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(ks, KEYSTORE_PASS.toCharArray());

        SSLContext ctx = SSLContext.getInstance("TLS");
        ctx.init(kmf.getKeyManagers(), null, null);
        return ctx;
    }
}
