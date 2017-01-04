package ru.vladislavsumin.vframe.socket.client;

import java.util.Map;

/**
 * Realize base client protocol methods
 *
 * @author Sumin Vladislav
 * @version 2.1
 */
@SuppressWarnings("WeakerAccess")
public abstract class ClientProtocolAbstract {

    public String getName() {
        return getClass().getSimpleName();
    }

    abstract public void exec(Map<String, Object> map, ClientSocketWorker worker);
}
