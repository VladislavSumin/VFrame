package ru.falseteam.vframe.socket;


import ru.falseteam.vframe.VFrameRuntimeException;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Base abstract class to server connection.
 *
 * @author Sumin Vladislav
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public abstract class ConnectionAbstract<T extends Enum<T>> {
    private static final Logger log = Logger.getLogger(ConnectionAbstract.class.getName());

    // Container to internal VFrame protocols
    private static final Map<String, ProtocolAbstract> defaultProtocols = new HashMap<>();

    private final Socket socket;
    private final SocketWorker<T> worker;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    private T permission;

    private boolean connected = true;
    long lastPing;

    static {
        // Add internal VFrame protocols
        addDefaultProtocol(new Ping());
        addDefaultProtocol(new SubscriptionManager.SubscriptionProtocol());
    }

    private static void addDefaultProtocol(ProtocolAbstract protocol) {
        defaultProtocols.put(protocol.getName(), protocol);
    }

    public ConnectionAbstract(final Socket socket, final SocketWorker<T> worker) {
        this.socket = socket;
        this.worker = worker;

        lastPing = System.currentTimeMillis();
        permission = worker.getPermissionManager().getDefaultPermission();

        final ConnectionAbstract link = this; //TODO пофиксить это
        new Thread("Connection with " + socket.getInetAddress().getHostAddress()) {
            @Override
            public void run() {
                while (connected) {
                    try {
                        //Create input && output streams
                        in = new ObjectInputStream(socket.getInputStream());
                        out = new ObjectOutputStream(socket.getOutputStream());

                        log.info(String.format("Client %s connected", socket.getInetAddress().getHostAddress()));
                        worker.addToClientsList(link);
                        onConnect();

                        while (connected) {
                            Container container = (Container) in.readObject();
                            onDataReceive(container);
                        }

                    } catch (ClassNotFoundException e) {
                        disconnect(e.getMessage());
                    } catch (IOException ignore) {
                        disconnect(null);
                    }
                }
            }
        }.start();
    }

    private void onDataReceive(Container container) {
        // Try to find on defaultProtocols
        ProtocolAbstract protocol = defaultProtocols.get(container.protocol);
        if (protocol == null)
            // Try to find on other protocol
            protocol = worker.getPermissionManager().getProtocols(permission).get(container.protocol);
        if (protocol == null) {
            PermissionManager.DefaultProtocol defaultProtocol =
                    worker.getPermissionManager().getDefaultProtocol();
            if (defaultProtocol == null)
                log.log(Level.WARNING,
                        String.format("VFrame: client used unknown protocol %s", container.protocol));
            else
                defaultProtocol.exec(container, this);
        } else
            protocol.exec(container.data, this);
    }

    protected void onConnect() { //TODO Подумать над этим

    }

    protected void onDisconnect() {

    }

    public void disconnect() {
        disconnect(null);
    }

    public void disconnect(String reason) {
        synchronized (socket) {
            if (!connected) return;
            connected = false;
            //TODO тут тоже посмотреть
            worker.removeFromClientsList(this);
            worker.getSubscriptionManager().removeSubscriber(this);
            onDisconnect();
            try {
                socket.close();
            } catch (IOException e) {
                throw new VFrameRuntimeException("VFrame: ConnectionAbstract: Cannot close port", e);
            }
            //TODO тест костыля
            try {
                in.close();
            } catch (IOException ignore) {
            }
            if (reason != null)
                log.info(String.format("Client %s disconnected, reason: %s",
                        socket.getInetAddress().getHostAddress(), reason));
            else
                log.info(String.format("Client %s disconnected", socket.getInetAddress().getHostAddress()));
        }
    }

    public void send(Container container) {
        synchronized (socket) {
            if (!connected || out == null) return;
            try {
                out.reset();
                out.writeObject(container);
                out.flush();
            } catch (IOException ignore) {
                disconnect(null);
            }
        }
    }

    public SocketWorker<T> getWorker() {
        return worker;
    }

    public T getPermission() {
        return permission;
    }

    public void setPermission(T permission) {
        this.permission = permission;
        Container c = new Container("SyncPermissionProtocol", true);
        c.data.put("permission", permission);
        send(c);
    }
}
