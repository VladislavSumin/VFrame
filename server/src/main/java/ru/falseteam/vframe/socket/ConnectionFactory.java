package ru.falseteam.vframe.socket;


import java.net.Socket;

/**
 * Factory to create connections
 *
 * @author Sumin Vladislav
 * @version 1.0
 */
@SuppressWarnings("WeakerAccess")
public interface ConnectionFactory {
    ConnectionAbstract createNewConnection(Socket socket, SocketWorker parent);
}
