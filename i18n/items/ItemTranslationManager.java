package i18n.items;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;

public class ItemTranslationManager {

    private static final ObjectMapper mapper = new ObjectMapper();

    // don't explode memory, make them static!
    private static final Map<String, Map<String, String>> characterMap = getItemsTranslation("Characters.json");
    private static final Map<String, Map<String, String>> weaponMap = getItemsTranslation("Weapons.json");

    // deserialize json
    private static Map<String, Map<String, String>> getItemsTranslation(String fileName){
        InputStream inputStream = ItemTranslationManager.class.getClassLoader().getResourceAsStream("i18n/items/" + fileName);
        if (inputStream == null) {
            inputStream = ItemTranslationManager.class.getClassLoader().getResourceAsStream(fileName);
        }

        if (inputStream == null) {
            System.err.println("Warning: translation file not found: " + fileName);
            return Collections.emptyMap();
        }

        try (InputStream in = inputStream) {
            return mapper.readValue(in, new TypeReference<Map<String, Map<String, String>>>() {});
        } catch (FileNotFoundException e) {
            System.err.println("Warning: translation file not found: " + fileName);
            return Collections.emptyMap();
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse translation file: " + fileName, e);
        }
    }


    // return the name based on id and language setting
    public static String returnName(String id, String lang){

        // get default is used so that it returns id instead of a null object
        if(characterMap.containsKey(id)){
            return characterMap.get(id).getOrDefault(lang, id);
        }

        if (weaponMap.containsKey(id)){
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
