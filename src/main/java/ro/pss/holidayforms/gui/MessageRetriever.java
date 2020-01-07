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
import java.util.ResourceBundle;

@Slf4j
@SpringComponent
public class MessageRetriever {
	private static UserPreferenceService userPreferenceService;
	private static UserPreferences userPreferences;

	@Autowired
	UserPreferenceService userPreferenceServiceAutowired;

	public static void switchLocale(UserPreferences.Locale localeOption) {
		switch (localeOption) {
			case ro:
				changeLocaleToRo();
				break;
			case en:
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

	public static String get(String key) {
		return get(key, getUserPreferences().getLocale().locale);
	}

	public static String get(String key, Locale locale) {
		try {
			return ResourceBundle.getBundle("i18n/messages", locale).getString(key);
		} catch (MissingResourceException e) {
			log.warn(String.format("%s is missing from the messages bundle!", key));
			return "##_" + key;
		}
	}

	private static UserPreferences getUserPreferences() {
		if (userPreferences == null) {
			userPreferences = userPreferenceService.findByEmployeeEmail(SecurityUtils.getLoggedInUser().getEmail());
		}
		return userPreferences;
	}

	public static void setUserPreferences(UserPreferences userPreferences) {
		MessageRetriever.userPreferences = userPreferences;
	}

	@PostConstruct
	private void init() {
		userPreferenceService = this.userPreferenceServiceAutowired;
	}
}
