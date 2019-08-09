package ro.pss.holidayforms.gui.broadcast;

import com.vaadin.flow.component.UI;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BroadcastNewData implements Serializable {
	static final ExecutorService executorService = Executors.newSingleThreadExecutor();
	private static final Map<UI, NewDataListener> listeners = new HashMap<>();

    public static synchronized void register(UI ui, NewDataListener listener) {
        listeners.put(ui, listener);
    }

    public static synchronized void unregister(UI ui) {
        listeners.remove(ui);
    }

    public static synchronized void broadcast(final String message) {
        for (final Map.Entry<UI, NewDataListener> entry : listeners.entrySet()) {
            executorService.execute(() -> entry.getValue().onDataReceive(entry.getKey(), message));
        }
    }

    public interface NewDataListener {
        void onDataReceive(UI ui, String message);
    }
}
