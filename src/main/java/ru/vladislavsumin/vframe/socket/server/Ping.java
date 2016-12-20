package ru.vladislavsumin.vframe.socket.server;

import java.util.Map;

/**
 * Internal ping protocol
 *
 * @author Sumin Vladislav
 * @version 0.1
 */
class Ping extends ServerProtocolAbstract {

    @Override
    public void exec(Map<String, Object> data, ServerConnectionInterface connection) {
        //TODO ping check
    }

    @Override
    public String getName() {
        return "ping";
    }
}
