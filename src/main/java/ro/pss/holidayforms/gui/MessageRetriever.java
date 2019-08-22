package ro.pss.holidayforms.gui;

import com.vaadin.flow.component.UI;
import lombok.extern.slf4j.Slf4j;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

@Slf4j
public class MessageRetriever {
	public static void switchLocale() {
		if (getLocale().equals(new Locale("en", "US"))) {
			changeLocaleToRo();
		} else {
			changeLocaleToEn();
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
			locale = UI.getCurrent().getLocale();
		}catch (Exception e) {
			log.error("Cannot get locale from UI, set default", e);
			locale = new Locale("ro");
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
}
