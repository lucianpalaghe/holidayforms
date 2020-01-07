package ro.pss.holidayforms.gui.preferences;

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
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.renderer.IconRenderer;
import com.vaadin.flow.router.*;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import ro.pss.holidayforms.config.security.SecurityUtils;
import ro.pss.holidayforms.domain.UserPreferences;
import ro.pss.holidayforms.gui.MessageRetriever;
import ro.pss.holidayforms.gui.components.dialog.HolidayConfirmationDialog;
import ro.pss.holidayforms.gui.layout.HolidayAppLayout;
import ro.pss.holidayforms.service.UserPreferenceService;

@SpringComponent
@UIScope
@Slf4j
@Route(value = "preferences", layout = HolidayAppLayout.class)
public class UserPreferencesView extends HorizontalLayout implements AfterNavigationObserver, BeforeLeaveObserver, HasDynamicTitle {
	private final Binder<UserPreferences> binder = new Binder<>(UserPreferences.class);
	private UserPreferenceService userPreferenceService;
	private UserPreferences userPreferences;
	private RadioButtonGroup<UserPreferences.Locale> localeOptions;
	private Checkbox notificationsChk;
	private CheckboxGroup<UserPreferences.EmailOption> emailOptions;

	private String browserOriginalLocation;

	@Autowired
	public UserPreferencesView(UserPreferenceService userPreferenceService) {
		this.userPreferenceService = userPreferenceService;

		// header
		H2 heading = new H2(MessageRetriever.get("prefHeader"));
		VerticalLayout headingLayout = new VerticalLayout();
		headingLayout.add(heading);

		// locale
		localeOptions = new RadioButtonGroup<>();
		localeOptions.setItems(UserPreferences.Locale.values());
		localeOptions.setRenderer(new IconRenderer<>(item -> {
			Image image = new Image("flag_" + item.name(), "");
			image.getStyle().set("height", "15px");
			image.getStyle().set("float", "left");
			image.getStyle().set("marginRight", "5px");
			image.getStyle().set("marginTop", "2px");
			return image;
		}, item -> MessageRetriever.get(MessageRetriever.get("prefLang_" + item))));
		VerticalLayout localeGroupLayout = new VerticalLayout();
		localeGroupLayout.add(new H3(MessageRetriever.get("prefLang")));
		localeGroupLayout.add(localeOptions);

		// notifications
		notificationsChk = new Checkbox(MessageRetriever.get("prefShowNotifications"), this.userPreferences.isShowNotifications());
		VerticalLayout notificationsLayout = new VerticalLayout();
		notificationsLayout.add(new H3(MessageRetriever.get("prefNotifications")));
		notificationsLayout.add(notificationsChk);

		// email options
		emailOptions = new CheckboxGroup<>();
		emailOptions.addThemeVariants(CheckboxGroupVariant.LUMO_VERTICAL);
		emailOptions.setItems(UserPreferences.EmailOption.values());
		emailOptions.setItemLabelGenerator(item -> MessageRetriever.get(MessageRetriever.get("prefEmail_" + item)));
		VerticalLayout emailOptionsLayout = new VerticalLayout();
		emailOptionsLayout.add(new H3(MessageRetriever.get("prefEmailOptions")));
		emailOptionsLayout.add(emailOptions);

		binder.bindInstanceFields(this);
		bindFields();
		// btn save
		Button btnSave = new Button(MessageRetriever.get("btnSaveLbl"), VaadinIcon.LOCK.create(), event -> {
			UserPreferences savedUp = userPreferenceService.savePreferences(userPreferences);
			MessageRetriever.setUserPreferences(savedUp);
			MessageRetriever.switchLocale(userPreferences.getLocale());
			Notification.show(MessageRetriever.get("preferencesSaved"), 2000, Notification.Position.TOP_CENTER);
			UI.getCurrent().getPage().reload();

		});
		VerticalLayout container = new VerticalLayout();
		container.setPadding(true);
		container.setWidth("100%");
		container.setMaxWidth("70em");
		container.setHeightFull();
		Hr hr = new Hr();
		container.add(headingLayout, hr, localeGroupLayout, hr, notificationsLayout, hr, emailOptionsLayout, hr, btnSave);
		setJustifyContentMode(JustifyContentMode.CENTER);
		setAlignItems(Alignment.CENTER);
		add(container);
		setHeightFull();
	}

	private void bindFields() {
		binder.forField(localeOptions).bind(UserPreferences::getLocale, UserPreferences::setLocale);
		binder.forField(notificationsChk).bind(UserPreferences::isShowNotifications, UserPreferences::setShowNotifications);
		binder.forField(emailOptions).bind(UserPreferences::getEmailOptions, UserPreferences::setEmailOptions);
	}

	@Override
	public String getPageTitle() {
		return MessageRetriever.get("titlePreferences");
	}

	@Override
	public void beforeLeave(BeforeLeaveEvent event) {
		UserPreferences userPreferences = userPreferenceService.findByEmployeeEmail(SecurityUtils.getLoggedInUser().getEmail());
		if (this.hasChanges(userPreferences)) {
			BeforeLeaveEvent.ContinueNavigationAction action = event.postpone();
			UI.getCurrent().getPage().executeJavaScript("history.replaceState({},'','" + browserOriginalLocation + "');");
			HolidayConfirmationDialog holidayConfirmationDialog = new HolidayConfirmationDialog(HolidayConfirmationDialog.HolidayConfirmationType.DENIAL,
					() -> {
						action.proceed();
						String destination = event.getLocation().getPathWithQueryParameters();
						UI.getCurrent().getPage().executeJavaScript("history.replaceState({},'','" + destination + "');");
					}, MessageRetriever.get("unsavedChanges"), MessageRetriever.get("unsavedChangesMsg"), MessageRetriever.get("answerYes"), MessageRetriever.get("backTxt"));
			holidayConfirmationDialog.open();
		}
	}

	@Override
	public void afterNavigation(AfterNavigationEvent afterNavigationEvent) {
//		browserOriginalLocation = afterNavigationEvent.getLocation().getPathWithQueryParameters();
//		resetFieldsToOriginal();
		userPreferences = userPreferenceService.findByEmployeeEmail(SecurityUtils.getLoggedInUser().getEmail());
		binder.setBean(userPreferences);
	}

	private boolean hasChanges(UserPreferences originalOptions) {
		return !(originalOptions.getLocale().equals(userPreferences.getLocale()) &&
				originalOptions.isShowNotifications() == userPreferences.isShowNotifications() &&
				userPreferences.getEmailOptions().containsAll(originalOptions.getEmailOptions()) &&
				originalOptions.getEmailOptions().size() == userPreferences.getEmailOptions().size());
	}
}