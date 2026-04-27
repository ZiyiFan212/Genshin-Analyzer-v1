package Renderer.ServiceAction;

import Model.GachaRecord;
import Storage.ReadWrite.ReadRecord;
import core.Genshin.GenshinPlayerData;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public class LoadDirectory {

    public LoadResult getFromDirectory(Path directory) throws ReadRecord.DirectoryError, IOException {

        try{
            ReadRecord rr = new ReadRecord(directory);

            return new LoadResult(
                    rr.readRecords(),
                    "Local data loaded successfully.",
                    true
            );
        } catch(IOException e){
            return new LoadResult(
                    new LinkedHashMap<>(),
                    "Error: local data failed to load.\n" + e.getMessage(),
                    false
            );
        }
    }

    public record LoadResult(Map<String, GenshinPlayerData> data, String message, boolean bool) {}
}
