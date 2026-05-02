package Utilities.StandardItemDetector;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Teacher: Daniel Vriesinga
 * Frank Fan at 2026/04/24
 *
 * This class simply checks if the item belongs to five star standard items
 */
public class Detector {

    /**
     * A private constructor to prevent being initialized by external class
     */
    private Detector() {
        /* This utility class should not be instantiated */
    }

    // two standard character id set (refer to the note for each item..)
    private static final Set<String> standard_Character_ID = Set.of(
            "10000016", "10000003", "10000035", "10000042",
            "10000041", "10000079", "10000069","10000104"
    );

    private static final Set<String> standard_Weapon_ID = Set.of(
            "11502", "12501", "14501", "13501", "14502", // refer to the note
            "11501", "15501", "12502", "13502", "12503"
    );

    /**
     * A sequential search to detect if the item belongs to the standard item set.
     * @param itemId item ID in string
     * @return true if this item belongs to the set, false if this item is not a standard item
     */
    public static boolean sequentialSearch(String itemId) {
        Set<String> set = Stream.concat(standard_Character_ID.stream(), standard_Weapon_ID.stream())
                .collect(Collectors.toSet());// use stream api to concatenate two sets
        for(String item : set){
            if(itemId.equalsIgnoreCase(item)){ return true;}
        }
        return false;
    }

    // getters
    public static Set<String> getStandard_Character_ID() {return standard_Character_ID;}
    public static Set<String> getStandard_Weapon_ID(){return standard_Weapon_ID;}
}
