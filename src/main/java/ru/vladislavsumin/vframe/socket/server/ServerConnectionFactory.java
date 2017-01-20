package ru.vladislavsumin.vframe.socket.server;


import java.net.Socket;

public interface ServerConnectionFactory {
    ServerConnectionAbstract createNewConnection(Socket socket, ServerSocketWorker parent);
}
