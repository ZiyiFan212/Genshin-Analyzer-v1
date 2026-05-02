package I18n.items;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;

/**
 *
 * This class allows the external class/method to retrieve the correct translation of the item's name.
 * Two JSON files are created through using jackson to process the character info from the sever site.
 * @course Teacher: Daniel Vriesinga
 * @author Frank Fan at 2026/04/24
 **/
public class ItemTranslationManager {
    private ItemTranslationManager() {
        /* This utility class should not be instantiated */
    }

    // final object mapper to read and parse JSON
    private static final ObjectMapper mapper = new ObjectMapper();

    // don't explode memory, make them static!
    private static final Map<String, Map<String, String>> characterMap = getItemsTranslation("Characters.json");
    private static final Map<String, Map<String, String>> weaponMap = getItemsTranslation("Weapons.json");

    /**
     * The language JSON file is deserialized into a map (item id -> (English/Chinese name))
     * @param fileName the path of the JSON in string
     * @return a map of translation
     */
    private static Map<String, Map<String, String>> getItemsTranslation(String fileName){
        // load JSON to JVM memory
        InputStream inputStream = ItemTranslationManager.class.getClassLoader().getResourceAsStream("I18n/items/" + fileName);
        if (inputStream == null) {// try the file name instead
            inputStream = ItemTranslationManager.class.getClassLoader().getResourceAsStream(fileName);
        }

        // fail after re-try, print error text and return an empty map
        if (inputStream == null) {
            System.err.println("Warning: translation file not found: " + fileName);
            return Collections.emptyMap();
        }

        // deserialize it into the object
        try (InputStream in = inputStream) {
            // type reference to indicate a clear type -> what are deserializing to? map with strings!
            return mapper.readValue(in, new TypeReference<Map<String, Map<String, String>>>() {});

        // explicitly handle types of exceptions
        } catch (FileNotFoundException e) {
            System.err.println("Warning: translation file not found: " + fileName);
            return Collections.emptyMap();
        } catch (IOException e) {
            System.err.println("Warning: translation file not found: " + fileName + e);
            return Collections.emptyMap();
        }
    }


    /** Returns the name based on ID and required language.
     *
     * @param id the id of the item in string
     * @param lang required language in string -> 'zh' or 'en'
     * @return  item translation
     */
    public static String returnName(String id, String lang){

        // get default is used so that it returns id instead of a null object
        if(characterMap.containsKey(id)){
            return characterMap.get(id).getOrDefault(lang, id);
        } else if (weaponMap.containsKey(id)){
            return weaponMap.get(id).getOrDefault(lang, id);
        }

        return id;
    }

    // a getter for two maps
    public static Map<String, Map<String, String>> getCharacterMap() {
        return characterMap;
    }
    public static Map<String, Map<String, String>> getWeaponMap(){
        return weaponMap;
    }

}
