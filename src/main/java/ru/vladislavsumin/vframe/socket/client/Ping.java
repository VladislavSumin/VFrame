package ru.vladislavsumin.vframe.socket.client;

import java.util.Map;

/**
 * Internal ping protocol
 *
 * @author Sumin Vladislav
 * @version 0.1
 */
public class Ping extends ClientProtocolAbstract {
    @Override
    public void exec(Map<String, Object> map, ClientSocketWorker worker) {
        //TODO ping realization
    }

    @Override
    public String getName() {
        return "ping";
    }
}
