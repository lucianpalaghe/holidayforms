package ro.pss.holidayforms.scheduled;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Component;
import ro.pss.holidayforms.config.security.CustomUserPrincipal;
import ro.pss.holidayforms.domain.User;
import ro.pss.holidayforms.gui.broadcast.Broadcaster;

import java.util.List;

import static java.util.stream.Collectors.*;

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
			List<String> loggedUsersEmail = allPrincipals.stream()
					.filter(principal -> principal instanceof CustomUserPrincipal)
					.map(principal -> ((CustomUserPrincipal) principal).getUser())
					.map(User::getEmail)
					.collect(toList());

            Broadcaster.getListeners().entrySet().removeIf(e -> !loggedUsersEmail.contains(e.getKey().getUser().getEmail()));
        }
    }
}
