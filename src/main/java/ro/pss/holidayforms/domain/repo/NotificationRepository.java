package ro.pss.holidayforms.domain.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.pss.holidayforms.domain.notification.Notification;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findAllByTargetUserEmailAndStatus(String targetUserEmail, Notification.Status status);

	List<Notification> findAllByTargetUserEmailOrderByStatusAscCreationDateTimeDesc(String targetUserEmail);
    void deleteAllByStatusEquals(Notification.Status status);
}
