package ru.falseteam.vframe.socket;

import ru.falseteam.vframe.redraw.Redrawer;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Sumin Vladislav
 * @version 1.4
 */
public class SubscriptionManager {
    private static class SubscriptionSyncProtocol extends ProtocolAbstract {
        @Override
        public void exec(Map<String, Object> map, SocketWorker worker) {
            worker.getSubscriptionManager().dataUpdate(map.get("eventName").toString(),
                    (Map<String, Object>) map.get("data"));
        }
    }

    private static class SubscriptionProtocol extends ProtocolAbstract {
        @Override
        public void exec(Map<String, Object> map, SocketWorker worker) {
            worker.getSubscriptionManager()
                    .setFailed(map.get("eventName").toString(), (boolean) map.get("subscription"));
        }

        static Container subscribe(String eventName) {
            Container container = new Container(SubscriptionProtocol.class.getSimpleName(), true);
            container.data.put("requestType", "subscribe");
            container.data.put("eventName", eventName);
            return container;
        }

        static Container unsubscribe(String eventName) {
            Container container = new Container(SubscriptionProtocol.class.getSimpleName(), true);
            container.data.put("requestType", "unsubscribe");
            container.data.put("eventName", eventName);
            return container;
        }
    }

    private class Pair {
        int subscriptionCount = 1;
        boolean subscriptionsFailed = false;
        Map<String, Object> data = null;
    }

    private final SocketWorker sw;
    private final Map<String, Pair> data = new HashMap<>();

    SubscriptionManager(SocketWorker sw) {
        this.sw = sw;
        sw.addProtocol(new SubscriptionSyncProtocol());
        sw.addProtocol(new SubscriptionProtocol());
    }

    public void subscribe(final String eventName) {
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

    public void unsubscribe(final String eventName) {
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

    public void dataUpdate(String eventName, Map<String, Object> newData) {
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

    public Map<String, Object> getData(String eventName) {
        return data.get(eventName).data;
    }

    private void runFromUiThread(Runnable run) {
        new Thread(run).start();
        //TODO сб стоит 2 разных метода сделать из UI и обычный.
    }

    void resubscribeAll() {
        synchronized (data) {
            for (String key : data.keySet()) {
                sw.send(SubscriptionProtocol.subscribe(key));
            }
        }
    }

    void setFailed(String eventName, boolean failed) {
        synchronized (data) {
            Pair pair = data.get(eventName);
            if (pair != null)
                pair.subscriptionsFailed = failed;
        }
    }

    public void resubscribeFailed() {//TODO попровать это.
        synchronized (data) {
            for (Map.Entry<String, Pair> key : data.entrySet()) {
                if (key.getValue().subscriptionsFailed)
                    sw.send(SubscriptionProtocol.subscribe(key.getKey()));
            }
        }
    }

    //TODO автоподписка после реконнекта. Важно!
}
