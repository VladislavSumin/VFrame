package ru.falseteam.vframe.socket;


import java.util.Map;

/**
 * Internal ping protocol
 *
 * @author Sumin Vladislav
 * @version 1.2
 */
@SuppressWarnings("WeakerAccess")
class Ping extends ServerProtocolAbstract {

    @Override
    public void exec(Map<String, Object> data, ServerConnectionAbstract connection) {
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
