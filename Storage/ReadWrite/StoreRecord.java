package Storage.ReadWrite;

import Model.RecordTemplate.InfoRecord;
import Storage.Configuration.StorageConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import Core.Genshin.GenshinPlayerData;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Teacher: Daniel Vriesinga
 * Frank Fan at 2026/04/23
 *
 * This class stores the record to the directory initialized in {@link StorageConfig}
 * Export to JSON format for reusability with external Jackson library.
 */
public class StoreRecord {// start class

    /**
     * a method that serialize player data object to JSON file and store locally in given directory
     * @param playerData game data with info and records list
     * @param directory directory path
     * @throws IOException caught when writing JSON file
     */
    public static void savePlayerData(GenshinPlayerData playerData, Path directory) throws IOException {
        // create a new mapper and enable pretty writing format
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT);

        // update info class with current time
        InfoRecord updatedInfo = playerData.info().withCurrentTime();
        GenshinPlayerData gpd = new GenshinPlayerData(updatedInfo, playerData.records());

        // serialize objects to JSON files
        Path filePath = directory.resolve("Genshin-Analyzer V1.0 " + gpd.info().getUid() + ".json");
        mapper.writeValue(filePath.toFile(), gpd);
    }

}// end class
