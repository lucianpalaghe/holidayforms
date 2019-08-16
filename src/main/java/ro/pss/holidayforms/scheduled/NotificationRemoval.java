/*
Removed because redundant for now, might return in the future.

package ro.pss.holidayforms.scheduled;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.pss.holidayforms.domain.notification.Notification;
import ro.pss.holidayforms.domain.repo.NotificationRepository;

@Service
@Slf4j
public class NotificationRemoval {
    @Autowired
    private NotificationRepository notificationRepository;

    @Scheduled(cron = "${remove.read.notifications.cron}")
    @Transactional
    public void removeReadNotifications() {
        log.info("removeReadNotifications()");
        notificationRepository.deleteAllByStatusEquals(Notification.Status.READ);
    }
}
*/
