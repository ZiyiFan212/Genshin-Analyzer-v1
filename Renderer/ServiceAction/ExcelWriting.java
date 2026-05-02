package Renderer.ServiceAction;

import Storage.ExcelExport.ExcelExporter;
import Storage.Configuration.StorageConfig;
import Core.Genshin.GenshinPlayerData;
import I18n.General.GeneralMessageManager;

import java.io.IOException;

/**
 * Teacher: Daniel Vriesinga
 * Frank Fan at 2026/04/26
 * This is the action class that transmitting the command from the GUI layer to the service layer
 */
public class ExcelWriting {// start class

    /**
     * A public static method passing data and language setting
     * @param data genshin player data
     * @param lang language in string 'zh' and 'en'
     * @return the string message indicating if the action is successful or failed
     */
    public static String writingAction(GenshinPlayerData data, String lang){
        // catch error
        try{
            ExcelExporter.exportExcel(data, lang);
        } catch (IOException e){
            return GeneralMessageManager.get("action.excel.fail") + e.getMessage();
        }

        return GeneralMessageManager.get("action.excel.success") +
                StorageConfig.excelPath;
    }
}// end class
