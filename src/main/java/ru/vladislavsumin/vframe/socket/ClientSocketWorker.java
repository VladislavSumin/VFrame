package ru.vladislavsumin.vframe.socket;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.vladislavsumin.vframe.VFrameRuntimeException;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

/**
 * Base client socket worker class
 *
 * @author Sumin Vladislav
 * @version 0.1
 */
public class ClientSocketWorker {
    private static final Logger log = LogManager.getLogger();

    private final String ip;
    private final int port;

    private SSLSocketFactory ssf;


    public ClientSocketWorker(String ip, int port, InputStream keystore, String keystorePassword) {
        this.ip = ip;
        this.port = port;

        init(keystore, keystorePassword);
    }


    public ClientSocketWorker(String ip, int port, String path, String keystorePassword) {
        this.ip = ip;
        this.port = port;

        InputStream is;
        try {
            is = new FileInputStream(path);
        } catch (FileNotFoundException e) {
            log.fatal("VFrame can not open keystore file", e);
            throw new VFrameRuntimeException(e);
        }
        init(is, keystorePassword);
    }

    private void init(InputStream keystore, String keystorePassword) {
        initKeystore(keystore, keystorePassword);
    }

    private void initKeystore(InputStream keystore, String keystorePassword) {
        try {
            String algorithm = KeyManagerFactory.getDefaultAlgorithm();

            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(keystore, keystorePassword.toCharArray());

            TrustManagerFactory tmf = TrustManagerFactory.getInstance(algorithm);
            tmf.init(ks);

            SSLContext sc = SSLContext.getInstance("TLS");
            TrustManager[] trustManagers = tmf.getTrustManagers();
            sc.init(null, trustManagers, null);

            ssf = sc.getSocketFactory();
        } catch (IOException | CertificateException | NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
            throw new RuntimeException(e);
        }
    }
}
