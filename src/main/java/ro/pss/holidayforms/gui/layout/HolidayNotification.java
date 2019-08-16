package ro.pss.holidayforms.gui.layout;

import com.github.appreciated.app.layout.notification.entitiy.DefaultNotification;
import com.github.appreciated.app.layout.notification.entitiy.Priority;
import lombok.Getter;
import lombok.Setter;
import ro.pss.holidayforms.domain.notification.Notification;

public class HolidayNotification extends DefaultNotification {
    @Getter @Setter
    private Notification notification;

    public HolidayNotification(String title, String description, Notification notification) {
        super(title, description, getPriority(notification.getPriority()));
        this.notification = notification;
    }

    private static Priority getPriority(Notification.Priority priority) {
        switch (priority) {
            case HIGH:
                return Priority.ERROR;
            case MEDIUM:
                return Priority.WARNING;
            case LOW:
                return Priority.LOW;
            default:
                return Priority.MEDIUM;
        }
    }

}
