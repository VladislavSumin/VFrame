package ru.falseteam.vframe.socket;

import ru.falseteam.vframe.subscription.SubscriptionManager;

import java.util.Map;

class SubscriptionSyncProtocol extends ProtocolAbstract {
    @Override
    public void exec(Map<String, Object> map, SocketWorker worker) {
        SubscriptionManager.dataUpdate(map.get("eventName").toString(),
                (Map<String, Object>) map.get("data"));
    }
}
