package ru.vladislavsumin.vframe.socket.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.vladislavsumin.vframe.VFrame;
import ru.vladislavsumin.vframe.VFrameRuntimeException;
import ru.vladislavsumin.vframe.serializable.Container;
import ru.vladislavsumin.vframe.socket.VFKeystore;

import javax.net.ssl.SSLServerSocket;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.TimerTask;

/**
 * Listen socket and create connection class.
 *
 * @author Sumin Vladislav
 * @version 4.0
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class ServerSocketWorker {
    private static final Logger log = LogManager.getLogger();

    private final SSLServerSocket socket;
    private final List<ServerConnectionAbstract> connections = new LinkedList<>();

    private final TimerTask pingTask = new TimerTask() {
        @Override
        public void run() {
            synchronized (connections) {
                Iterator<ServerConnectionAbstract> iterator = connections.iterator();
                while (iterator.hasNext()) {
                    ServerConnectionAbstract connection = iterator.next();
                    if (connection.lastPing + 24000 > System.currentTimeMillis()) {
                        connection.send(Ping.getRequest());
                    } else {
                        iterator.remove();
                        connection.disconnect("timeout");
                    }
                }
            }
        }
    };

    /**
     * @param connection connection class,
     *                   must have constructor with parameters {@link java.net.Socket}, {@link ServerSocketWorker}
     */
    public ServerSocketWorker(final Class<? extends ServerConnectionAbstract> connection,
                              final VFKeystore keystore, int port) {
        try {
            this.socket = (SSLServerSocket) keystore.getSSLContext().getServerSocketFactory().createServerSocket(port);
        } catch (IOException e) {
            log.fatal("VFrame can not open port {}", port);
            throw new VFrameRuntimeException(e);
        }

        //Initialize constructor
        final Constructor<? extends ServerConnectionAbstract> constructor;
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

        VFrame.addPeriodicalTimerTask(pingTask, 16000);
    }

    /**
     * Stop listen and close port.
     * Close all connection.
     */
    public void stop() {
        pingTask.cancel();

        //Close socket
        try {
            socket.close();
        } catch (IOException e) {
            log.fatal("Can not close port", e);
            throw new VFrameRuntimeException(e);
        }

        //Close all connection
        synchronized (connections) {
            Iterator<ServerConnectionAbstract> iterator = connections.iterator();
            while (iterator.hasNext()) {
                ServerConnectionAbstract connection = iterator.next();
                iterator.remove();
                connection.disconnect("server stop");
            }
        }
    }

    public void sandToAll(Container container) {
        synchronized (connections) {
            for (ServerConnectionAbstract connection : connections) {
                connection.send(container);
            }
        }
    }

    public void addToClientsList(ServerConnectionAbstract connection) {
        synchronized (connections) {
            connections.add(connection);
        }
    }

    public void removeFromClientsList(ServerConnectionAbstract connection) {
        synchronized (connections) {
            connections.remove(connection);
        }
    }
}
