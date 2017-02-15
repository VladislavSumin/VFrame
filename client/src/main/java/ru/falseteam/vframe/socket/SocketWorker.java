package ru.falseteam.vframe.socket;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.falseteam.vframe.VFrame;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.*;

/**
 * Base client socket worker class
 *
 * @author Sumin Vladislav
 * @version 4.2
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class SocketWorker<T extends Enum<T>> {
    private static final Logger log = LogManager.getLogger();

    private final Object lock = new Object();

    private final SocketAddress socketAddress;

    private final Map<String, ProtocolAbstract> protocols = new HashMap<>();
    private final Set<OnConnectionChangeStateListener> listeners = new HashSet<>();

    private final SubscriptionManager subscriptionManager;

    private SSLSocketFactory ssf;
    private SSLSocket socket;
    private ObjectOutputStream out;

    private boolean work = false;
    private boolean connected = false;
    long lastPing = System.currentTimeMillis();

    public interface OnConnectionChangeStateListener {
        void onConnectionChangeState(boolean connected);
    }

    private final SocketWorker link = this;
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
                        ProtocolAbstract protocol = protocols.get(container.protocol);
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

    public SocketWorker(String ip, int port, VFKeystore keystore,
                        Class<T> permissionEnum, T disconnectedPermission, T defaultPermission) {
        //TODO permission.
        this.subscriptionManager = new SubscriptionManager(this);
        socketAddress = new InetSocketAddress(ip, port);
        ssf = keystore.getSSLContext().getSocketFactory();
        addProtocol(new Ping());
        addProtocol(new SubscriptionSyncProtocol());
        addProtocol(new SubscriptionProtocol());
    }

    public void start() {
        synchronized (lock) {
            if (work) {
                log.error("VFrame: SocketWorker already worked");
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
                log.error("VFrame: SocketWorker already stopped");
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

    public void addProtocol(ProtocolAbstract protocol) {
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
        if (connected) {
            subscriptionManager.resubscribeAll();
        }
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

    public SubscriptionManager getSubscriptionManager() {
        return subscriptionManager;
    }
}
