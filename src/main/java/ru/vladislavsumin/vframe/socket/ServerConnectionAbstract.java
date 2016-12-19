package ru.vladislavsumin.vframe.socket;


import ru.vladislavsumin.vframe.serializable.Container;

import java.net.Socket;

/**
 * Base abstract class to server connection.
 *
 * @author Sumin Vladislav
 * @version 1.0
 */
@SuppressWarnings("unused")
public abstract class ServerConnectionAbstract implements ServerConnectionInterface {

    public ServerConnectionAbstract(Socket socket, ServerSocketWorker worker) {

    }

    //TODO THIS
    @Override
    public void disconnect() {

    }

    @Override
    public void disconnect(String reason) {

    }

    @Override
    public void sand(Container container) {

    }
}
