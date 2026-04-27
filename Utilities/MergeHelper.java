package Utilities;

import Model.GachaRecord;
import core.Genshin.GenshinPlayerData;
import core.Interfaces.Mergeable;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class MergeHelper {
    private MergeHelper() {
        /* This utility class should not be instantiated */
    }


    public static boolean isDuplicate(String uid, List<String> uids) {
        for (String x : uids) {
            if (x.equalsIgnoreCase(uid)) {
                return true;
            }
        }

        return false;
    }

    public static List<GachaRecord> getOldData(String uid, Map<String, GenshinPlayerData> map) {
        GenshinPlayerData playerData = map.getOrDefault(uid, null);
        if (playerData == null) return Collections.emptyList();

        return playerData.getRecords();
    }

    public static <T extends Mergeable> List<T> mergeData (List<T> newData, List<T> localData){
       if (newData.isEmpty() && localData.isEmpty()) {
           return new ArrayList<>();
       }

       List<T> mergeList;
       if (localData.isEmpty()) {
           mergeList = new ArrayList<>(newData);
       } else if (newData.isEmpty()) {
           mergeList = new ArrayList<>(localData);
       } else {
           LocalDateTime latestNewTime = getLatest(newData);
           LocalDateTime latestOldTime = getLatest(localData);
           if (latestNewTime.isBefore(latestOldTime)) {
               mergeList = new ArrayList<>(localData);
           } else {
               Map<String, T> map = new LinkedHashMap<>();
               for (T o : localData) { map.put(o.ID(), o); }
               for (T n : newData) { map.put(n.ID(), n); }
               mergeList = new ArrayList<>(map.values());
           }
       }

       // validate and sort
       mergeList = validate(mergeList);
       insertionSort(mergeList);
       return mergeList;
    }

    private static <T extends Mergeable> LocalDateTime getLatest(List<T> data){
        return data.stream().map(wish -> parseTime(wish.Time())).
                max(Comparator.naturalOrder()).orElse(LocalDateTime.MIN);
    }

    // sorting algorithm, would be compressed to one line in comparator ;)
    // Comparator.comparing(mergeable::time) <- more convenient
    private static <T extends Mergeable> void insertionSort(List<T> list) {
        for (int i = 1; i < list.size(); i++) {
            T key = list.get(i);
            int j = i - 1;

            // insertion sorting algorithm implemented
            while (j >= 0 && shouldMoveAfter(list.get(j), key)) {
                list.set(j + 1, list.get(j));
                j--;
            }

            list.set(j + 1, key);
        }
    }

    // a helper function to covert to local date time format from the string
    private static LocalDateTime parseTime(String time) {
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return LocalDateTime.parse(time, df);
    }

    // a helper to ensure the chronological order, compare the id if the time is same
    private static <T extends Mergeable> boolean shouldMoveAfter(T left, T right) {
        LocalDateTime l = parseTime(left.Time());
        LocalDateTime r = parseTime(right.Time());
        if (l.isAfter(r)) return true;
        if (l.isBefore(r)) return false;

        return compareId(left.ID(), right.ID()) > 0;
    }

    private static int compareId(String left, String right) {
        if (left == null && right == null) return 0;

        if (left == null) return -1;
        if (right == null) return 1;
        if (left.length() != right.length()) return Integer.compare(left.length(), right.length());
        return left.compareTo(right);
    }

    // validator of time format
    private static <T extends Mergeable> List<T> validate(List<T> list) {
        Iterator<T> it = list.iterator();
        List<T> validList = new LinkedList<>();
        while (it.hasNext()) {
            T item = it.next();
            if (!isValidTime(item.Time())) {
                it.remove();
            } else {
                validList.add(item);
            }
        }

        return validList;
    }


    // a helper function checking if the time pattern is correct
    // add and remove are used
    private static boolean isValidTime(String time) {
        String pattern = "\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}";

        if (!time.matches(pattern)) {
            return false;
        }

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime.parse(time, formatter);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}

