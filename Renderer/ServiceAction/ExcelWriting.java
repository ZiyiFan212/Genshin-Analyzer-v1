package Renderer.ServiceAction;

import Storage.ExcelExport.ExcelExporter;
import Storage.StorageConfig;
import core.Genshin.GenshinPlayerData;

import java.io.IOException;
import java.util.Map;

public class ExcelWriting {

    public static String writingAction(Map<String, GenshinPlayerData> data, String lang){
        GenshinPlayerData playerData = data.entrySet().iterator().next().getValue();

        try{
            ExcelExporter.exportExcel(playerData, lang);
        } catch (IOException e){
            return "Error occurred in writing process!\n" + e.getMessage();
        }

        return "Excel has been successfully written!\n" +
                "Please check directory: " + StorageConfig.excelPath;
    }
}
