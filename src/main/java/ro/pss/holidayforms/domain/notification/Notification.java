package ro.pss.holidayforms.domain.notification;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ro.pss.holidayforms.gui.notification.broadcast.BroadcastEvent;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@NoArgsConstructor
@Entity
public class Notification {
    public Notification(LocalDateTime creationDateTime, LocalDateTime changedDateTime, String title,
                        String targetUserEmail, String userIdentifier, BroadcastEvent.Type type, Status status, Priority priority) {
        this.creationDateTime = creationDateTime;
        this.changedDateTime = changedDateTime;
        this.title = title;
        this.targetUserEmail = targetUserEmail;
        this.userIdentifier = userIdentifier;
        this.type = type;
        this.status = status;
        this.priority = priority;
    }

    @Id
    @GeneratedValue
    @Getter
    private Long id;

    @Getter
    @NotNull
    private LocalDateTime creationDateTime;

    @Getter @Setter
    private LocalDateTime changedDateTime;

    @Getter
    private String title;

    @Getter
    private String targetUserEmail;

    @Getter
    private String userIdentifier;

    @Enumerated(EnumType.STRING)
    @Getter
    private BroadcastEvent.Type type;

    @Enumerated(EnumType.STRING)
    @Getter @Setter
    private Status status;

    @Enumerated(EnumType.STRING)
    @Getter @Setter
    private Priority priority;

    public enum Status {
        NEW, READ
    }

    public enum Priority {
        HIGH, MEDIUM, LOW
    }
}
