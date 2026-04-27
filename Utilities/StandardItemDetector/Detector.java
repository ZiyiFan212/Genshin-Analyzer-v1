package Utilities.StandardItemDetector;

import java.util.Set;

public class Detector {
    private Detector() {
        /* This utility class should not be instantiated */
    }


    private static final Set<String> STANDARD_CHARACTER_IDS = Set.of(
            "10000016", // refer to the note
            "10000003",
            "10000035",
            "10000042",
            "10000041",
            "10000079",
            "10000069",
            "10000104"
    );

    private static final Set<String> STANDARD_WEAPON_IDS = Set.of(
            "11502", "12501", "14501", "13501", "14502", // refer to the note
            "11501", "15501", "12502", "13502", "12503"
    );

    public static boolean isStandardCharacter(String itemId) {
        return STANDARD_CHARACTER_IDS.contains(itemId);
    }

    public static boolean isStandardWeapon(String itemId) {
        return STANDARD_WEAPON_IDS.contains(itemId);
    }

    public static Set<String> getStandardCharacterIds() {return STANDARD_CHARACTER_IDS;}
    public static Set<String> getStandardWeaponIds(){return STANDARD_WEAPON_IDS;}
}
