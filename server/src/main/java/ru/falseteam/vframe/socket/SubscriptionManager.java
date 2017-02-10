package ru.falseteam.vframe.socket;

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
 * @version 2.0
 */
public class SubscriptionManager<T extends Enum<T>> {
    public interface SubscriptionInterface {
        Map<String, Object> getAllData();
    }

    private class Data {
        final SubscriptionInterface subscriptionInterface;
        final List<ConnectionAbstract<T>> subscribers;
        final T[] permissions;

        Data(SubscriptionInterface subscriptionInterface, T[] permissions) {
            this.subscriptionInterface = subscriptionInterface;
            this.permissions = permissions;
            subscribers = new LinkedList<>();
        }
    }

    private final Map<String, Data> events = new HashMap<>();

    public void addEvent(String name, SubscriptionInterface allInfoMethod, T... permissions) {
        synchronized (events) {
            events.put(name, new Data(allInfoMethod, permissions));
        }
    }

    private Map<String, Object> getAllData(String eventName) {
        return events.get(eventName).subscriptionInterface.getAllData();
    }

    public void onEventDataChange(String eventName, Map<String, Object> newData) {
        synchronized (events) {
            events.get(eventName).subscribers.forEach(connection ->
                    connection.send(dataUpdate(eventName, newData)));
            //TODO отправлять только изменения а не все по новой
        }
    }

    public boolean addSubscription(String eventName, ConnectionAbstract<T> connection) {
        synchronized (events) {
            if (events.containsKey(eventName)) {
                events.get(eventName).subscribers.add(connection);
                connection.send(dataUpdate(eventName, getAllData(eventName)));
                return true;
            }
            return false;
        }
    }

    public boolean removeSubscription(String eventName, ConnectionAbstract<T> connection) {
        synchronized (events) {
            if (events.containsKey(eventName)) {
                events.get(eventName).subscribers.remove(connection);
                return true;
            }
            return false;
        }
    }

    public void removeSubscriber(ConnectionAbstract<T> connection) {
        synchronized (events) {
            events.forEach((s, pair) -> pair.subscribers.remove(connection));
        }
    }

    private Container dataUpdate(String eventName, Map<String, Object> data) {
        Container container = new Container("SubscriptionSyncProtocol", true);
        container.data.put("eventName", eventName);
        container.data.put("data", data);
        return container;
    }

    //TODO возможно потом протокол синхронизации, где при повтороной подписке вычисляются изменения, но это потом.
}
