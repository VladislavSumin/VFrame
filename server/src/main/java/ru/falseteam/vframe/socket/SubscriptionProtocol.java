package ru.falseteam.vframe.socket;


import java.util.Map;

public class SubscriptionProtocol extends ProtocolAbstract {
    @Override
    public void exec(Map<String, Object> data, ConnectionAbstract connection) {
        if (data == null) return;
        String requestType = data.get("requestType").toString();
        String eventName = data.get("eventName").toString();
        if (requestType == null || eventName == null) return;
        Container container = new Container(getName(), true);

        if (requestType.equals("subscribe")) {
            container.data.put("subscription",
                    //TODO добавить в протоколы шаблоны.
                    connection.getWorker().getSubscriptionManager().addSubscription(eventName, connection));
            connection.send(container);
            return;
        }

        if (requestType.equals("unsubscribe")) {
            container.data.put("subscription",
                    connection.getWorker().getSubscriptionManager().removeSubscription(eventName, connection));
            connection.send(container);
            //return;
        }
    }
}
