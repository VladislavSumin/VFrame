package ru.falseteam.vframe.socket;

import ru.falseteam.vframe.VFrame;
import ru.falseteam.vframe.VFrameRuntimeException;

import javax.net.ssl.SSLServerSocket;
import java.io.IOException;
import java.net.Socket;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.TimerTask;
import java.util.logging.Logger;

/**
 * Listen socket and create connection class.
 *
 * @author Sumin Vladislav
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class SocketWorker<T extends Enum<T>> {

    private static final Logger log = Logger.getLogger(SocketWorker.class.getName());

    // interface to create new connection
    public interface ConnectionFactory {
        ConnectionAbstract createNewConnection(Socket socket, SocketWorker parent);
    }

    private final SSLServerSocket socket;
    private final ConnectionFactory connectionFactory;

    private final List<ConnectionAbstract> connections = new LinkedList<>();

    private final PermissionManager<T> permissionManager;
    private final SubscriptionManager<T> subscriptionManager;

    private final TimerTask pingTask = new TimerTask() {
        @Override
        public void run() {
            synchronized (connections) {
                Iterator<ConnectionAbstract> iterator = connections.iterator();
                while (iterator.hasNext()) {
                    ConnectionAbstract connection = iterator.next();
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

    public SocketWorker(
            final ConnectionFactory connectionFactory,
            final VFKeystore keystore,
            int port,
            PermissionManager<T> permissionManager) {

        this.connectionFactory = connectionFactory;
        this.permissionManager = permissionManager;
        this.subscriptionManager = new SubscriptionManager<>();

        //Create server socket
        try {
            this.socket = (SSLServerSocket) keystore.getSSLContext().getServerSocketFactory().createServerSocket(port);
        } catch (IOException e) {
            throw new VFrameRuntimeException(String.format("VFrame can not open port %d", port), e);
        }


    }

    /**
     * Start listen port
     */
    public void start() {
        final SocketWorker link = this;
        //Run listen thread
        new Thread("VFrame: SocketWorker port " + socket.getLocalPort()) {
            @Override
            public void run() {
                log.info("VFrame: SocketWorker: Port " + socket.getLocalPort() + " open and listening");
                while (true) {
                    try {
                        connectionFactory.createNewConnection(socket.accept(), link);
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
        // Cancel ping task
        pingTask.cancel();

        //Close socket
        try {
            socket.close();
        } catch (IOException e) {
            throw new VFrameRuntimeException("VFrame: SocketWorker: Can not close port", e);
        }
        log.info("Port " + socket.getLocalPort() + " closed");

        //Close all connection
        // TODO подумать над тем как лучше закрывать соединения
        synchronized (connections) {
            Iterator<ConnectionAbstract> iterator = connections.iterator();
            while (iterator.hasNext()) {
                ConnectionAbstract connection = iterator.next();
                iterator.remove();
                connection.disconnect("server stop");
            }
        }
    }

    public void sandToAll(Container container) {
        synchronized (connections) {
            for (ConnectionAbstract connection : connections) {
                connection.send(container);
            }
        }
    }

    void addToClientsList(ConnectionAbstract connection) {
        synchronized (connections) {
            connections.add(connection);
        }
    }

    void removeFromClientsList(ConnectionAbstract connection) {
        synchronized (connections) {
            connections.remove(connection);
        }
    }

    public List<ConnectionAbstract> getConnections() {
        return connections;
    }

    public PermissionManager<T> getPermissionManager() {
        return permissionManager;
    }

    public SubscriptionManager<T> getSubscriptionManager() {
        return subscriptionManager;
    }
}
