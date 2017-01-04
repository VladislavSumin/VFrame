package ru.vladislavsumin.vframetest;

import ru.vladislavsumin.vframe.socket.server.ServerConnectionAbstract;
import ru.vladislavsumin.vframe.socket.server.ServerProtocolAbstract;
import ru.vladislavsumin.vframe.socket.server.ServerSocketWorker;

import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class Test extends ServerConnectionAbstract {
    public Test(Socket socket, ServerSocketWorker worker) {
        super(socket, worker);
    }

    @Override
    protected Map<String, ServerProtocolAbstract> getProtocols() {
        return new HashMap<>();
    }
}
