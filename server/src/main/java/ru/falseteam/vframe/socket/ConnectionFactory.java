package ru.falseteam.vframe.socket;


import java.net.Socket;

public interface ConnectionFactory {
    ConnectionAbstract createNewConnection(Socket socket, SocketWorker parent);
}
