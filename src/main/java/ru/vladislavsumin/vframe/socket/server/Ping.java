package ru.vladislavsumin.vframe.socket.server;

import ru.vladislavsumin.vframe.serializable.Container;

import java.util.Map;

/**
 * Internal ping protocol
 *
 * @author Sumin Vladislav
 * @version 1.0
 */
class Ping extends ServerProtocolAbstract {

    @Override
    public void exec(Map<String, Object> data, ServerConnectionInterface connection) {
        ((ServerConnectionAbstract) connection).lastPing = System.currentTimeMillis();
    }

    @Override
    public String getName() {
        return "ping";
    }

    public static Container getRequest() {
        return new Container("ping");
    }
}
