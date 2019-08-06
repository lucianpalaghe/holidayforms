package ro.pss.holidayforms.gui;

import com.vaadin.flow.component.UI;

import java.util.Locale;
import java.util.ResourceBundle;

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
        return UI.getCurrent().getLocale();
    }

    public static String get(String key) {
        return ResourceBundle.getBundle("i18n/messages", getLocale()).getString(key);
    }
}
