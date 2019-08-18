package ro.pss.holidayforms.gui.notification;

import com.vaadin.flow.component.UI;
import lombok.Getter;
import org.springframework.stereotype.Service;
import ro.pss.holidayforms.gui.notification.broadcast.BroadcastEvent;
import ro.pss.holidayforms.gui.notification.broadcast.UserUITuple;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class Broadcaster implements Serializable {
    @Getter
    private static final Map<UserUITuple, BroadcastListener> listeners = new ConcurrentHashMap<>();
    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public static synchronized void register(UserUITuple uit, BroadcastListener listener) {
        if(listeners.containsValue(listener)) {
            return;
        }
       listeners.put(uit, listener);
    }

    public static synchronized void unregister(String userId) {
        for(Map.Entry<UserUITuple, BroadcastListener> entry : listeners.entrySet()) {
            if(entry.getKey().getUser().getEmail().equals(userId)) {
                listeners.remove(entry.getKey());
            }
        }
   }

   static synchronized void unregisterAllUsers() {
        listeners.clear();
   }

//   public static synchronized void unregister(String userId, int uiId) {
//       for(Map.Entry<UserUITuple, BroadcastListener> entry : listeners.entrySet()) {
//           if(entry.getKey().getUser().getEmail().equals(userId) && (entry.getKey().getUi().getUIId() == uiId)) {
//               listeners.remove(entry.getKey());
//           }
//       }
//   }

    static synchronized void broadcast(final BroadcastEvent message) {
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