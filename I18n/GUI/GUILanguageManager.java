package I18n.GUI;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * A manager class designed for the GUI localization.
 * @course Teacher: Daniel Vriesinga
 * @author Frank Fan at 2026/04/25
 */
public class GUILanguageManager {
    // set up the language bundle and current language
    private static ResourceBundle bundle;
    private static String currentLang;

    // set default language as English
    static {
        load("en");
    }

    /**
     * Same functionality as the general message manager {@link I18n.General.GeneralMessageManager},
     * which loads required language property file.
     * @param lang return the corresponding translation in string
     */
    public static void load(String lang) {
        currentLang = lang;
        // load bundle from classpath (should be -> src/i18n/GUI/GUImessage_<lang>.properties)
        Locale locale = Locale.forLanguageTag(lang);
        bundle = ResourceBundle.getBundle(
                "i18n.GUI.GUImessage",
                locale,
                ResourceBundle.Control.getControl(ResourceBundle.Control.FORMAT_DEFAULT)
        );
    }

    /**
     * Returning the translation based on the input key.
     * @param key key in string
     * @return the corresponding translation
     */
    public static String get(String key) {
        try {
            return bundle.getString(key);
        } catch (Exception _) {
            return key; // translation is missing, return the key
        }
    }

    // getter
    public static String getCurrentLang(){
        return currentLang;
    }
}
