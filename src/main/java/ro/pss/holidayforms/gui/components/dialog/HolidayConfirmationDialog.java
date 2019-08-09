package ro.pss.holidayforms.gui.components.dialog;

import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import lombok.NoArgsConstructor;
import org.claspina.confirmdialog.ButtonOption;
import org.claspina.confirmdialog.ButtonType;
import org.claspina.confirmdialog.ConfirmDialog;

@NoArgsConstructor
public class HolidayConfirmationDialog {
    private ConfirmDialog confirmDialog;
    public enum HolidayConfirmationType {
        APPROVAL, DENIAL
    }

    public HolidayConfirmationDialog(HolidayConfirmationType type,Runnable runOnClick, String headerText, String message, String confirmBtnText, String cancelBtnText) {
        this.confirmDialog = ConfirmDialog
                .create()
                .withCaption(headerText)
                .withMessage(message)
                .withYesButton(runOnClick, ButtonOption.focus(), ButtonOption.caption(confirmBtnText))
                .withCancelButton(ButtonOption.caption(cancelBtnText), ButtonOption.icon(VaadinIcon.ARROW_BACKWARD))
                .withButtonAlignment(FlexComponent.Alignment.CENTER);
        switch (type)  {
            case APPROVAL: this.confirmDialog.getButton(ButtonType.YES).getElement().setAttribute("theme", "primary success"); break;
            case DENIAL: this.confirmDialog.getButton(ButtonType.YES).getElement().setAttribute("theme", "primary error"); break;
            default: this.confirmDialog.getButton(ButtonType.YES).getElement().setAttribute("theme", "primary success");
        }
        this.confirmDialog.getButton(ButtonType.CANCEL).getElement().setAttribute("theme", "primary cancel");
    }

    public void open() {
        this.confirmDialog.open();
    }

    public void close () {
        this.confirmDialog.close();
    }
}
