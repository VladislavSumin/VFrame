package ru.vladislavsumin.vframetest;

import ru.vladislavsumin.vframe.socket.server.ServerConnectionAbstract;
import ru.vladislavsumin.vframe.socket.server.ServerSocketWorker;

import java.net.Socket;

public class Test extends ServerConnectionAbstract {
    public Test(Socket socket, ServerSocketWorker worker) {
        super(socket, worker);
    }
}
