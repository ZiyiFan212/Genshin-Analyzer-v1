package Model.Genshin;

/**
 * Teacher: Daniel Vriesinga
 * Frank Fan at 2026/04/26
 *
 * This the template class that stores the statistic of the gacha records.
 */
public class GenshinGachaStatSummary {

    // private final objects
    private final String uid;
    private final int totalWishes;
    private final int totalPrimogem;
    private final int fiveStarTotal;
    private final int fiveStarCharacter;
    private final int fiveStarWeapon;
    private final int limitedFiveStarCharacter;
    private final double winRatePercent;// % of 50/50s won
    private final double avgPityPerFiveStar;

    /**
     * Public parametric constructor
     * @param uid UID in string
     * @param totalWishes total number of pity in integer
     * @param totalPrimogem total number of primogem in integer
     * @param fiveStarTotal total number of 5-star items
     * @param fiveStarCharacter total number of 5-star characters
     * @param fiveStarWeapon total number of 5-star weapons
     * @param limitedFiveStarCharacter total limited 5-star characters
     * @param winRatePercent win rate in double (percentage)
     * @param avgPityPerFiveStar average pity for obtaining a 5-star (in double)
     */
    public GenshinGachaStatSummary(String uid, int totalWishes, int totalPrimogem,
                                   int fiveStarTotal, int fiveStarCharacter, int fiveStarWeapon,
                                   int limitedFiveStarCharacter, double winRatePercent, double avgPityPerFiveStar) {
        // assigning values
        this.uid = uid;
        this.totalWishes = totalWishes;
        this.totalPrimogem = totalPrimogem;
        this.fiveStarTotal = fiveStarTotal;
        this.fiveStarCharacter = fiveStarCharacter;
        this.fiveStarWeapon = fiveStarWeapon;
        this.limitedFiveStarCharacter = limitedFiveStarCharacter;
        this.winRatePercent = winRatePercent;
        this.avgPityPerFiveStar = avgPityPerFiveStar;
    }

    // getters + toString
    @Override
    public String toString() {
        return String.format("""
                UID: %s
                Total Wishes: %d | Primogems Spent: %d
                Five Stars: %d (Characters: %d | Weapons: %d)
                Limited Five Stars: %d | 50/50 Win Rate: %.1f%%
                Avg Pity per Five Star: %.1f
                """, uid, totalWishes, totalPrimogem,
                fiveStarTotal, fiveStarCharacter, fiveStarWeapon,
                limitedFiveStarCharacter, winRatePercent, avgPityPerFiveStar);
    }

    public String uid() {
        return this.uid;
    }
    public int totalWishes() {return totalWishes;}
    public int totalPrimogem() {return totalPrimogem;}
    public int fiveStarTotal() {return fiveStarTotal;}
    public int fiveStarCharacter() {return fiveStarCharacter;}
    public int fiveStarWeapon(){return fiveStarWeapon;}
    public int limitedFiveStarCharacter(){return limitedFiveStarCharacter;}
    public double overallProb(){return winRatePercent;}
    public double overallPity(){return avgPityPerFiveStar;}
}
