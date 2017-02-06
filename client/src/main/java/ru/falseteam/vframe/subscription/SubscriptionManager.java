package ru.falseteam.vframe.subscription;

import ru.falseteam.vframe.redraw.Redrawer;
import ru.falseteam.vframe.socket.SocketWorker;
import ru.falseteam.vframe.socket.SubscriptionProtocol;

import java.util.HashMap;
import java.util.Map;


public class SubscriptionManager {
    private static final Map<String, Map<String, Object>> data = new HashMap<>();

    public static void subscribe(final String eventName, final SocketWorker sw) {
        runFromUiThread(new Runnable() {
            @Override
            public void run() {
                sw.send(SubscriptionProtocol.subscribe(eventName));
            }
        });
    }

    public static void unsubscribe(final String eventName, final SocketWorker sw) {
        runFromUiThread(new Runnable() {
            @Override
            public void run() {
                sw.send(SubscriptionProtocol.unsubscribe(eventName));
            }
        });
    }

    public static void dataUpdate(String eventName, Map<String, Object> newData) {
        synchronized (data) {
            data.put(eventName, newData);
        }
        Redrawer.redraw();
        //TODO сделать полноценные слушатели подписок
        //TODO мб атоотписка когда слушатели кончаются, впрочем как и автоподписка.
    }

    public static Map<String, Object> getData(String eventName) {
        return data.get(eventName);
    }

    private static void runFromUiThread(Runnable run) {
        new Thread(run).start();
        //TODO сб стоит 2 разных метода сделать из UI и обычный.
    }

    //TODO автоподписка после реконнекта. Важно!
}
