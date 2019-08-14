package ro.pss.holidayforms.gui.broadcast;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Component;
import ro.pss.holidayforms.config.security.CustomUserPrincipal;
import ro.pss.holidayforms.domain.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class BroadcasterUserRemoval {
    @Autowired
    private SessionRegistry sessionRegistry;

    @Scheduled(fixedRate = 9000000) // every 15 minutes
    public void listLoggedInUsers() {
        final List<Object> allPrincipals = sessionRegistry.getAllPrincipals();
        if (allPrincipals.isEmpty()) {
            Broadcaster.unregisterAllUsers();
        } else {
            List<User> usersToRemove = new ArrayList<>();
            for (final Object principal : allPrincipals) {
                if (principal instanceof CustomUserPrincipal) {
                    final User user = ((CustomUserPrincipal) principal).getUser();
                    List<SessionInformation> activeUserSessions = sessionRegistry.getAllSessions(principal, /* includeExpiredSessions */ false); // Should not return null;
                    if (activeUserSessions.isEmpty()) {
                        usersToRemove.add(user);
                    }
                }
            }
            for (User user : usersToRemove) {
                for (Map.Entry<UserUITuple, Broadcaster.BroadcastListener> entry : Broadcaster.getListeners().entrySet()) {
                    if (entry.getKey().getUser().getEmail().equals(user.getEmail())) {
                        Broadcaster.getListeners().remove(entry);
                    }
                }
            }
        }
    }
}
