package ro.pss.holidayforms.gui.broadcast;

import com.vaadin.flow.component.UI;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BroadcastNewData implements Serializable {
	static final ExecutorService executorService = Executors.newSingleThreadExecutor();
	private static final Map<UserUITuple, NewDataListener> listeners = new HashMap<>();

    public static synchronized void register(UserUITuple uit, NewDataListener listener) {
        listeners.put(uit, listener);
    }

    public static synchronized void unregister(UserUITuple uit) {
        listeners.remove(uit);
    }

    public static synchronized void broadcast(final String message) {
        for (final Map.Entry<UserUITuple, NewDataListener> entry : listeners.entrySet()) {
            executorService.execute(() -> entry.getValue().onDataReceive(entry.getKey(), message));
        }
    }

    public interface NewDataListener {
        void onDataReceive(UserUITuple uit, String message);
    }
}
