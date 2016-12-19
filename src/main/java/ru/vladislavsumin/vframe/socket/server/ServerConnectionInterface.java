package ru.vladislavsumin.vframe.socket.server;

import ru.vladislavsumin.vframe.serializable.Container;
import ru.vladislavsumin.vframe.socket.server.ServerSocketWorker;

/**
 * Interface for each server connection created with {@link ServerSocketWorker}
 *
 * @author Sumin Vladislav
 * @version 1.1
 */
@SuppressWarnings("unused")
public interface ServerConnectionInterface {
    void disconnect();

    void disconnect(String reason);

    void send(Container container);
}
