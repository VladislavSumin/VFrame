package ru.falseteam.vframe.subscriptions;

import javafx.util.Pair;
import ru.falseteam.vframe.socket.ConnectionAbstract;
import ru.falseteam.vframe.socket.Container;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Subscription manager.
 *
 * @author Sumin Vladislav
 * @version 1.0
 */
public class SubscriptionManager {
    private static final Map<String, Pair<SubscriptionInterface, List<ConnectionAbstract>>> events = new HashMap<>();

    public static void addEvent(String name, SubscriptionInterface allInfoMethod) {
        synchronized (events) {
            events.put(name, new Pair<>(allInfoMethod, new LinkedList<>()));
        }
    }

    private static Map<String, Object> getAllData(String eventName) {
        return events.get(eventName).getKey().getAllData();
    }

    public static void onEventDataChange(String eventName, Map<String, Object> newData) {
        events.get(eventName).getValue().forEach(connection ->
                connection.send(dataUpdate(eventName, getAllData(eventName))));
        //TODO отправлять только изменения а не все по новой
    }

    public static boolean addSubscription(String eventName, ConnectionAbstract connection) {
        synchronized (events) {
            if (events.containsKey(eventName)) {
                events.get(eventName).getValue().add(connection);
                connection.send(dataUpdate(eventName, getAllData(eventName)));
                return true;
            }
            return false;
        }
    }

    public static boolean removeSubscription(String eventName, ConnectionAbstract connection) {
        synchronized (events) {
            if (events.containsKey(eventName)) {
                events.get(eventName).getValue().remove(connection);
                return true;
            }
            return false;
        }
    }

    public static void removeSubscriber(ConnectionAbstract connection) {
        synchronized (events) {
            events.forEach((s, pair) -> pair.getValue().remove(connection));
        }
    }

    private static Container dataUpdate(String eventName, Map<String, Object> data) {
        Container container = new Container("SubscriptionSyncProtocol", true);
        container.data.put("eventName", eventName);
        container.data.put("data", data);
        return container;
    }
    
    //TODO возможно потом протокол синхронизации, где при повтороной подписке вычисляются изменения, но это потом.
}