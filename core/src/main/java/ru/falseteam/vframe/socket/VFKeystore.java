package ru.falseteam.vframe.socket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.falseteam.vframe.VFrame;
import ru.falseteam.vframe.VFrameRuntimeException;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.security.KeyStore;

/**
 * VFrame keystore container
 *
 * @author Sumin Vladislav
 * @version 1.5
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class VFKeystore {
    private static final Logger log = LogManager.getLogger();
    private final SSLContext sslContext;

    public VFKeystore(InputStream path, String publicPassword, String privatePassword) {
        try {
            String algorithm = KeyManagerFactory.getDefaultAlgorithm();

            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(path, publicPassword.toCharArray());

            KeyManager[] keyManagers = null;
            if (privatePassword != null) {
                KeyManagerFactory kmf = KeyManagerFactory.getInstance(algorithm);
                kmf.init(keyStore, privatePassword.toCharArray());
                keyManagers = kmf.getKeyManagers();
            }

            TrustManagerFactory tmf = TrustManagerFactory.getInstance(algorithm);
            tmf.init(keyStore);

            sslContext = SSLContext.getInstance("TLS");
            TrustManager[] trustManagers = tmf.getTrustManagers();
            sslContext.init(keyManagers, trustManagers, null);

            VFrame.print("VFrame: keystore loaded");

        } catch (Exception e) {
            log.fatal("VFrame: Can not load keyStore");
            throw new VFrameRuntimeException(e);
        }
    }

    public VFKeystore(String path, String publicPassword, String privatePassword) throws FileNotFoundException {
        this(new FileInputStream(path), publicPassword, privatePassword);
    }

    public VFKeystore(String path, String publicPassword) throws FileNotFoundException {
        this(new FileInputStream(path), publicPassword, null);
    }

    public VFKeystore(InputStream path, String publicPassword) {
        this(path, publicPassword, null);
    }

    public SSLContext getSSLContext() {
        return sslContext;
    }
}
