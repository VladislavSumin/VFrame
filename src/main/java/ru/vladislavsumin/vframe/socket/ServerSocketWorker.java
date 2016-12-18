package ru.vladislavsumin.vframe.socket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.vladislavsumin.vframe.VFrameRuntimeException;
import ru.vladislavsumin.vframe.serializable.Container;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Listen socket and create connection class.
 *
 * @param <T> connection class
 * @author Sumin Vladislav
 * @version 1.0
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class ServerSocketWorker<T extends ServerConnectionInterface> {
    private static final Logger log = LogManager.getLogger();

    private final ServerSocket socket;
    private final Constructor<T> constructor;
    private final List<ServerConnectionInterface> connections = new LinkedList<>();

    /**
     * @param connection connection class,
     *                   must have constructor with parameters {@link java.net.Socket}, {@link ServerSocketWorker}
     * @param socket     - server socket to listen
     */
    public ServerSocketWorker(Class<T> connection, final ServerSocket socket) {
        this.socket = socket;

        //Initialize constructor.
        try {
            constructor = connection.getConstructor(Socket.class, this.getClass());
        } catch (NoSuchMethodException e) {
            log.fatal("VFrame: Class {} have not needed constructor", connection.getName());
            throw new VFrameRuntimeException(e);
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                log.trace("ServerSocketWorker with port {} init", socket.getLocalPort());
                while (true) {
                    try {
                        constructor.newInstance(socket.accept(), this);
                    } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                        log.fatal("VFrame Internal error", e);
                        throw new VFrameRuntimeException(e);
                    } catch (IOException e) {
                        // this run when serverSocket closing.
                        return;
                    }
                }
            }
        }, "ServerSocketWorker port " + socket.getLocalPort()).start();
    }

    /**
     * Stop listen and close port.
     * Close all connection.
     */
    public void stop() {
        try {
            socket.close();
        } catch (IOException e) {
            log.fatal("Can not close port", e);
            throw new VFrameRuntimeException(e);
        }

        //close all connection.
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
                connection.sand(container);
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
