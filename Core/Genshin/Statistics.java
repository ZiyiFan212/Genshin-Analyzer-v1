package Core.Genshin;

import Model.RecordTemplate.GachaRecord;
import Model.Genshin.GenshinGachaStatSummary;
import Model.RecordTemplate.InfoRecord;
import Utilities.StandardItemDetector.Detector;

import java.util.ArrayList;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p></p>The statistic class calculates the win rate, probability, five-star counts, etc.
 *  * Wrap all information inside a class.
 * @course Teacher: Daniel Vriesinga
 * @author Frank Fan at 2026/04/26
 */
public class Statistics {

    private final GameService gameService;// a final game service instance


    /**
     * Public parametric constructor requires a {@link GameService} instance, used by the
     * importing data workflow.
     * @param gameService game service instance for initializing
     */
    public Statistics(GameService gameService) {
        this.gameService = gameService;
    }

    // refactor in the future, revamp the prob calculation
    /** A static method that calculates this user's gacha statistics.
     *
     * @param playerData player data; must be non-null
     * @return a {@link GenshinGachaStatSummary} wraps all statistics
     */
    public static GenshinGachaStatSummary generateSummary (GenshinPlayerData playerData) {

        // grab the player data once
        ArrayList<GachaRecord> records = playerData.records();
        InfoRecord info = playerData.info();

        // get the UID
        String uid = info.getUid();

        // general info
        int totalWishes = records.size();
        int totalPrimogem = totalWishes * 160;
        int fiveStarNum = countFiveStar(records);
        int limitedFiveStarCharacter = countLimitedFiveStarCharacter(records);
        int fiveStarCharacter = countFiveStarCharacter(records);
        int fiveStarWeapon = fiveStarNum - fiveStarCharacter;


        // combine both 301 and 400 as they are all limited character banner
        // varargs used because we need to pass two banner codes (one or more same types of variables)
        ArrayList<Integer> characterPityList = calculatePity(records, "301", "400");
        double characterProbability = calculateProbability(records, "301", "400");

        // limited weapon banner
        ArrayList<Integer> weaponPityList = calculatePity(records, "302");
        double weaponProbability = calculateProbability(records, "302");

        // chronicle banner
        ArrayList<Integer> chroniclePityList = calculatePity(records, "500");
        double chronicleProbability = calculateProbability(records, "500");

        // statistics algorithm (ONLY for limited banner)
        // 总体平均抽数 = (总出金时的总抽数和) / (总金数) ; total aver = (total 5-star pity) / 5 star item num
        int totalFiveStars = characterPityList.size() + weaponPityList.size() + chroniclePityList.size();
        double totalPitySum = 0;
        for(int pity : characterPityList) totalPitySum += pity;
        for(int pity : weaponPityList) totalPitySum += pity;
        for(int pity : chroniclePityList) totalPitySum += pity;

        // total pity / total gold
        double overallPity = (totalFiveStars > 0) ? totalPitySum / totalFiveStars : 0;

        // weighted calculation. When a player get one 5-star limited weapon but 10 limited character and some standard items,
        // weighting is important to reflect the real probability
        // (概率 * 该卡池金数) 之和 / 总金数 ; (prob * total gold in this banner) / total gold
        double overallProb = (totalFiveStars > 0) ?
                ((characterProbability * characterPityList.size()) + (weaponProbability * weaponPityList.size()) +
                        (chronicleProbability * chroniclePityList.size())) / totalFiveStars
                                                        : 0;

        // finally, store them and return!
        return new GenshinGachaStatSummary(
                uid, totalWishes, totalPrimogem,
                fiveStarNum, fiveStarCharacter, fiveStarWeapon, limitedFiveStarCharacter,
                overallProb, overallPity
        );
    }

    /** Count all five-star items.
     *
     * @param records array list containing every pity
     * @return numbers of five start items
     */
    private static int countFiveStar(ArrayList<GachaRecord> records) {
        int fiveStarNum = 0;
        // iterate through the records
        for (GachaRecord rd : records) {
            if (rd.getRank_type() == 5) fiveStarNum++;// 5-star
        }
        return fiveStarNum;
    }

    /** Count all limited five-star characters
     *
     * @param records array list containing all wishes
     * @return number of limited five start character
     */
    private static int countLimitedFiveStarCharacter(ArrayList<GachaRecord> records) {
        int fiveStarLimCharacNum = 0;
        // iterate all records
        for (GachaRecord rd : records) {
            // should be five star character not in the standard banner
            if (rd.getRank_type() == 5
                    && (rd.getItem_type().equals("角色") || rd.getItem_type().equals("character"))
                    && !Detector.getStandard_Character_ID().contains(rd.getItem_id())) fiveStarLimCharacNum++;
        }
        return fiveStarLimCharacNum;
    }

    /** Count number of five-star characters are obtained.
     *
     * @param records array list containing all wishes
     * @return the number of five start character
     */
    private static int countFiveStarCharacter(ArrayList<GachaRecord> records) {
        int fiveStarCharacter = 0;
        // iterate through all records
        for (GachaRecord rd : records) {
            if (rd.getRank_type() == 5 && (rd.getItem_type().equals("character") || rd.getItem_type().equals("角色"))) {
                fiveStarCharacter++;// add one
            }
        }
        return fiveStarCharacter;
    }

    /** calculate the total amount of pity in this banner
     *
     * @param records array list containing all wishes
     * @param bannerType code of each banner in string (varargs)
     * @return pity between each five star item
     */
    private static ArrayList<Integer> calculatePity(ArrayList<GachaRecord> records, String... bannerType) {
        ArrayList<Integer> pityList = new ArrayList<>();
        int pity = 0;

        Set<String> targetBanners = Set.of(bannerType);

        // iterate through the list
        for (GachaRecord rd : records) {
            // if this record is not wanted banner, skip
            if (!targetBanners.contains(rd.getGacha_type())) {
                continue;
            }

            pity++;
            if(rd.getRank_type() == 5){
                pityList.add(pity);
                pity = 0;// reset pity because five-star pulled
            }
        }
        return pityList;
    }

    /** A reusable method that calculates the win rate of a given banner.
     *
     * @param records array list containing all wishes
     * @param bannerType code of the banner in string (varargs)
     * @return the win rate percentage in double
     */
    private static double calculateProbability(ArrayList<GachaRecord> records, String... bannerType) {
        Set<String> targetBanners = Set.of(bannerType);
        String primaryType = bannerType.length > 0 ? bannerType[0] : "";

        // a switch case assigning corresponding set based on input banner
        Set<String> standardSet = switch (primaryType) {
            case "301", "400" -> Detector.getStandard_Character_ID();
            case "302" -> Detector.getStandard_Weapon_ID();
            case "500" -> Stream.concat(Detector.getStandard_Character_ID().stream(),
                            Detector.getStandard_Weapon_ID().stream())
                    .collect(Collectors.toSet());
            default -> throw new IllegalArgumentException("Invalid banner type");
        };

        int total = 0;
        int win = 0;
        // iterate through the list
        for(GachaRecord rd : records){
            if (rd.getRank_type() == 5 && targetBanners.contains(rd.getGacha_type())) {
                total++;// five start obtained
                if (!standardSet.contains(rd.getItem_id())) {
                    win++;
                }// limited five start obtained

            }
        }
       if(total == 0) return 0;
       return (double) win/total * 100;// return a percentage
    }


}