package ru.falseteam.vframe.socket;


import java.util.Map;

/**
 * Internal ping protocol
 *
 * @author Sumin Vladislav
 * @version 1.0
 */
public class Ping extends ProtocolAbstract { //TODO почему класс бубличный?
    @Override
    public void exec(Map<String, Object> map, SocketWorker worker) {
        worker.lastPing = System.currentTimeMillis();
        Container c = new Container("ping");
        worker.send(c);
    }

    @Override
    public String getName() {
        return "ping";
    }
}
