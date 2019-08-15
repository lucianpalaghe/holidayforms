package ro.pss.holidayforms.scheduled;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Component;
import ro.pss.holidayforms.config.security.CustomUserPrincipal;
import ro.pss.holidayforms.domain.User;
import ro.pss.holidayforms.gui.broadcast.Broadcaster;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class BroadcasterUserRemoval {
    @Autowired
    private SessionRegistry sessionRegistry;

    @Scheduled(cron = "${remove.users.from.broadcaster.cron}")
    public void removeLoggedOffUsersFromBroadcaster() {
        log.info("removeLoggedOffUsersFromBroadcaster()");
        final List<Object> allPrincipals = sessionRegistry.getAllPrincipals();
        if (allPrincipals.isEmpty()) {
            Broadcaster.unregisterAllUsers();
        } else {
            List<String> loggedUsersEmail = new ArrayList<>();
            for (final Object principal : allPrincipals) {
                if (principal instanceof CustomUserPrincipal) {
                    final User user = ((CustomUserPrincipal) principal).getUser();
                    loggedUsersEmail.add(user.getEmail());
                }
            }
            Broadcaster.getListeners().entrySet().removeIf(e -> !loggedUsersEmail.contains(e.getKey().getUser().getEmail()));
        }
    }
}
