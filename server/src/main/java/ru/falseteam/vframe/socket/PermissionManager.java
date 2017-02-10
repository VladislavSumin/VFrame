package ru.falseteam.vframe.socket;

import java.util.HashMap;
import java.util.Map;

/**
 * access level manager
 *
 * @param <T> - enum with permissions
 * @author Sumin Vladislav
 * @version 1.0
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class PermissionManager<T extends Enum<T>> {
    public interface DefaultProtocol {
        void exec(Container container, ConnectionAbstract connection);
    }

    private final Map<T, Map<String, ProtocolAbstract>> permissions = new HashMap<>();
    private final T defaultPermission;
    private DefaultProtocol defaultProtocol = null;

    public PermissionManager(Class<T> permissionEnum, T defaultPermission) {
        this.defaultPermission = defaultPermission;
        for (T t : permissionEnum.getEnumConstants()) {
            permissions.put(t, new HashMap<>());
        }
    }

    @SafeVarargs
    public final void addCommand(ProtocolAbstract c, T... groupies) {
        for (T g : groupies) permissions.get(g).put(c.getName(), c);
    }

    public void setDefaultProtocol(DefaultProtocol defaultProtocol) {
        this.defaultProtocol = defaultProtocol;
    }

    DefaultProtocol getDefaultProtocol() {
        return defaultProtocol;
    }

    Map<String, ProtocolAbstract> getProtocols(T g) {
        return permissions.get(g);
    }

    T getDefaultPermission() {
        return defaultPermission;
    }
}
