package ru.falseteam.vframe.socket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.falseteam.vframe.VFrame;
import ru.falseteam.vframe.VFrameRuntimeException;

import javax.net.ssl.SSLServerSocket;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.TimerTask;

/**
 * Listen socket and create connection class.
 *
 * @author Sumin Vladislav
 * @version 4.2
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


    public ServerSocketWorker(final ServerConnectionFactory connectionFactory, final VFKeystore keystore, int port) {
        try {
            this.socket = (SSLServerSocket) keystore.getSSLContext().getServerSocketFactory().createServerSocket(port);
        } catch (IOException e) {
            log.fatal("VFrame can not open port {}", port);
            throw new VFrameRuntimeException(e);
        }

        final ServerSocketWorker link = this;

        //Run listen thread
        new Thread("ServerSocketWorker port " + socket.getLocalPort()) {
            @Override
            public void run() {
                VFrame.print("VFrame: port " + socket.getLocalPort() + " open and listening");
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
        pingTask.cancel();

        //Close socket
        try {
            socket.close();
        } catch (IOException e) {
            log.fatal("VFrame: Can not close port");
            throw new VFrameRuntimeException(e);
        }
        VFrame.print("Port " + socket.getLocalPort() + " closed");

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
