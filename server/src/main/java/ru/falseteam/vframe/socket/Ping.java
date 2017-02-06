package ru.falseteam.vframe.socket;


import java.util.Map;

/**
 * Internal ping protocol
 *
 * @author Sumin Vladislav
 * @version 1.2
 */
@SuppressWarnings("WeakerAccess")
class Ping extends ProtocolAbstract {

    @Override
    public void exec(Map<String, Object> data, ConnectionAbstract connection) {
        ((ConnectionAbstract) connection).lastPing = System.currentTimeMillis();
    }

    @Override
    public String getName() {
        return "ping";
    }

    public static Container getRequest() {
        return new Container("ping");
    }
}
