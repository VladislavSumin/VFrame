package ru.vladislavsumin.vframe.socket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.vladislavsumin.vframe.VFrameRuntimeException;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

/**
 * Create {@link SSLServerSocket}
 *
 * @author Vladislav Sumin
 * @author Evgeny Rudzyansky
 * @version 1.4
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class SSLServerSocketFactory {
    private static Logger log = LogManager.getLogger(javax.net.ssl.SSLServerSocketFactory.class.getName());

    private javax.net.ssl.SSLServerSocketFactory ssf;

    /**
     * @param path            - path to keystore
     * @param publicPassword  - public password
     * @param privatePassword - private password
     * @throws VFrameRuntimeException if can not initialize factory
     */
    public SSLServerSocketFactory(String path, String publicPassword, String privatePassword)
            throws VFrameRuntimeException {
        String algorithm = KeyManagerFactory.getDefaultAlgorithm();

        // Загружаем связку ключей.
        KeyStore keyStore;
        try {
            keyStore = KeyStore.getInstance("JKS");
            keyStore.load(new FileInputStream(path),
                    publicPassword.toCharArray());
        } catch (CertificateException | NoSuchAlgorithmException | IOException | KeyStoreException e) {
            log.fatal("VFrame: Can not load keyStore", e);
            throw new VFrameRuntimeException(e);
        }

        try {
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(algorithm);
            kmf.init(keyStore, privatePassword.toCharArray());

            TrustManagerFactory tmf = TrustManagerFactory.getInstance(algorithm);
            tmf.init(keyStore);

            SSLContext sc = SSLContext.getInstance("TLS");
            TrustManager[] trustManagers = tmf.getTrustManagers();
            sc.init(kmf.getKeyManagers(), trustManagers, null);

            ssf = sc.getServerSocketFactory();
        } catch (Exception e) {
            log.fatal("VFrame: Can not create server socket factory", e);
            throw new VFrameRuntimeException(e);
        }
    }

    /**
     * @param port -  port
     * @return {@link SSLServerSocket}
     * @throws RuntimeException if cannot bind socket.
     */
    public ServerSocket createServerSocket(int port) throws VFrameRuntimeException {
        try {
            return ssf.createServerSocket(port);
        } catch (IOException e) {
            log.fatal("VFrame: Cannot open port {}");
            throw new VFrameRuntimeException(e);
        }
    }
}
