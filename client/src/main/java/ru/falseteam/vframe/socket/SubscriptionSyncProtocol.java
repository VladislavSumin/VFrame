package ru.falseteam.vframe.socket;

import java.util.Map;

class SubscriptionSyncProtocol extends ProtocolAbstract {
    @Override
    public void exec(Map<String, Object> map, SocketWorker worker) {
        worker.getSubscriptionManager().dataUpdate(map.get("eventName").toString(),
                (Map<String, Object>) map.get("data"));
    }
}
