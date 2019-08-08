package ro.pss.holidayforms.gui.broadcast;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Setter @Getter
public class BroadcastMessage {
    private String targetUserId;
	private BroadcastMessageType type;
	private String message;

    public enum BroadcastMessageType {
		SUBSTITUTE, APPROVE, REQUEST
    }
}
