package ro.pss.holidayforms.gui.broadcast;

import com.vaadin.flow.component.UI;
import lombok.AllArgsConstructor;
import lombok.Getter;
import ro.pss.holidayforms.domain.User;

@AllArgsConstructor
@Getter
public class UserUITuple {
    User user;
    UI ui;

}
