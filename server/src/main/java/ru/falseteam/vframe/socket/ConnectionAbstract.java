package ru.falseteam.vframe.socket;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.falseteam.vframe.VFrameRuntimeException;
import ru.falseteam.vframe.subscriptions.SubscriptionManager;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 * Base abstract class to server connection.
 *
 * @author Sumin Vladislav
 * @version 3.4
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public abstract class ConnectionAbstract {
    private static final Logger log = LogManager.getLogger();

    private static final Map<String, ProtocolAbstract> defaultProtocols = new HashMap<>();

    private final Socket socket;
    private final SocketWorker worker;

    private ObjectOutputStream out;

    private boolean connected = true;
    long lastPing;

    static {
        addDefaultProtocol(new Ping());
    }

    private static void addDefaultProtocol(ProtocolAbstract protocol) {
        defaultProtocols.put(protocol.getName(), protocol);
    }

    public ConnectionAbstract(final Socket socket, final SocketWorker worker) {
        this.socket = socket;
        this.worker = worker;
        lastPing = System.currentTimeMillis();

        final ConnectionAbstract link = this;
        new Thread("Connection with " + socket.getInetAddress().getHostAddress()) {
            @Override
            public void run() {
                while (connected) {
                    try {
                        ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                        out = new ObjectOutputStream(socket.getOutputStream());
                        log.trace("Client {} connected", socket.getInetAddress().getHostAddress());
                        worker.addToClientsList(link);
                        onConnect();
                        //noinspection InfiniteLoopStatement
                        while (true) {
                            Container container = (Container) in.readObject();
                            ProtocolAbstract protocol = defaultProtocols.get(container.protocol);
                            if (protocol == null) protocol = getProtocols().get(container.protocol);
                            if (protocol == null) protocol = getDefaultProtocol(container);
                            if (protocol == null) {
                                log.error("VFrame: client used unknown protocol {}", container.protocol);
                                continue;
                            }
                            protocol.exec(container.data, link);
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

    protected void onConnect() {

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
            worker.removeFromClientsList(this);
            SubscriptionManager.removeSubscriber(this);
            onDisconnect();
            try {
                socket.close();
            } catch (IOException e) {
                log.fatal("Cannot close port");
                throw new VFrameRuntimeException(e);
            }
            if (reason != null)
                log.trace("Client {} disconnected, reason: {}", socket.getInetAddress().getHostAddress(), reason);
            else
                log.trace("Client {} disconnected", socket.getInetAddress().getHostAddress());
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

    /**
     * @return Map with all user protocols.
     */
    protected abstract Map<String, ProtocolAbstract> getProtocols();


    /**
     * @param container - container with unknown protocol
     * @return - default protocol or null if protocol is not supported
     */
    protected ProtocolAbstract getDefaultProtocol(Container container) {
        return null;
    }
}
