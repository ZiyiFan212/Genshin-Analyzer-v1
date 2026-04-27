package Renderer.Charts;

import Model.GachaRecord;

import java.util.ArrayList;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;

// package-private class
// acting like a general information class for both swing and JFX chart to use
public class RetrieveRecord {

    private static ArrayList<GachaRecord> records;
    private static String lang;

    static{
        prepareRecords(new ArrayList<>());
        lang = "en";
    }

    public static void prepareRecords(ArrayList<GachaRecord> input){
        records = new ArrayList<>(input);
        // Defensive ordering: pity logic expects chronological sequence.
        records.sort((a, b) -> {
            LocalDateTime ta = parseTime(a.getTime());
            LocalDateTime tb = parseTime(b.getTime());
            int byTime = ta.compareTo(tb);
            if (byTime != 0) return byTime;
            return compareId(a.getId(), b.getId());
        });
    }

    public static void setLang(String newLang){
        lang = newLang;
    }

    protected static ArrayList<GachaRecord> getRecords(){
        return records;
    }

    protected static String getLang(){
        return lang;
    }

    // since pity count was not in the stat service, we calculate it here
    protected static Map<GachaRecord, Integer> calculatePity(String banner) {
        final Map<GachaRecord, Integer> pityForFiveStar = new LinkedHashMap<>();
        final ArrayList<GachaRecord> fiveStars = new ArrayList<>();
        final ArrayList<Integer> pityValues = new ArrayList<>();

        int pity = 0;
        for (GachaRecord rd : records) {
            if (!rd.getGacha_type().equals(banner)) continue;
            pity++;
            if (rd.getRank_type() == 5) {
                fiveStars.add(rd);
                pityValues.add(pity);
                pity = 0;
            }
        }
        for (int i = fiveStars.size() - 1; i >= 0; i--) {
            pityForFiveStar.put(fiveStars.get(i), pityValues.get(i));
        }
        return  pityForFiveStar;
    }

    private static LocalDateTime parseTime(String time) {
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return LocalDateTime.parse(time, df);
    }

    private static int compareId(String a, String b) {
        if (a == null && b == null) return 0;
        if (a == null) return -1;
        if (b == null) return 1;
        if (a.length() != b.length()) return Integer.compare(a.length(), b.length());
        return a.compareTo(b);
    }
}
