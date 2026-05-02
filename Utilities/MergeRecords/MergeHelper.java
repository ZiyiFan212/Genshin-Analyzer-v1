package Utilities.MergeRecords;

import Model.RecordTemplate.GachaRecord;
import Core.Genshin.GenshinPlayerData;
import Core.Interfaces.Mergeable;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 *  This is the merge helper class that is able to detect duplicate UID,
 *  then merge the old and new data and return a sorted list.
 * @course Teacher: Daniel Vriesinga
 * @author Frank Fan at 2026/04/23
 */
public class MergeHelper {// start class

    /**
     * private, non-parameter constructor in case this class is instantiated by other classes
     */
    private MergeHelper() {
        /* This utility class should not be instantiated */
    }

    /**
     * This function searches the import data's uid from the local data uid
     * @param uid import record's uid in string
     * @param uids all UIDs of local data as a list
     * @return true when the duplication detected, false if the input data's uid is unique
     */
    public static boolean isDuplicate(String uid, List<String> uids) {
        // iterate through the list
        for (String x : uids) {
            if (x.equalsIgnoreCase(uid)) {
                return true;// true -> same UID
            }
        }

        return false;
    }

    /**
     * Once the duplicate UID is found, old data is retrieved from the old list
     * @param uid the duplicated UID in string
     * @param map local data (uid -> each player's data)
     * @return local records containing all wishes in a list
     */
    public static List<GachaRecord> getOldData(String uid, Map<String, GenshinPlayerData> map) {
        // get the value, or return a default value
        GenshinPlayerData playerData = map.getOrDefault(uid, null);
        if (playerData == null) return Collections.emptyList();

        return playerData.records();
    }

    /**
     * This is a generic class that can be used for different games, if the record extends the interface.
     *
     * @param newData the list of new gacha records that extends the mergeable interface {@link Mergeable}
     * @param localData the list of local records data
     * @return a new list of merged data
     * @param <T> generic type that is stored in list, must extends {@link Mergeable}
     */
    public static <T extends Mergeable> List<T> mergeData (List<T> newData, List<T> localData){
        // return if both importing and local data is empty
        if (newData.isEmpty() && localData.isEmpty()) {
           return new ArrayList<>();
       }

       List<T> mergeList;// declare a new list with type T

       if (localData.isEmpty()) {
           mergeList = new ArrayList<>(newData);// if no local data, return the new data
       } else if (newData.isEmpty()) {
           mergeList = new ArrayList<>(localData);
       } else {
           // dedup by id to make sure no data is lost
           Map<String, T> map = new LinkedHashMap<>();
           // add local data and new data to the linked hashmap, key must be unique
           for (T o : localData) {map.put(o.ID(), o);}
           for (T n : newData) { map.put(n.ID(), n); }
           mergeList = new ArrayList<>(map.values());
       }

       // validate and insertion sort for ascending time
       mergeList = validate(mergeList);
       insertionSort(mergeList);
       return mergeList;
    }


    /** sorting algorithm, would be compressed to one line in comparator :)
     *
     * @param list type T in the list
     * @param <T> {@link Mergeable} interface acts like a contract for type T
     */
    // Comparator.comparing(mergeable::time) <- more convenient
    private static <T extends Mergeable> void insertionSort(List<T> list) {
        for (int i = 1; i < list.size(); i++) {
            T key = list.get(i);// iterate through the list
            int j = i - 1;

            // insertion sorting algorithm implemented
            while (j >= 0 && shouldMoveAfter(list.get(j), key)) {// determine if the previous record is later
                list.set(j + 1, list.get(j));
                j--;
            }

            list.set(j + 1, key);
        }
    }

    /** a helper function to covert to local date time format from the string
     *
     * @param time record's time in string
     * @return local date time in correct format
     */
    private static LocalDateTime parseTime(String time) {
        try{
            DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            return LocalDateTime.parse(time, df);// parsing gives the correct format
        } catch (DateTimeParseException e) {
            return LocalDateTime.MIN;
        }
    }

    /** a helper to ensure the chronological order, compare the id if the time is same
     *
     * @param left the type T on the left
     * @param right the type T on the right
     * @return true if left type T is later than right type T, and vice versa.
     * @param <T> {@link Mergeable} interface as a contract
     */
    private static <T extends Mergeable> boolean shouldMoveAfter(T left, T right) {
        // call the helper function to get the time format
        LocalDateTime l = parseTime(left.Time());
        LocalDateTime r = parseTime(right.Time());

        // compare
        if (l.isAfter(r)) return true;
        if (l.isBefore(r)) return false;

        // rare case: if the time is exactly the same, compare the id
        return compareId(left.ID(), right.ID()) > 0;
    }

    /**
     * Compare the two string elements
     * @param left string on the left of the list
     * @param right string on the right of the list
     * @return a value of string comparison.
     */
    private static int compareId(String left, String right) {
        if (left == null && right == null) return 0;
        if (left == null) return -1;
        if (right == null) return 1;

        // rare case: if the ID length is different (fallback measurement)
        if (left.length() != right.length()) return Integer.compare(left.length(), right.length());
        return left.compareTo(right);
    }

    /** validator of time format
     *
     * @param list a list containing type T
     * @return a list containing only valid time format data
     * @param <T> {@link Mergeable} a contract for type T
     */
    private static <T extends Mergeable> List<T> validate(List<T> list) {
        // use iterator to traverse the list because we also need to add/remove element
        Iterator<T> it = list.iterator();

        List<T> validList = new LinkedList<>();
        while (it.hasNext()) {
            T item = it.next();
            if (!isValidTime(item.Time())) {// call the helper function to validate the time
                it.remove();
            } else {
                validList.add(item);// add it if valid
            }
        }

        return validList;
    }


    /** a helper function checking if the time pattern is correct
     *
     * @param time type T's time in string (T must implement {@link Mergeable})
     * @return true/false based on if the time is valid
     */
    private static boolean isValidTime(String time) {
        String pattern = "\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}";// defining a pattern of the time

        if (!time.matches(pattern)) {
            return false;
        }

        // try-catch block because formatter may throw exception when the time string is formatted incorrectly
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime.parse(time, formatter);
            return true;
        } catch (java.time.format.DateTimeParseException e) {
            return false;
        }
    }
}

