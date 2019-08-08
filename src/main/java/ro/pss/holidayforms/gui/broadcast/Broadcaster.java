package ro.pss.holidayforms.gui.broadcast;

import com.vaadin.flow.component.UI;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Broadcaster implements Serializable {
	static ExecutorService executorService = Executors.newSingleThreadExecutor();
	private static Map<UI, BroadcastListener> listeners = new HashMap<>();

	public static synchronized void register(UI ui, BroadcastListener listener) {
		listeners.put(ui, listener);
	}

	public static synchronized void unregister(UI ui) {
		listeners.remove(ui);
	}

	public static synchronized void broadcast(final BroadcastMessage message) {
		for (final Map.Entry<UI, BroadcastListener> entry : listeners.entrySet()) {
			executorService.execute(() -> entry.getValue().receiveBroadcast(entry.getKey(), message));
		}
	}

	public interface BroadcastListener {
		void receiveBroadcast(UI ui, BroadcastMessage message);
	}
}