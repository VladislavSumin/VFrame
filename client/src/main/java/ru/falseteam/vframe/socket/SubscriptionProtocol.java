package ru.falseteam.vframe.socket;

import java.util.Map;

import ru.falseteam.vframe.socket.ProtocolAbstract;
import ru.falseteam.vframe.socket.SocketWorker;
import ru.falseteam.vframe.socket.Container;

public class SubscriptionProtocol extends ProtocolAbstract {
    @Override
    public void exec(Map<String, Object> map, SocketWorker worker) {
        //TODO this
        //TODO Удаление инфы после отписки от нее.
    }

    public static Container subscribe(String eventName) {
        Container container = new Container(SubscriptionProtocol.class.getSimpleName(), true);
        container.data.put("requestType", "subscribe");
        container.data.put("eventName", eventName);
        return container;
    }

    public static Container unsubscribe(String eventName) {
        Container container = new Container(SubscriptionProtocol.class.getSimpleName(), true);
        container.data.put("requestType", "unsubscribe");
        container.data.put("eventName", eventName);
        return container;
    }
}
