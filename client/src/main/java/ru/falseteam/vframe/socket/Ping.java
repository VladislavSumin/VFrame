package ru.falseteam.vframe.socket;


import java.util.Map;

/**
 * Internal ping protocol
 *
 * @author Sumin Vladislav
 * @version 1.0
 */
public class Ping extends ClientProtocolAbstract {
    @Override
    public void exec(Map<String, Object> map, ClientSocketWorker worker) {
        worker.lastPing = System.currentTimeMillis();
        Container c = new Container("ping");
        worker.send(c);
    }

    @Override
    public String getName() {
        return "ping";
    }
}
