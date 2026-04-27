package Assets.Resources;

import i18n.items.ItemTranslationManager;

import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class AssetsManager {

    // a red cross png used when no icon is found
    static final String RED_CROSS = "/Assets/Resources/missingWeaponIcon.png";

    // a private map that stores item id and png path
    private static final Map<String, String> characterIconMap = new HashMap<>();
    private static final Map<String, String> weaponIconMap = new HashMap<>();
    // save memory
    private static final Map<String, Image> cache = new LinkedHashMap<>();
    private static boolean initialized = false;

    private static void initializeMaps(Map<String, String> iconMap, Map<String, Map<String, String>> dataMap, String folderName) {
        for (String id : dataMap.keySet()) {
            // process the name in translation to match file's name
            String processedENName = processFileName(dataMap.get(id).get("en"));

            // construct the path, all in lower case to be consistent with files
            String resourcePath = "/Assets/Resources/" + folderName + processedENName + ".png";

            // check if it exists
            if (AssetsManager.class.getResource(resourcePath) != null) {
                iconMap.put(id, resourcePath);
            }
        }
    }

    private static String processFileName(String name) {
        if (name == null) return "";
        // Normalize aggressively: keep only letters/digits for filename matching.
        // Example: "Wolf's Gravestone" -> "wolfsgravestone"
        return name.toLowerCase().replaceAll("[^a-z0-9]", "");
    }

    private static synchronized void ensureInitialized() {
        if (initialized) {
            return;
        }
        try {
            Map<String, Map<String, String>> characterMap = new HashMap<>(ItemTranslationManager.getCharacterMap());
            Map<String, Map<String, String>> weaponMap = new HashMap<>(ItemTranslationManager.getWeaponMap());
            initializeMaps(characterIconMap, characterMap, "CharacterIcons/");
            initializeMaps(weaponIconMap, weaponMap, "WeaponIcons/");
        } catch (Throwable t) {
            System.err.println("Warning: failed to initialize icon maps: " + t.getMessage());
        } finally {
            initialized = true;
        }
    }

    public static Image getIcon(String id) {
        ensureInitialized();

        if(cache.containsKey(id)){
            return cache.get(id);
        }

        String path = RED_CROSS;
        if(characterIconMap.containsKey(id)){
            path = characterIconMap.get(id);
        }
        if(weaponIconMap.containsKey(id)){
            path = weaponIconMap.get(id);
        }

        URL url;
        try {
            url = AssetsManager.class.getResource(path);
            if (url != null) {
                Image img = new ImageIcon(url).getImage();
                cache.put(id, img); // store in cache map
                return img;
            }
        } catch (Exception e) {
            System.err.println("Could not load icon: " + path);
        }


        URL fallback = AssetsManager.class.getResource(RED_CROSS);
        if (fallback != null) {
            return new ImageIcon(fallback).getImage();
        }

        // Fallback image when resource files are unavailable.
        return createMissingImage();
    }

    public static String getIconPath(String id) {
        ensureInitialized();
        if (characterIconMap.containsKey(id)) return characterIconMap.get(id);
        if (weaponIconMap.containsKey(id)) return weaponIconMap.get(id);
        return RED_CROSS;
    }

    private static Image createMissingImage() {
        Image img = new java.awt.image.BufferedImage(40, 40, java.awt.image.BufferedImage.TYPE_INT_ARGB);
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



