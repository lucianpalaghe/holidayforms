package ro.pss.holidayforms.gui.notification.broadcast;

import lombok.Data;
import ro.pss.holidayforms.domain.notification.Notification;

@Data
public class BroadcastEvent {
    private String targetUserId;
    private String userIdentifier;
    private Type type;
    private Notification notification;   

    public BroadcastEvent(String targetUserId, Type type, String userIdentifier) {
        this.targetUserId = targetUserId;
        this.userIdentifier = userIdentifier;
        this.type = type;
    }

    public enum Type {
		SUBSTITUTE_ADDED, APPROVE_ADDED, SUBSTITUTE_CHANGED, APPROVE_CHANGED, SUBSTITUTE_DELETED, APPROVE_DELETED,
        SUBSTITUTE_ACCEPTED, SUBSTITUTE_DENIED, APPROVER_ACCEPTED, APPROVER_DENIED, WORKLOGS_POSTED
    }
}
