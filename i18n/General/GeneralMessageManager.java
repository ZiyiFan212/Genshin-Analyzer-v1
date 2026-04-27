package i18n.General;

import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

public class GeneralMessageManager {

    private static ResourceBundle bundle;
    private static String currentLang;

    // set default language as English
    static {
        load("en");
    }

    public static void load(String lang) {
        currentLang = lang;
        // load properties file from classpath, avoiding no bundle found issue
        Locale locale = Locale.forLanguageTag(lang);

        bundle = ResourceBundle.getBundle(
                "i18n.General.message",
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

    private static final Map<String, String> rawToIdMap = Map.of(
            "Character", "character",
            "角色", "character",
            "Weapon", "weapon",
            "武器", "weapon"
    );

    public static String getLocalizedType(String input) {
        String neutralId = rawToIdMap.getOrDefault(input, input.toLowerCase());
        String key = "item.type." + neutralId;
        String translated = get(key);
        if (!translated.equals(key)) return translated;
        return get("type." + neutralId);
    }

    public static String getCurrentLang() {
        return currentLang;
    }
}
