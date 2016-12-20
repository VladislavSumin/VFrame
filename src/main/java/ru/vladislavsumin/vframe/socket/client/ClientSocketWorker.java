package ru.vladislavsumin.vframe.socket.client;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.vladislavsumin.vframe.VFrameRuntimeException;
import ru.vladislavsumin.vframe.serializable.Container;

import javax.net.ssl.*;
import java.io.*;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Map;

/**
 * Base client socket worker class
 *
 * @author Sumin Vladislav
 * @version 1.2
 */
@SuppressWarnings("unused")
public class ClientSocketWorker {
    private static final Logger log = LogManager.getLogger();

    private final Object lock = new Object();

    private final String ip;
    private final int port;

    private final Map<String, ClientProtocolInterface> protocols = new HashMap<>();

    private SSLSocketFactory ssf;
    private SSLSocket socket;
    private ObjectOutputStream out;

    private boolean work = false;
    private boolean connected = false;
    long lastPing = System.currentTimeMillis();

    private final ClientSocketWorker link = this;
    private final Runnable run = new Runnable() {
        @Override
        public void run() {
            while (work) {
                try {
                    initSSL();
                    out = new ObjectOutputStream(socket.getOutputStream());
                    ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                    connected = true;
                    while (work) {
                        Container container = (Container) in.readObject();
                        ClientProtocolInterface protocol = protocols.get(container.protocol);
                        if (protocol == null) {
                            log.error("VFrame: Server used unknown protocol {}", container.protocol);
                            continue;
                        }
                        protocol.exec(container.data, link);
                    }
                } catch (ClassNotFoundException | IOException ignore) {
                }
                disconnect();
                sleep();
            }
        }
    };

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
        addProtocol(new Ping());
    }

    public void start() {
        synchronized (lock) {
            if (work) {
                log.error("VFrame: ClientSocketWorker already worked");
                return;
            }
            work = true;
            new Thread(run, "Client socket worker thread").start();
        }
    }

    public void stop() {
        synchronized (lock) {
            if (!work) {
                log.error("VFrame: ClientSocketWorker already stopped");
                return;
            }
            work = false;
            disconnect();
        }
    }

    public boolean send(Container container) {
        synchronized (lock) {
            try {
                out.reset();
                out.writeObject(container);
                out.flush();
            } catch (Exception ignore) {
                disconnect();
                return false;
            }
            return true;
        }
    }

    public void addProtocol(ClientProtocolInterface protocol) {
        protocols.put(protocol.getName(), protocol);
    }

    private synchronized void disconnect() {
        synchronized (lock) {
            if (!connected) return;
            connected = false;
            try {
                socket.close();
            } catch (Exception ignore) {
            }
        }
    }

    private void initSSL() throws IOException {
        socket = (SSLSocket) ssf.createSocket(ip, port);
        socket.setEnabledCipherSuites(new String[]{"TLS_RSA_WITH_AES_128_CBC_SHA"});
        socket.setEnabledProtocols(new String[]{"TLSv1.2"});
        socket.setEnableSessionCreation(true);
        socket.setUseClientMode(true);
        socket.startHandshake();
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

    private void sleep() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
