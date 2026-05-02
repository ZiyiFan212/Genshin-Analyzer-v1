package Assets.Resources;

import I18n.items.ItemTranslationManager;

import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Teacher: Daniel Vriesinga
 * Frank Fan at 2026/04/26
 * This class manages all items' icons, providing a method to get icon based on item ID
 */
public class AssetsManager {

    // a red cross png used as a fallback image
    static final String RED_CROSS = "/Assets/Resources/missingWeaponIcon.png";

    // a private map that stores item id and png path
    private static final Map<String, String> characterIconMap = new HashMap<>();
    private static final Map<String, String> weaponIconMap = new HashMap<>();
    // save memory by creating a cache map
    private static final Map<String, Image> cache = new LinkedHashMap<>();
    private static boolean initialized = false;

    /** A static method to initialize the character and weapon icon map by
     * reading the assets from the given path.
     *
     * @param iconMap icon map (item id -> path of the corresponding image)
     * @param dataMap character name map in en and zh
     * @param folderName name of the assets folder
     */
    private static void initializeMaps(Map<String, String> iconMap, Map<String, Map<String, String>> dataMap, String folderName) {
        // EntrySet instead of keyset for better efficiency
        for (Map.Entry<String, Map<String, String>> entry: dataMap.entrySet()) {
            String id = entry.getKey();

            // process the name in translation to match file's name
            String processedENName = processFileName(entry.getValue().get("en"));

            // construct the path, all in lower case to be consistent with files
            String resourcePath = "/Assets/Resources/" + folderName + processedENName + ".png";

            // check if it exists
            if (AssetsManager.class.getResource(resourcePath) != null) {
                iconMap.put(id, resourcePath);
            }
        }
    }

    /** Processes the input string by effectively removing spaces and punctuations
     *
     * @param name name of the item needed to be processed in string
     * @return purely letter-only name in string
     * <p> An empty string when the input name is null
     */
    private static String processFileName(String name) {
        if (name == null) return "";
        // keep only letters for filename matching
        // example: "Wolf's Gravestone" -> "wolfsgravestone"
        return name.toLowerCase().replaceAll("[^a-z0-9]", "");
    }


    /** Using synchronized keyword to ensure only one thread can execute initializing all map when
     *  this class is created as multiple objects, ensuring normal initialization.
     *  <p>Only loaded once in the memory
     */
    private static synchronized void ensureInitialized() {
        if (initialized) {
            return;
        }

        // try block to initialize maps
        try {
            Map<String, Map<String, String>> characterMap = new HashMap<>(ItemTranslationManager.getCharacterMap());
            Map<String, Map<String, String>> weaponMap = new HashMap<>(ItemTranslationManager.getWeaponMap());
            initializeMaps(characterIconMap, characterMap, "CharacterIcons/");
            initializeMaps(weaponIconMap, weaponMap, "WeaponIcons/");
        } catch (Throwable t) {// throwable catches all errors and exceptions, but without crashing the app
            System.err.println("Warning: errors occurred in initializing icon: " + t.getMessage());
        } finally {// set initialized = true, so even the initialization fails, we won't try again
            initialized = true;
        }
    }

    /**
     *  This static method returns the corresponding image of the input item's ID.
     *  <p>A fallback image would be returned if no such an image can be found from the
     *  resource or cache map.
     *
     * @param id Item's ID
     * @return An image object based on the input ID
     */
    public static Image getIcon(String id) {
        ensureInitialized();

        // try to find from cache if the icon is used beforehand
        if(cache.containsKey(id)){
            return cache.get(id);
        }

        String path = RED_CROSS;// default path
        // searching for icon path
        if(characterIconMap.containsKey(id)){
            path = characterIconMap.get(id);
        } else if(weaponIconMap.containsKey(id)){
            path = weaponIconMap.get(id);
        }

        URL url;
        try {
            url = AssetsManager.class.getResource(path);// try to fetch the icon from the resource pac
            // if the url is valid, return it
            if (url != null) {
                Image img = new ImageIcon(url).getImage();
                cache.put(id, img); // store in cache map
                return img;
            }
        } catch (Exception e) {
            System.err.println("Could not load icon: " + path);
        }

        // icon url is missing, using default icon instead
        URL fallback = AssetsManager.class.getResource(RED_CROSS);
        if (fallback != null) {
            return new ImageIcon(fallback).getImage();
        }

        // fallback image when resource files are unavailable
        return createMissingImage();
    }

    /** This method simply returns the icon path in string by the item id
     *
     * @param id item ID in string
     * @return the path of icon in string
     */
    public static String getIconPath(String id) {
        ensureInitialized();
        if (characterIconMap.containsKey(id)) return characterIconMap.get(id);
        if (weaponIconMap.containsKey(id)) return weaponIconMap.get(id);
        return RED_CROSS;
    }

    /** This creates a red X when the default icon failed to load
     *
     * @return fallback image
     */
    private static Image createMissingImage() {
        // create a 40 by 40 canvas
        Image img = new java.awt.image.BufferedImage(40, 40, java.awt.image.BufferedImage.TYPE_INT_ARGB);

        // paint the X
        Graphics2D g2 = (Graphics2D) img.getGraphics();
        g2.setColor(new Color(220, 220, 220));
        g2.fillRect(0, 0, 40, 40);
        g2.setColor(new Color(180, 40, 40));
        g2.drawLine(6, 6, 34, 34);
        g2.drawLine(34, 6, 6, 34);
        g2.dispose();
        return img;
    }
}



