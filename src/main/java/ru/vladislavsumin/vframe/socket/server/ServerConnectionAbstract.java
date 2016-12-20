package ru.vladislavsumin.vframe.socket.server;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.vladislavsumin.vframe.VFrameRuntimeException;
import ru.vladislavsumin.vframe.serializable.Container;

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
 * @version 1.2
 */
@SuppressWarnings("unused")
public abstract class ServerConnectionAbstract implements ServerConnectionInterface {
    private static final Logger log = LogManager.getLogger();

    private static final Map<String, ServerProtocolInterface> defaultProtocols = new HashMap<>();

    private final Socket socket;
    private final ServerSocketWorker worker;

    private ObjectOutputStream out;

    private boolean connected = true;

    static {
        addDefaultProtocol(new Ping());
    }

    private static void addDefaultProtocol(ServerProtocolInterface protocol) {
        defaultProtocols.put(protocol.getName(), protocol);
    }

    public ServerConnectionAbstract(final Socket socket, final ServerSocketWorker worker) {
        this.socket = socket;
        this.worker = worker;

        final ServerConnectionAbstract link = this;
        new Thread("Connection with " + socket.getInetAddress().getHostAddress()) {
            @Override
            public void run() {
                while (connected) {
                    try {
                        ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                        out = new ObjectOutputStream(socket.getOutputStream());
                        log.trace("Client {} connected", socket.getInetAddress().getHostAddress());
                        onConnect();
                        //noinspection InfiniteLoopStatement
                        while (true) {
                            Container container = (Container) in.readObject();
                            ServerProtocolInterface protocol = defaultProtocols.get(container.protocol);
                            //TODO add user protocols
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

    private void onConnect() {
        worker.addToClientsList(this);
    }

    @Override
    public void disconnect() {
        disconnect(null);
    }

    @Override
    public void disconnect(String reason) {
        synchronized (socket) {
            if (!connected) return;
            connected = false;
            worker.removeFromClientsList(this);
            try {
                socket.close();
            } catch (IOException e) {
                log.fatal("Cannot close port");
                throw new VFrameRuntimeException(e);
            }
            if (reason != null)
                log.trace("Client {} disconnected, reason: {}", socket.getInetAddress().getAddress(), reason);
        }
    }

    @Override
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
}