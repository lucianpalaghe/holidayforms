package ro.pss.holidayforms.gui.broadcast;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ro.pss.holidayforms.domain.notification.Notification;

@NoArgsConstructor
@Setter @Getter
public class BroadcastEvent {
    private String targetUserId;
    private String userIdentifier;
    private Type type;
    private String message;
    private Notification notification;   

    public BroadcastEvent(String targetUserId, Type type, String message, String userIdentifier) {
        this.targetUserId = targetUserId;
        this.userIdentifier = userIdentifier;
        this.type = type;
        this.message = message;
    }

    public enum Type {
		SUBSTITUTE_ADDED, APPROVE_ADDED, SUBSTITUTE_CHANGED, APPROVE_CHANGED, SUBSTITUTE_DELETED, APPROVE_DELETED,
        SUBSTITUTE_ACCEPTED, SUBSTITUTE_DENIED, APPROVER_ACCEPTED, APPROVER_DENIED
    }
}
