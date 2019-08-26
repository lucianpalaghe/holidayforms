package ro.pss.holidayforms.gui.preferences;

import com.vaadin.flow.component.ItemLabelGenerator;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.checkbox.CheckboxGroupVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Image;
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
import ro.pss.holidayforms.domain.UserPreferences;
import ro.pss.holidayforms.domain.repo.UserPreferencesRepository;
import ro.pss.holidayforms.gui.MessageRetriever;
import ro.pss.holidayforms.gui.layout.HolidayAppLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@SpringComponent
@UIScope
@Slf4j
@Route(value = "preferences", layout = HolidayAppLayout.class)
public class UserPreferencesView extends HorizontalLayout implements HasDynamicTitle {

    @Autowired
    private UserPreferencesRepository userPreferencesRepository;
    private UserPreferences.LocaleOption localeOption;
    private boolean showNotifications = false;
    private Set<UserPreferences.EmailOption> emailOptions;

    public UserPreferencesView() {
        // TODO: init  values with db data

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
        localeGroup.setValue(ro); // TODO: change to what user has set
        localeGroup.addValueChangeListener(event -> this.localeOption = event.getValue().getOption());
        VerticalLayout localeGroupLayout = new VerticalLayout();
        localeGroupLayout.add(new H3(MessageRetriever.get("prefLang")));
        localeGroupLayout.add(localeGroup);

        // notifications
        Checkbox notificationsCbo = new Checkbox(MessageRetriever.get("prefShowNotifications"));
        notificationsCbo.addValueChangeListener(event -> this.showNotifications = event.getValue().booleanValue());
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
        emailGroup.setItemLabelGenerator((ItemLabelGenerator<EmailOptionsTuple>) emailOptionsTuple -> emailOptionsTuple.getDescription());
        emailGroup.addSelectionListener((MultiSelectionListener<CheckboxGroup<EmailOptionsTuple>, EmailOptionsTuple>) multiSelectionEvent -> {
            emailOptions.clear();
            emailOptions.addAll(multiSelectionEvent.getAllSelectedItems().stream().map(e -> e.getOption()).collect(Collectors.toSet()));
        });
        VerticalLayout emailOptionsLayout = new VerticalLayout();
        emailOptionsLayout.add(new H3(MessageRetriever.get("prefEmailOptions")));
        emailOptionsLayout.add(emailGroup);

        VerticalLayout container = new VerticalLayout();
        container.setPadding(true);
        container.setWidth("100%");
        container.setMaxWidth("70em");
        container.setHeightFull();
        container.add(headingLayout, new Hr(), localeGroupLayout, new Hr(), notificationsLayout, new Hr(), emailOptionsLayout);
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
}