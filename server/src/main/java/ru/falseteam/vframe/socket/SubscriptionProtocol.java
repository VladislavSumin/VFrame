package ru.falseteam.vframe.socket;


import java.util.Map;

public class SubscriptionProtocol extends ProtocolAbstract {
    public interface AccessRule {
        boolean access(ConnectionAbstract connection, String eventName);
    }

    private final AccessRule rule;

    public SubscriptionProtocol() {
        this.rule = null;
    }

    public SubscriptionProtocol(AccessRule rule) {
        this.rule = rule;
    }

    @Override
    public void exec(Map<String, Object> data, ConnectionAbstract connection) {
        if (data == null) return;
        String requestType = data.get("requestType").toString();
        String eventName = data.get("eventName").toString();
        if (requestType == null || eventName == null) return;
        Container container = new Container(getName(), true);

        if (rule != null && !rule.access(connection, eventName)) {
            container.data.put("subscription", false);
            connection.send(container);
            return;
        }

        if (requestType.equals("subscribe")) {
            container.data.put("subscription",
                    SubscriptionManager.addSubscription(eventName, connection));
            connection.send(container);
            return;
        }

        if (requestType.equals("unsubscribe")) {
            container.data.put("subscription",
                    SubscriptionManager.removeSubscription(eventName, connection));
            connection.send(container);
            //return;
        }
    }
}
