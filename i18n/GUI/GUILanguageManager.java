package i18n.GUI;

import java.util.Locale;
import java.util.ResourceBundle;

public class GUILanguageManager {
    private static ResourceBundle bundle;
    private static String currentLang;

    // set default language as English
    static {
        load("en");
    }

    public static void load(String lang) {
        currentLang = lang;
        // Load src/i18n/GUI/GUImessage_<lang>.properties from classpath
        Locale locale = Locale.forLanguageTag(lang);

        bundle = ResourceBundle.getBundle(
                "i18n.GUI.GUImessage",
                locale,
                ResourceBundle.Control.getControl(ResourceBundle.Control.FORMAT_DEFAULT)
        );
    }

    public static String get(String key) {
        try {
            return bundle.getString(key);
        } catch (Exception e) {
            return key; // translation is missing, return the key
        }
    }


}
