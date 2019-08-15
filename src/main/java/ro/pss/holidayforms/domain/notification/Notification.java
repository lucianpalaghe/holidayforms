package ro.pss.holidayforms.domain.notification;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ro.pss.holidayforms.gui.broadcast.BroadcastEvent;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.Instant;

@NoArgsConstructor
@Entity
public class Notification {
    public Notification(Instant creationDateTime, Instant changedDateTime, String title,
                        String message, String targetUserEmail, BroadcastEvent.Type type, Status status, Priority priority) {
        this.creationDateTime = creationDateTime;
        this.changedDateTime = changedDateTime;
        this.title = title;
        this.message = message;
        this.targetUserEmail = targetUserEmail;
        this.type = type;
        this.status = status;
        this.priority = priority;
    }

    @Id
    @GeneratedValue
    @Getter
    private Long id;

    @Getter @Setter
    @NotNull
    private Instant creationDateTime;

    @Getter @Setter
    private Instant changedDateTime;

    @Getter @Setter
    private String title;

    @Getter @Setter
    private String message;

    @Getter @Setter
    private String targetUserEmail;

    @Enumerated(EnumType.STRING)
    @Getter @Setter
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
        RED, ORANGE, GREEN
    }
}
