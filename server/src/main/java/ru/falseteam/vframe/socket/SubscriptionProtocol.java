package ru.falseteam.vframe.socket;


import java.util.Map;

class SubscriptionProtocol extends ProtocolAbstract {
    @SuppressWarnings("unchecked")
    @Override
    public void exec(Map<String, Object> data, ConnectionAbstract connection) {
        if (data == null) return;
        String requestType = data.get("requestType").toString();
        String eventName = data.get("eventName").toString();
        if (requestType == null || eventName == null) return;

        Container container = new Container(getName(), true);
        container.data.put("eventName", eventName);
        switch (requestType) {
            case "subscribe":
                container.data.put("subscription",
                        connection.getWorker().getSubscriptionManager().addSubscription(eventName, connection));
                connection.send(container);
                return;
            case "unsubscribe":
                container.data.put("subscription",
                        connection.getWorker().getSubscriptionManager().removeSubscription(eventName, connection));
                connection.send(container);
        }
    }
}
