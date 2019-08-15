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
        super(title, description);
        this.notification = notification;
    }

    public HolidayNotification(String title, String description, Priority priority, Notification notification) {
        super(title, description, priority);
        this.notification = notification;
    }

    public HolidayNotification(String title, String description, Priority priority, boolean isSticky, Notification notification) {
        super(title, description, priority, isSticky);
        this.notification = notification;
    }

}
