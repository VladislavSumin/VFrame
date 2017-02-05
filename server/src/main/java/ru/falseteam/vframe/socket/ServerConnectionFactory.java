package ru.falseteam.vframe.socket;


import java.net.Socket;

public interface ServerConnectionFactory {
    ServerConnectionAbstract createNewConnection(Socket socket, ServerSocketWorker parent);
}
