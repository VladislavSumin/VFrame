package ru.falseteam.vframe.socket;


import java.util.HashMap;
import java.util.Map;

public class PermissionManager<T extends Enum<T>> {
    private final Map<T, Map<String, ProtocolAbstract>> permissions = new HashMap<>();
    private final T defaultPermission;

    public PermissionManager(Class<T> permissionEnum, T defaultPermission) {
        this.defaultPermission = defaultPermission;
        for (T t : permissionEnum.getEnumConstants()) {
            permissions.put(t, new HashMap<>());
        }
    }

    public void addCommand(ProtocolAbstract c, T... groupies) {
        for (T g : groupies) permissions.get(g).put(c.getName(), c);
    }


    public interface DefaultProtocol {
        void exec(Container container, ConnectionAbstract connection);
    }

    private DefaultProtocol defaultProtocol = null;

    DefaultProtocol getDefaultProtocol() {
        return defaultProtocol;
    }

    public void setDefaultProtocol(DefaultProtocol defaultProtocol) {
        this.defaultProtocol = defaultProtocol;
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    Map<String, ProtocolAbstract> getProtocols(T g) {
        return permissions.get(g);
    }

    T getDefaultPermission() {
        return defaultPermission;
    }
}
