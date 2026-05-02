package Renderer.ServiceAction;

import Core.Genshin.GenshinPlayerData;

import java.util.*;

/**
 * Teacher: Daniel Vriesinga
 * Frank Fan at 2026/04/26
 * This class performs searching function by implementing a binary search with insertion sort,
 * retrieving data based on the input UID
 */
public class Search {// start class


    /** support getting data based on uid from the local data list
     *
     * @param uid target UID in string
     * @param localData local data map (UID -> player data {@link GenshinPlayerData})
     * @return a map containing the wanted data
     */
    public static Map<String, GenshinPlayerData> searchData(String uid,
                                                            Map<String, GenshinPlayerData> localData){
        GenshinPlayerData hit = localData.get(uid);
        // empty map if no results, a new map with corresponding data and UID
        return (hit == null) ? Map.of() : Map.of(uid, hit);
    }

    /**
     * Binary search implementation. Providing another searching method
     * @param uid target UID in string
     * @param localData localData local data map (UID -> player data {@link GenshinPlayerData})
     * @return a map containing the wanted data
     */
    public static Map<String, GenshinPlayerData> binarySearch(String uid,
                                                            Map<String, GenshinPlayerData> localData){

        // calling quick sort before the binary search
        List<Map.Entry<String, GenshinPlayerData>> list = new ArrayList<>(localData.entrySet());
        quickSort(list, 0, list.size());

        // initialize low and high bound
        int low = 0;
        int high = list.size() - 1;

        while(low <= high){
            int mid = low + (high - low) / 2;// mid key

            String key = list.get(mid).getKey();
            int val = key.compareTo(uid);
            if(val == 0){
                return Map.of(key, list.get(val).getValue());// result is found, obtain the value and key
            } else if(val < 0){
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }

        return new HashMap<>();// nothing found, return an empty map
    }


    // link for quick sort -> https://www.geeksforgeeks.org/dsa/quick-sort-algorithm/
    /**
     * A helper to perform the quick sort
     * @param list list of data in single key-value pair
     * @param low low bound
     * @param high high bound
     */
    private static void quickSort(List<Map.Entry<String, GenshinPlayerData>> list, int low, int high) {
        if (low < high) {
            // the index of the pivot
            int pi = partition(list, low, high);

            // recursively calling for smaller and greater elements
            quickSort(list, low, high);
            quickSort(list, pi + 1, high);
        }
    }

    /**
     * partition function
     * @param list data list
     * @param low low bound
     * @param high high bound
     * @return partition of the pivot
     */
    private static int partition(List<Map.Entry<String, GenshinPlayerData>> list, int low, int high) {
        // pick the last element to avoid the worst case
        String pivot = list.get(high).getKey();

        // traverse through the list, moving smaller element to the left
        int i = (low - 1);
        for (int j = low; j < high; j++) {
            if (list.get(j).getKey().compareTo(pivot) <= 0) {
                i++;
                Collections.swap(list, i, j);// collection static class used to swap two elements at two specified position
            }
        }

        // move the pivot after smaller elements, returning its partition
        Collections.swap(list, i + 1, high);
        return i + 1;
    }
}// end class
