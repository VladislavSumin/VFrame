package ru.falseteam.vframe.socket;

import java.util.Map;

/**
 * Realize base client protocol methods
 *
 * @author Sumin Vladislav
 * @version 2.1
 */
@SuppressWarnings("WeakerAccess")
public abstract class ProtocolAbstract {

    public String getName() {
        return getClass().getSimpleName();
    }

    abstract public void exec(Map<String, Object> map, SocketWorker worker);
}
