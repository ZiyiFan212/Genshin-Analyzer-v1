package Renderer.ServiceAction;

import Storage.ReadWrite.ReadRecord;
import Core.Genshin.GenshinPlayerData;
import I18n.General.GeneralMessageManager;

import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Teacher: Daniel Vriesinga
 * Frank Fan at 2026/04/26
 * This is the action class that loads local data from the directory, returning the result in a record clas
 */
public class LoadDirectory {// start class

    /**
     * This method returns the result of loading from the directory
     * @param directory path of local data file
     * @return a record class that wraps both string message and local data {@link LoadResult}
     * @throws ReadRecord.DirectoryError file or directory initialization error
     */
    public LoadResult getFromDirectory(Path directory) throws ReadRecord.DirectoryError {

        try{
            // instantiate the read record class with pointed directory path
            ReadRecord rr = new ReadRecord(directory);

            // a new record class for multi-returning
            return new LoadResult(
                    rr.readRecords(),
                    GeneralMessageManager.get("action.loading.success"),
                    true
            );
        } catch(IOException e){
            return new LoadResult(
                    new LinkedHashMap<>(),// using empty map instead of null object
                    GeneralMessageManager.get("action.loading.fail") + e.getMessage(),
                    false
            );
        }
    }

    /**
     * A container that wraps different returning type in one class
     * @param data a map of user data (UID -> player data {@link GenshinPlayerData})
     * @param message success or fail message
     * @param bool true -> load successfully, false -> load field
     */
    public record LoadResult(Map<String, GenshinPlayerData> data, String message, boolean bool) {}
}// end class
