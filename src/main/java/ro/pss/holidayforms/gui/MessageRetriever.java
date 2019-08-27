package ro.pss.holidayforms.gui;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.spring.annotation.SpringComponent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import ro.pss.holidayforms.config.security.SecurityUtils;
import ro.pss.holidayforms.domain.UserPreferences;
import ro.pss.holidayforms.service.UserPreferenceService;

import javax.annotation.PostConstruct;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Optional;
import java.util.ResourceBundle;

@Slf4j
@SpringComponent
public class MessageRetriever {
	private static UserPreferenceService userPreferenceService;
	private static Optional<UserPreferences> userPreferences;

	@Autowired
	UserPreferenceService userPreferenceServiceAutowired;

	@PostConstruct
	private void init() {
		userPreferenceService = this.userPreferenceServiceAutowired;
	}

	public static void switchLocale(UserPreferences.LocaleOption localeOption) {
		switch(localeOption) {
			case ROMANIAN:
				changeLocaleToRo();
				break;
			case ENGLISH:
				changeLocaleToEn();
				break;
		}
	}

	private static void changeLocaleToRo() {
		UI.getCurrent().getSession().setLocale(new Locale("ro", "RO"));
	}

	private static void changeLocaleToEn() {
		UI.getCurrent().getSession().setLocale(new Locale("en", "US"));
	}

	public static Locale getLocale() {
		Locale locale;
		try {
			locale = getUserPreferences().isEmpty() ? UI.getCurrent().getLocale() : getLocaleFromLocaleOption(userPreferences.get().getLocaleOption());
		}catch (Exception e) {
			locale = new Locale("ro");
		}
		return locale;
	}

	private static Locale getLocaleFromLocaleOption(UserPreferences.LocaleOption localeOption) {
		Locale locale = null;
		switch (localeOption) {
			case ROMANIAN:
				locale = new Locale("ro");
				break;
			case ENGLISH:
				locale = new Locale("en");
				break;
		}
		return locale;
	}

	public static String get(String key) {
		try {
			return ResourceBundle.getBundle("i18n/messages", getLocale()).getString(key);
		} catch (MissingResourceException e) {
			log.warn(String.format("%s is missing from the messages bundle!", key));
			return "##_" + key;
		}
	}

	public static String get(String key, UserPreferences.LocaleOption localeOption) {
		try {
			return ResourceBundle.getBundle("i18n/messages", getLocaleFromLocaleOption(localeOption)).getString(key);
		} catch (MissingResourceException e) {
			log.warn(String.format("%s is missing from the messages bundle!", key));
			return "##_" + key;
		}
	}

	private static Optional<UserPreferences> getUserPreferences() {
		if(userPreferences == null || userPreferences.isEmpty()) {
			userPreferences = userPreferenceService.findByEmployeeEmail(SecurityUtils.getLoggedInUser().getEmail());
		}
		return userPreferences;
	}

	public static void setUserPreferences(UserPreferences userPreferences) {
		MessageRetriever.userPreferences = Optional.of(userPreferences);
	}
}
