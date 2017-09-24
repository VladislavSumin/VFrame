package ru.falseteam.vframe.socket;


import ru.falseteam.vframe.VFrame;
import ru.falseteam.vframe.VFrameRuntimeException;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Base client socket worker class
 *
 * @author Sumin Vladislav
 * @version 4.3
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class SocketWorker<T extends Enum<T>> {

    private static final Logger log = Logger.getLogger(SocketWorker.class.getName());

    private final Object lock = new Object();

    private final SocketAddress socketAddress;

    private final Map<String, ProtocolAbstract> protocols = new HashMap<>();
    private final Set<OnChangePermissionListener<T>> listeners = new HashSet<>();

    private final SubscriptionManager subscriptionManager;

    private SSLSocketFactory ssf;
    private SSLSocket socket;
    private ObjectOutputStream out;

    private boolean work = false;
    private boolean connected = false;
    long lastPing = System.currentTimeMillis();

    private final Class<T> permissionEnum;
    private final T defaultPermission;
    private final T disconnectedPermission;
    private T currentPermission;

    public interface OnChangePermissionListener<T extends Enum<T>> {
        void onChangePermission(T permission);
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
                            log.log(Level.WARNING,
                                    String.format("VFrame: Server used unknown protocol %s", container.protocol));
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
        this.permissionEnum = permissionEnum;
        this.defaultPermission = defaultPermission;
        this.disconnectedPermission = disconnectedPermission;

        currentPermission = disconnectedPermission;
        this.subscriptionManager = new SubscriptionManager(this);
        socketAddress = new InetSocketAddress(ip, port);
        ssf = keystore.getSSLContext().getSocketFactory();

        addProtocol(new Ping());
        addProtocol(new SyncPermissionProtocol());
    }

    public void start() {
        synchronized (lock) {
            if (work)
                throw new VFrameRuntimeException("VFrame: SocketWorker already worked");
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
            if (!work)
                throw new VFrameRuntimeException("VFrame: SocketWorker already stopped");
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
            setCurrentPermission(defaultPermission);
            subscriptionManager.resubscribeAll();
        } else {
            setCurrentPermission(disconnectedPermission);
        }
    }

    /**
     * Добавляет слушателя на изменение текущих привелегий.
     *
     * @param listener  - слушатель
     * @param invokeNow - если true то слушатель сработает сразу после добавления
     */
    public void addOnPermissionChangeStateListener(OnChangePermissionListener<T> listener, boolean invokeNow) {
        synchronized (listeners) {
            listeners.add(listener);
            if (invokeNow) listener.onChangePermission(currentPermission);
        }
    }

    /**
     * Удаляет слушаетля на изменение текущих привелегий.
     *
     * @param listener - слушатель
     */
    public void removeOnPermissionChangeStateListener(OnChangePermissionListener<T> listener) {
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

    void setCurrentPermission(String currentPermission) {
        setCurrentPermission(Enum.valueOf(permissionEnum, currentPermission));
    }

    void setCurrentPermission(T permission) {
        this.currentPermission = permission;
        synchronized (listeners) {
            for (OnChangePermissionListener<T> listener : listeners) {
                listener.onChangePermission(this.currentPermission);
            }
        }
    }

    public T getCurrentPermission() {
        return currentPermission;
    }
}
