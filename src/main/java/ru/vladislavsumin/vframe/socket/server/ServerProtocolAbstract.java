package ru.vladislavsumin.vframe.socket.server;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Realize base server protocol methods
 *
 * @author Sumin Vladislav
 * @version 2.0
 */
@SuppressWarnings("WeakerAccess")
public abstract class ServerProtocolAbstract{

    public String getName() {
        return getClass().getSimpleName();
    }

    abstract void exec(Map<String, Object> data, ServerConnectionAbstract connection);

}
