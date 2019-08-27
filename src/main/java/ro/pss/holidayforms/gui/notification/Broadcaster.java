package ro.pss.holidayforms.gui.notification;

import com.vaadin.flow.component.UI;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ro.pss.holidayforms.domain.UserPreferences;
import ro.pss.holidayforms.gui.MessageRetriever;
import ro.pss.holidayforms.gui.notification.broadcast.BroadcastEvent;
import ro.pss.holidayforms.gui.notification.broadcast.UserUITuple;
import ro.pss.holidayforms.service.UserPreferenceService;

import javax.annotation.PostConstruct;
import java.io.Serializable;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class Broadcaster implements Serializable {
    @Getter
    private static final Map<UserUITuple, BroadcastListener> listeners = new ConcurrentHashMap<>();
    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private static UserPreferenceService userPreferenceService;
    private static EmailService emailService;
    private static String emailTo;
    @Autowired
    UserPreferenceService userPreferenceServiceAutowired;
    @Autowired
    EmailService emailServiceAutowired;

    @PostConstruct
    private void init()   {
        userPreferenceService =userPreferenceServiceAutowired;
        emailService = emailServiceAutowired;
    }

    @Value("${spring.mail.to}")
    private void setEmailTo(String emailTo) {
        this.emailTo = emailTo;
    }

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

    static synchronized void broadcast(final BroadcastEvent event) {
        for (final Map.Entry<UserUITuple, BroadcastListener> entry : listeners.entrySet()) {
            if (entry.getKey().getUser().getEmail().equals(event.getTargetUserId())) {
                executorService.execute(() -> entry.getValue().receiveBroadcast(entry.getKey().getUi(), event));
            }
        }
        // send email
        prepareAndSendEmail(event);
    }

    static void prepareAndSendEmail(final BroadcastEvent event) {
        // TODO: replace target user email with the target user's one
        Optional<UserPreferences> userPrefOpt = userPreferenceService.findByEmployeeEmail(event.getTargetUserId());
        if (userPrefOpt.isPresent()) {
            UserPreferences userPref = userPrefOpt.get();
            if (!userPref.getEmailOption().isEmpty()) {
                if (isAnyUserEmailOptionsMatchingWithEventType(userPref.getEmailOption(), event.getType())) {
                    String title = MessageRetriever.get("notificationTitle_" + event.getType(), userPref.getLocaleOption());
                    String emailBody = String.format(MessageRetriever.get("notificationBody_" + event.getType(), userPref.getLocaleOption()), event.getUserIdentifier());
                    emailService.sendEmail(emailTo, title, emailBody);
                }
            }
        }
    }

    private static boolean isAnyUserEmailOptionsMatchingWithEventType(Set<UserPreferences.EmailOption> emailOptions, BroadcastEvent.Type type) {
        boolean match = false;
        switch (type) {
            case SUBSTITUTE_ACCEPTED:
            case SUBSTITUTE_DENIED:
                match = emailOptions.stream().filter(e -> e.equals(UserPreferences.EmailOption.ON_REPLACER_ACTION)).count() > 0;
                break;
            case APPROVER_ACCEPTED:
            case APPROVER_DENIED:
                match = emailOptions.stream().filter(e -> e.equals(UserPreferences.EmailOption.ON_APPROVER_ACTION)).count() > 0;
                break;
            case APPROVE_ADDED:
            case SUBSTITUTE_ADDED:
                match = emailOptions.stream().filter(e -> e.equals(UserPreferences.EmailOption.ON_NEW_REQUEST)).count() > 0;
                break;
            case SUBSTITUTE_CHANGED:
            case APPROVE_CHANGED:
                match = emailOptions.stream().filter(e -> e.equals(UserPreferences.EmailOption.ON_REQUEST_CHANGED)).count() > 0;
                break;

        }
        return match;
    }

    public interface BroadcastListener {
        void receiveBroadcast(UI ui, BroadcastEvent message);
    }
}