package ro.pss.holidayforms.gui.preferences;

import com.vaadin.flow.component.ItemLabelGenerator;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.checkbox.CheckboxGroupVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.data.renderer.IconRenderer;
import com.vaadin.flow.data.selection.MultiSelectionListener;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import ro.pss.holidayforms.config.security.SecurityUtils;
import ro.pss.holidayforms.domain.User;
import ro.pss.holidayforms.domain.UserPreferences;
import ro.pss.holidayforms.gui.MessageRetriever;
import ro.pss.holidayforms.gui.layout.HolidayAppLayout;
import ro.pss.holidayforms.service.UserPreferenceService;

import java.util.*;
import java.util.stream.Collectors;

@SpringComponent
@UIScope
@Slf4j
@Route(value = "preferences", layout = HolidayAppLayout.class)
public class UserPreferencesView extends HorizontalLayout implements HasDynamicTitle {
    @Autowired
    UserPreferenceService userPreferenceService;

    private UserPreferences userPreferences;

    public UserPreferencesView(UserPreferenceService userPreferenceService) {
        this.userPreferenceService = userPreferenceService;

        User user = SecurityUtils.getLoggedInUser();
        userPreferences = userPreferenceService.findByEmployeeEmail(user.getEmail())
                .orElse(new UserPreferences(user, UserPreferences.LocaleOption.ROMANIAN, new HashSet<>(), true));

        // header
        H2 heading = new H2(MessageRetriever.get("prefHeader"));
        VerticalLayout headingLayout = new VerticalLayout();
        headingLayout.add(heading);

        // locale
        RadioButtonGroup<LocaleCountryTuple> localeGroup = new RadioButtonGroup<>();
        LocaleCountryTuple ro = new LocaleCountryTuple(UserPreferences.LocaleOption.ROMANIAN, MessageRetriever.get("prefLangRomanian"));
        LocaleCountryTuple en = new LocaleCountryTuple(UserPreferences.LocaleOption.ENGLISH, MessageRetriever.get("prefLangEnglish"));
        localeGroup.setItems(ro, en);
        localeGroup.setRenderer(new IconRenderer<>(item -> {
            String imgPath = "";
            switch (item.getOption()) {
                case ROMANIAN:
                    imgPath = "ro.jpg";
                    break;
                case ENGLISH:
                    imgPath = "en.jpg";
                    break;
            }
            Image image = new Image(imgPath, "");
            image.getStyle().set("height", "15px");
            image.getStyle().set("float", "left");
            image.getStyle().set("marginRight", "5px");
            image.getStyle().set("marginTop", "2px");
            return image;
        }, LocaleCountryTuple::getLanguage));
        localeGroup.setValue(userPreferences.getLocaleOption().equals(UserPreferences.LocaleOption.ROMANIAN) ? ro : en);
        localeGroup.addValueChangeListener(event -> this.userPreferences.setLocaleOption(event.getValue().getOption()));
        VerticalLayout localeGroupLayout = new VerticalLayout();
        localeGroupLayout.add(new H3(MessageRetriever.get("prefLang")));
        localeGroupLayout.add(localeGroup);

        // notifications
        Checkbox notificationsCbo = new Checkbox(MessageRetriever.get("prefShowNotifications"), this.userPreferences.isShowNotifications());
        notificationsCbo.addValueChangeListener(event -> this.userPreferences.setShowNotifications(event.getValue()));
        VerticalLayout notificationsLayout = new VerticalLayout();
        notificationsLayout.add(new H3(MessageRetriever.get("prefNotifications")));
        notificationsLayout.add(notificationsCbo);

        // email options
        CheckboxGroup<EmailOptionsTuple> emailGroup = new CheckboxGroup<>();
        emailGroup.addThemeVariants(CheckboxGroupVariant.LUMO_VERTICAL);
        List<EmailOptionsTuple> allEmailOptions = new ArrayList<>();
        for (UserPreferences.EmailOption option : UserPreferences.EmailOption.values()) {
            EmailOptionsTuple emailOptionsTuple = new EmailOptionsTuple(option, MessageRetriever.get("pref_" + option));
            allEmailOptions.add(emailOptionsTuple);
        }
        emailGroup.setItems(allEmailOptions);
        emailGroup.setItemLabelGenerator((ItemLabelGenerator<EmailOptionsTuple>) EmailOptionsTuple::getDescription);
        Set<EmailOptionsTuple> selectedEmailOptions = allEmailOptions.stream().filter(e -> userPreferences.getEmailOption().contains(e.getOption())).collect(Collectors.toSet());
        emailGroup.select(selectedEmailOptions);
        emailGroup.addSelectionListener((MultiSelectionListener<CheckboxGroup<EmailOptionsTuple>, EmailOptionsTuple>) multiSelectionEvent -> {
            this.userPreferences.getEmailOption().clear();
            this.userPreferences.setEmailOption(multiSelectionEvent.getAllSelectedItems().stream().map(EmailOptionsTuple::getOption).collect(Collectors.toSet()));
        });
        VerticalLayout emailOptionsLayout = new VerticalLayout();
        emailOptionsLayout.add(new H3(MessageRetriever.get("prefEmailOptions")));
        emailOptionsLayout.add(emailGroup);

        // btn save
        Button btnSave = new Button(MessageRetriever.get("btnSaveLbl"), VaadinIcon.LOCK.create(), event -> {
            userPreferenceService.savePreferences(userPreferences);
            MessageRetriever.switchLocale(userPreferences.getLocaleOption());
            Notification.show(MessageRetriever.get("preferencesSaved"), 3000, Notification.Position.TOP_CENTER);
            UI.getCurrent().getPage().reload();

        });
        VerticalLayout container = new VerticalLayout();
        container.setPadding(true);
        container.setWidth("100%");
        container.setMaxWidth("70em");
        container.setHeightFull();
        container.add(headingLayout, new Hr(), localeGroupLayout, new Hr(), notificationsLayout, new Hr(), emailOptionsLayout, btnSave);
        setJustifyContentMode(JustifyContentMode.CENTER);
        setAlignItems(Alignment.CENTER);
        add(container);
        setHeightFull();
    }

    @Override
    public String getPageTitle() {
        return MessageRetriever.get("titleInfo");
    }

}

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
class LocaleCountryTuple {
    private UserPreferences.LocaleOption option;
    private String language;
}

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
class EmailOptionsTuple {
    private UserPreferences.EmailOption option;
    private String description;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EmailOptionsTuple that = (EmailOptionsTuple) o;
        return option == that.option;
    }

    @Override
    public int hashCode() {
        return Objects.hash(option);
    }
}