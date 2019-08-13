package ro.pss.holidayforms.gui.broadcast;

import com.vaadin.flow.component.UI;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Broadcaster implements Serializable {
    static final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private static final Map<UserUITuple, BroadcastListener> listeners = new HashMap<>();

    public static synchronized void register(UserUITuple uit, BroadcastListener listener) {
        listeners.put(uit, listener);
    }

    public static synchronized void unregister(UserUITuple uit) {
        listeners.remove(uit);
    }

    public static synchronized void broadcast(final BroadcastEvent message) {
        for (final Map.Entry<UserUITuple, BroadcastListener> entry : listeners.entrySet()) {
            if (entry.getKey().getUser().getEmail().equals(message.getTargetUserId())) {
                executorService.execute(() -> entry.getValue().receiveBroadcast(entry.getKey().getUi(), message));
            }
        }
    }

    public interface BroadcastListener {
        void receiveBroadcast(UI ui, BroadcastEvent message);
    }
}