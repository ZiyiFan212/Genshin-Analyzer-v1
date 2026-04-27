package core.Genshin;

import Model.GachaRecord;
import Model.Genshin.GenshinGachaStatSummary;
import Model.InfoRecord;
import Storage.ReadWrite.ReadRecord;
import Utilities.StandardItemDetector.Detector;
import core.PathValidator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Statistics {

    private final GameService gameService;

    public Statistics(GameService gameService) {
        this.gameService = gameService;
    }

    // normal dynamic method for the service with fetching data
    public GenshinGachaStatSummary compute() throws PathValidator.PathException, ReadRecord.DirectoryError, IOException,
            GenshinRecordFetcher.EmptyListException, GenshinRecordFetcher.EmptyPathException, GenshinRecordFetcher.MissingComponentException {
        Map<String, GenshinPlayerData> data = gameService.fetchAllData();
        GenshinPlayerData playerData = data.values().iterator().next();

        // static logic for stat
        return generateSummary(playerData);
    }


    // static for rollback calculation. Doesn't need to instantiated when calculating old data
    public static GenshinGachaStatSummary generateSummary (GenshinPlayerData playerData) {

        // grab the player data once
        ArrayList<GachaRecord> records = playerData.getRecords();
        InfoRecord info = playerData.getInfo();

        // get the UID
        String uid = info.getUid();

        // general info
        int totalWishes = records.size();
        int totalPrimogem = totalWishes * 160;
        int fiveStarNum = countFiveStar(records);
        int limitedFiveStarCharacter = countLimitedFiveStarCharacter(records);
        int fiveStarCharacter = countFiveStarCharacter(records);
        int fiveStarWeapon = fiveStarNum - fiveStarCharacter;

        // for limited character banner
        ArrayList<Integer> characterPityList = calculatePity(records, "301");
        double averageCharacterPity = calculateAveragePity(characterPityList);
        double characterProbability = calculateProbability(records, "301");

        // for limited weapon banner
        ArrayList<Integer> weaponPityList = calculatePity(records, "302");
        double averageWeaponPity = calculateAveragePity(weaponPityList);
        double weaponProbability = calculateProbability(records, "302");

        // for chronicle (weapon+character) banner
        ArrayList<Integer> chroniclePityList = calculatePity(records, "500");
        double averageChroniclePity = calculateAveragePity(chroniclePityList);
        double chronicleProbability = calculateProbability(records, "500");

        // overall probability
        double overallProb = (characterProbability + weaponProbability + chronicleProbability) / 3.0;

        // overall average pity
        double overallPity = (averageCharacterPity + averageWeaponPity + averageChroniclePity) / 3.0;

        // finally, store them and return!
        return new GenshinGachaStatSummary(
                uid, totalWishes, totalPrimogem,
                fiveStarNum, fiveStarCharacter, fiveStarWeapon, limitedFiveStarCharacter,
                overallProb, overallPity
        );
    }


    // count all five-star items, including character and weapon
    private static int countFiveStar(ArrayList<GachaRecord> records) {
        int fiveStarNum = 0;
        for (GachaRecord rd : records) {
            if (rd.getRank_type() == 5) fiveStarNum++;
        }
        return fiveStarNum;
    }

    // count all limited five-star characters
    private static int countLimitedFiveStarCharacter(ArrayList<GachaRecord> records) {
        int fiveStarLimCharacNum = 0;
        for (GachaRecord rd : records) {
            if (rd.getRank_type() == 5
                    && (rd.getItem_type().equals("角色") || rd.getItem_type().equals("character"))
                    && !Detector.getStandardCharacterIds().contains(rd.getItem_id())) fiveStarLimCharacNum++;
        }
        return fiveStarLimCharacNum;
    }

    // count how many five-star characters are pulled
    private static int countFiveStarCharacter(ArrayList<GachaRecord> records) {
        int fiveStarCharacter = 0;
        for (GachaRecord rd : records) {
            if (rd.getRank_type() == 5 && (rd.getItem_type().equals("character") || rd.getItem_type().equals("角色"))) {
                fiveStarCharacter++;
            }
        }
        return fiveStarCharacter;
    }

    // calculate the total amount of pity in this banner
    private static ArrayList<Integer> calculatePity(ArrayList<GachaRecord> records, String bannerType) {
        ArrayList<Integer> pityList = new ArrayList<>();
        int pity = 0;

        for (GachaRecord rd : records) {
            if(!rd.getGacha_type().equals(bannerType)) continue;

            pity++;
            if(rd.getRank_type() == 5){
                pityList.add(pity);
                pity = 0;
            }
        }
        return pityList;
    }

    // calculates average pity between adjacent five-star objects
    private static double calculateAveragePity(ArrayList<Integer> pityList) {
        if (pityList.isEmpty()) return 0;
        return (double) pityList.stream().mapToInt(Integer::intValue).sum() / pityList.size();
    }

    // reusable probability calculator method
    private static double calculateProbability(ArrayList<GachaRecord> records, String bannerType) {
        Set<String> set = switch (bannerType){
            case "301" -> Detector.getStandardCharacterIds();
            case "302" -> Detector.getStandardWeaponIds();
            case "500" -> Stream.concat(Detector.getStandardCharacterIds().stream(), Detector.getStandardWeaponIds().stream()).collect(Collectors.toSet());
            default -> throw new IllegalArgumentException("Invalid banner type");
        };

        int total = 0;
        int win = 0;
        for(GachaRecord rd : records){
            if(rd.getRank_type() == 5 && rd.getGacha_type().equals(bannerType)){
                total++;
                if(!set.contains(rd.getItem_id())) win++;

            }
        }
       if(total == 0) return 0;

       return (double) win/total * 100;// return a percentage
    }


}