package ru.vladislavsumin.vframe.socket.server;

import java.util.Map;

/**
 * Interface from server protocol
 *
 * @author Sumin Vladislav
 * @version 1.1
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public interface ServerProtocolInterface {
    String getName();

    void exec(Map<String, Object> data, ServerConnectionAbstract connection);
}
