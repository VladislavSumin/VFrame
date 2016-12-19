package ru.vladislavsumin.vframe.socket.server;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.vladislavsumin.vframe.VFrameRuntimeException;
import ru.vladislavsumin.vframe.serializable.Container;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Base abstract class to server connection.
 *
 * @author Sumin Vladislav
 * @version 1.0
 */
@SuppressWarnings("unused")
public abstract class ServerConnectionAbstract implements ServerConnectionInterface {
    private static final Logger log = LogManager.getLogger();

    private final Socket socket;
    private final ServerSocketWorker worker;

    private ObjectOutputStream out;

    private boolean connected = true;

    private final Runnable run = new Runnable() {
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
                        //TODO protocols worker
                    }

                } catch (ClassNotFoundException e) {
                    disconnect(e.getMessage());
                } catch (IOException ignore) {
                    disconnect(null);
                }
            }
        }
    };

    public ServerConnectionAbstract(Socket socket, ServerSocketWorker worker) {
        this.socket = socket;
        this.worker = worker;


        new Thread(run, "Connection with " + socket.getInetAddress().getHostAddress()).start();
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
