package ro.pss.holidayforms.gui.broadcast;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Setter @Getter
public class BroadcastEvent {
    private String targetUserId;
	private Type type;
	private String message;

    public enum Type {
		SUBSTITUTE_ADDED, APPROVE_ADDED, SUBSTITUTE_CHANGED, APPROVE_CHANGED, SUBSTITUTE_DELETED, APPROVE_DELETED
    }
}
