package Renderer.ServiceAction;

import core.Genshin.GenshinPlayerData;

import java.util.*;

public class Search {

    // support searching based on uid from the local data list
    public static Map<String, GenshinPlayerData> binarySearch(String uid,
                                                              Map<String, GenshinPlayerData> localData){

        List<String> UIDs = localData.keySet().stream().toList();

        int low = 0;
        int high = UIDs.size() - 1;

        while(low <= high){
            int mid = (low + high) / 2;

            String key = UIDs.get(mid);
            int val = key.compareTo(uid);
            if(UIDs.contains(key)){
                return Map.of(key, localData.get(key));
            } else if(val < 0){
                low = mid + 1;
            } else if(val > 0){
                high = mid - 1;
            }
        }

        return new HashMap<>();
    }


}
