package ru.vladislavsumin.vframe.socket.client;

import java.util.Map;

/**
 * Interface from server protocol
 *
 * @author Sumin Vladislav
 * @version 1.0
 */
@SuppressWarnings("WeakerAccess")
public interface ClientProtocolInterface {
    String getName();

    void exec(Map<String, Object> map, ClientSocketWorker worker);
}
