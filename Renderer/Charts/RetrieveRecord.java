package Renderer.Charts;

import Model.RecordTemplate.GachaRecord;

import java.util.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// package-private class acting like a general information class for both swing and JFX chart to use
/**
 * <p></p>This class obtains the record and calculates the pity for each five-star, used by
 * {@link JFXChart} and {@link SwingChart}.
 * Teacher: Daniel Vriesinga
 * Frank Fan at 2026/04/26
 */
public class RetrieveRecord {

    // static object for holding data and preferred language
    private static ArrayList<GachaRecord> records;
    private static String lang;


    // static block runs when the method is called, current language is set as well.
    static{
        prepareRecords(new ArrayList<>());
        lang = "en";
    }

    /**
     * Sort records on ascending chronological order.
     * @param input record list (should use list, an interface)
     */
    public static void prepareRecords(ArrayList<GachaRecord> input){
        records = new ArrayList<>(input);
        // defensive ordering: must follow chronological order.
        records.sort((a, b) -> {
            LocalDateTime ta = parseTime(a.getTime());
            LocalDateTime tb = parseTime(b.getTime());
            int byTime = ta.compareTo(tb);
            if (byTime != 0) return byTime;// return the comparison result
            return compareId(a.getId(), b.getId());// return the ID comparison when times are equal
        });
    }

    // getters + setters
    public static void setLang(String newLang){ lang = newLang;}
    protected static ArrayList<GachaRecord> getRecords(){ return records;}
    protected static String getLang(){return lang;}

    /** Calculates the pity for each five-star item for limited even banners.
     *  Trailing pity is also recorded.
     *
     * @param banner caller provides a banne code (e.g. 301, 400, etc)
     * @return an immutable pityWithTrailing class that stores each five star's pity and trailing value
     */
    protected static pityWithTrailing calculatePity(String banner) {
        final Map<GachaRecord, Integer> pityForFiveStar = new LinkedHashMap<>();

        // modern syntax..
        final var fiveStars = new ArrayList<GachaRecord>();// 5-star list
        final var pityValues = new ArrayList<Integer>();// pity value

        int pity = 0;

        // iterate through the list
        for (GachaRecord rd : records) {
            if (!rd.getGacha_type().equals(banner)) continue;
            pity++;

            // add it to the map if the item is five star
            if (rd.getRank_type() == 5) {
                fiveStars.add(rd);
                pityValues.add(pity);
                pity = 0;
            }
        }

        // since two lists are parallel, every five-star item has a corresponding pity value
        for (int i = fiveStars.size() - 1; i >= 0; i--) {
            pityForFiveStar.put(fiveStars.get(i), pityValues.get(i));
        }

        return new pityWithTrailing(pityForFiveStar, pity);
    }

    /**
     * A helper simply parses the string time to {@link LocalDateTime}
     * @param time string time
     * @return time as a LocalDateTime format
     */
    private static LocalDateTime parseTime(String time) {
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return LocalDateTime.parse(time, df);
    }

    /**
     * Compare the given id when the time is the exactly same, carefully guarded for possible null objects
     * @param a string left
     * @param b string right
     * @return the result of comparison in integer
     */
    private static int compareId(String a, String b) {
        // handling extremely rare case when time is null or incorrect formatted
        if (a == null && b == null) return 0;
        if (a == null) return -1;
        if (b == null) return 1;
        if (a.length() != b.length()) return Integer.compare(a.length(), b.length());

        // return the comparison result
        return a.compareTo(b);
    }

    // record class for double-return, getter is auto-generated
    public record pityWithTrailing(Map<GachaRecord, Integer> pityForFiveStar, int trailing){}
}
