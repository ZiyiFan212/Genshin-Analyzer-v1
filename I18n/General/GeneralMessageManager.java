package I18n.General;

import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * This class manages the general information that is used by multiple class to handle
 *  multilingual conversion. It loads the language bundle from the default path, initializing when it is
 *  called first time.
 *  Properties files are included for zh and en support.
 * @course Teacher: Daniel Vriesinga
 * @author Frank Fan at 2026/04/26
 */
public class GeneralMessageManager {

    // language bundle and current language (not final because we need to edit it!)
    private static ResourceBundle bundle;
    private static String currentLang;

    private static final Map<String, String> rawToIdMap = Map.of(
            "Character", "character",
            "角色", "character",
            "Weapon", "weapon",
            "武器", "weapon"
    );

    // set default language as English, initializing when other methods are called
    static {
        load("en");
    }

    /**
     *  This static method loads the property file of the given language.
     * @param lang loading language
     */
    public static void load(String lang) {
        currentLang = lang;

        // load properties file from classpath, avoiding no bundle found issue
        Locale locale = Locale.forLanguageTag(lang);
        bundle = ResourceBundle.getBundle(
                "I18n.General.message",// providing a base name
                locale,
                ResourceBundle.Control.getControl(ResourceBundle.Control.FORMAT_DEFAULT)// UTC+8!!!
        );
    }

    /**
     * This method allows external classes/methods to get the corresponding translation
     * by providing a key value.
     * @param key the key of the text in string
     * @return the corresponding translation
     */
    public static String get(String key) {
        try {
            return bundle.getString(key);
        } catch (Exception _) {
            return key; // translation is missing, return the key itself
        }
    }

    /**
     * Since JSON input can contain either zh or en weapon/character type, using properties directly to convert language
     * can cause system glitch. We use a map to help obtaining the translation in different cases.
     * @param input input string
     * @return the corresponding translation
     */
    public static String getLocalizedType(String input) {
        String neutralId = rawToIdMap.getOrDefault(input, input.toLowerCase());

        String firstKey = "item.type." + neutralId;// concatenate the key
        String first = get(firstKey);
        // correct translation found, return it
        if(!first.equals(firstKey)) return first;

        String secondKey = "type." + neutralId;
        String second = get(secondKey);
        if (!second.equals(secondKey)) return second;

        return input;// no such translation is stored locally, return the original string
    }

    /**
     * Returning the current language.
     * @return string of current language "zh" or "en"
     */
    public static String getCurrentLang() {
        return currentLang;
    }
}
