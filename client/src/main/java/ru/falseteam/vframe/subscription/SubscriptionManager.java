package ru.falseteam.vframe.subscription;

import ru.falseteam.vframe.redraw.Redrawer;
import ru.falseteam.vframe.socket.SocketWorker;
import ru.falseteam.vframe.socket.SubscriptionProtocol;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Sumin Vladislav
 * @version 1.2
 */
public class SubscriptionManager {
    private static SocketWorker sw;

    private static class Pair {
        int subscriptionCount = 1;
        Map<String, Object> data = null;
    }

    private static final Map<String, Pair> data = new HashMap<>();


    public static void init(final SocketWorker sw) {
        SubscriptionManager.sw = sw;
        sw.addOnConnectionChangeStateListener(new SocketWorker.OnConnectionChangeStateListener() {
            @Override
            public void onConnectionChangeState(boolean connected) {
                if (connected) {

                }
            }
        });
    }

    public static void subscribe(final String eventName) {
        synchronized (data) {
            Pair pair = data.get(eventName);
            if (pair == null) {
                data.put(eventName, new Pair());
                runFromUiThread(new Runnable() {
                    @Override
                    public void run() {
                        sw.send(SubscriptionProtocol.subscribe(eventName));
                    }
                });
            } else {
                pair.subscriptionCount++;
            }
        }
    }

    public static void unsubscribe(final String eventName) {
        synchronized (data) {
            Pair pair = data.get(eventName);
            if (pair.subscriptionCount == 1) {
                data.remove(eventName);

                runFromUiThread(new Runnable() {
                    @Override
                    public void run() {
                        sw.send(SubscriptionProtocol.unsubscribe(eventName));
                    }
                });
            } else {
                pair.subscriptionCount--;
            }
        }

    }

    public static void dataUpdate(String eventName, Map<String, Object> newData) {
        synchronized (data) {
            Pair pair = data.get(eventName);
            if (pair != null) {
                pair.data = newData;
            }
        }
        Redrawer.redraw();
        //TODO сделать полноценные слушатели подписок
        //TODO мб атоотписка когда слушатели кончаются, впрочем как и автоподписка.
    }

    public static Map<String, Object> getData(String eventName) {
        return data.get(eventName).data;
    }

    private static void runFromUiThread(Runnable run) {
        new Thread(run).start();
        //TODO сб стоит 2 разных метода сделать из UI и обычный.
    }

    public static void resubscribeAll() {
        synchronized (data) {
            for (String key : data.keySet()) {
                sw.send(SubscriptionProtocol.subscribe(key));
            }
        }
    }

    //TODO автоподписка после реконнекта. Важно!
}
