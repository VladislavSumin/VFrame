package ru.falseteam.vframe.socket;

import java.util.Map;

class SyncPermissionProtocol extends ProtocolAbstract {
    @Override
    public void exec(Map<String, Object> map, SocketWorker worker) {
        worker.setCurrentPermission(map.get("permission").toString());
    }
}
