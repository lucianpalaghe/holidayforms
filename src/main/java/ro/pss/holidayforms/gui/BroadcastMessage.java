package ro.pss.holidayforms.gui;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Setter @Getter
public class BroadcastMessage {
    private String targetUserId;
    private Enum messageType;

    public enum BroadcastMessageType {
       REPLACE, APPROVE, REQUEST
    }
}
