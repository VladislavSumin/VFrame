package ru.vladislavsumin.vframe.socket;

import ru.vladislavsumin.vframe.serializable.Container;

/**
 * Interface for each server connection created with {@link ServerSocketWorker}
 */
public interface ServerConnectionInterface {
    void disconnect();

    void disconnect(String reason);

    void sand(Container container);
}
