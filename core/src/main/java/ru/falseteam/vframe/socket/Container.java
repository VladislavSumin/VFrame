package ru.falseteam.vframe.socket;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Base serializable class to transfer data.
 *
 * @author Sumin Vladislav
 * @version 1.3
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class Container implements Serializable {
    public final String protocol;
    public Map<String, Object> data;

    public Container(String protocol) {
        this.protocol = protocol;
    }

    public Container(String protocol, boolean initMap) {
        this.protocol = protocol;
        if (initMap) data = new HashMap<>();
    }

    public Container(String protocol, Map<String, Object> data) {
        this.protocol = protocol;
        this.data = data;
    }
}
