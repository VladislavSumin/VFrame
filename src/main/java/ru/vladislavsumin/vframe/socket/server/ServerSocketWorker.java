package ru.vladislavsumin.vframe.socket.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.vladislavsumin.vframe.VFrameRuntimeException;
import ru.vladislavsumin.vframe.serializable.Container;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Listen socket and create connection class.
 *
 * @author Sumin Vladislav
 * @version 2.0
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class ServerSocketWorker {
    private static final Logger log = LogManager.getLogger();

    private final SSLServerSocket socket;
    private final List<ServerConnectionInterface> connections = new LinkedList<>();

    /**
     * @param connection connection class,
     *                   must have constructor with parameters {@link java.net.Socket}, {@link ServerSocketWorker}
     * @param socket     - server socket to listen
     */
    public ServerSocketWorker(final Class<? extends ServerConnectionInterface> connection,
                              final SSLServerSocket socket) {
        this.socket = socket;

        //Initialize constructor
        final Constructor<? extends ServerConnectionInterface> constructor;
        try {
            constructor = connection.getDeclaredConstructor(Socket.class, ServerSocketWorker.class);
        } catch (NoSuchMethodException e) {
            log.fatal("VFrame: Class {} have not needed constructor", connection.getName());
            throw new VFrameRuntimeException(e);
        }

        final ServerSocketWorker link = this;

        //Run listen thread
        new Thread("ServerSocketWorker port " + socket.getLocalPort()) {
            @Override
            public void run() {
                log.trace("ServerSocketWorker with port {} init", socket.getLocalPort());
                while (true) {
                    try {
                        constructor.newInstance(socket.accept(), link);
                    } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                        log.fatal("VFrame Internal error", e);
                        throw new VFrameRuntimeException(e);
                    } catch (IOException e) {
                        // this run when serverSocket closing.
                        return;
                    }
                }
            }
        }.start();
    }

    /**
     * Stop listen and close port.
     * Close all connection.
     */
    public void stop() {
        //Close socket
        try {
            socket.close();
        } catch (IOException e) {
            log.fatal("Can not close port", e);
            throw new VFrameRuntimeException(e);
        }

        //Close all connection
        synchronized (connections) {
            Iterator<ServerConnectionInterface> iterator = connections.iterator();
            while (iterator.hasNext()) {
                ServerConnectionInterface connection = iterator.next();
                iterator.remove();
                connection.disconnect("server stop");
            }
        }
    }

    public void sandToAll(Container container) {
        synchronized (connections) {
            for (ServerConnectionInterface connection : connections) {
                connection.send(container);
            }
        }
    }

    public void addToClientsList(ServerConnectionInterface connection) {
        synchronized (connections) {
            connections.add(connection);
        }
    }

    public void removeFromClientsList(ServerConnectionInterface connection) {
        synchronized (connections) {
            connections.remove(connection);
        }
    }
}
