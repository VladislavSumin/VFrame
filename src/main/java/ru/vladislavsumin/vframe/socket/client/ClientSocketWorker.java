package ru.vladislavsumin.vframe.socket.client;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.vladislavsumin.vframe.VFrame;
import ru.vladislavsumin.vframe.VFrameRuntimeException;
import ru.vladislavsumin.vframe.serializable.Container;
import ru.vladislavsumin.vframe.socket.VFKeystore;

import javax.net.ssl.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.*;

/**
 * Base client socket worker class
 *
 * @author Sumin Vladislav
 * @version 4.0
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class ClientSocketWorker {
    private static final Logger log = LogManager.getLogger();

    private final Object lock = new Object();

    private final SocketAddress socketAddress;

    private final Map<String, ClientProtocolAbstract> protocols = new HashMap<>();
    private final Set<OnConnectionChangeStateListener> listeners = new HashSet<>();

    private SSLSocketFactory ssf;
    private SSLSocket socket;
    private ObjectOutputStream out;

    private boolean work = false;
    private boolean connected = false;
    long lastPing = System.currentTimeMillis();

    public interface OnConnectionChangeStateListener {
        void onConnectionChangeState(boolean connected);
    }

    private final ClientSocketWorker link = this;
    private final Runnable run = new Runnable() {
        @Override
        public void run() {
            while (work) {
                try {
                    initSSL();
                    out = new ObjectOutputStream(socket.getOutputStream());
                    ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                    setConnected(true);
                    while (work) {
                        Container container = (Container) in.readObject();
                        ClientProtocolAbstract protocol = protocols.get(container.protocol);
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

    private TimerTask pingTask;

    public ClientSocketWorker(String ip, int port, VFKeystore keystore) {
        socketAddress = new InetSocketAddress(ip, port);
        ssf = keystore.getSSLContext().getSocketFactory();
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

            pingTask = new TimerTask() {
                @Override
                public void run() {
                    if (connected && System.currentTimeMillis() - lastPing > 24000) {
                        disconnect();
                    }
                }
            };
            VFrame.addPeriodicalTimerTask(pingTask, 16000);
        }
    }

    public void stop() {
        synchronized (lock) {
            if (!work) {
                log.error("VFrame: ClientSocketWorker already stopped");
                return;
            }
            work = false;
            pingTask.cancel();
            pingTask = null;
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

    public void sendFromMainThread(final Container container) {
        new Thread() {
            @Override
            public void run() {
                send(container);
            }
        }.start();
    }

    public void addProtocol(ClientProtocolAbstract protocol) {
        protocols.put(protocol.getName(), protocol);
    }

    private synchronized void disconnect() {
        synchronized (lock) {
            if (!connected) return;
            setConnected(false);
            try {
                socket.close();
            } catch (Exception ignore) {
            }
        }
    }

    private void initSSL() throws IOException {
        socket = (SSLSocket) ssf.createSocket();
        socket.connect(socketAddress, 5000);
        socket.setEnabledCipherSuites(new String[]{"TLS_RSA_WITH_AES_128_CBC_SHA"});
        socket.setEnabledProtocols(new String[]{"TLSv1.2"});
        socket.setEnableSessionCreation(true);
        socket.setUseClientMode(true);
        socket.startHandshake();
    }

    private void sleep() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void setConnected(boolean connected) {
        this.connected = connected;
        synchronized (listeners) {
            for (OnConnectionChangeStateListener listener : listeners) {
                listener.onConnectionChangeState(connected);
            }
        }
    }

    public void addOnConnectionChangeStateListener(OnConnectionChangeStateListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    public void removeOnConnectionChangeStateListener(OnConnectionChangeStateListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    public boolean isConnected() {
        return connected;
    }
}
