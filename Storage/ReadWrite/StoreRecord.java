package Storage.ReadWrite;

import Model.InfoRecord;
import com.fasterxml.jackson.databind.ObjectMapper;
import core.*;
import core.Genshin.GameService;
import core.Genshin.GenshinPlayerData;
import core.Genshin.GenshinRecordFetcher;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;


public class StoreRecord {// start class

    // data that needs to be stored
    private final GameService gameService;
    // path to write files
    private final Path directory;
    // mapper object
    private final ObjectMapper mapper;

    public StoreRecord (GameService gameService, Path directory) {
        this.gameService = gameService;
        this.directory = directory;
        this.mapper = new ObjectMapper();
        this.mapper.enable(com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT); // human-readable
    }

    public void writeJSON() throws PathValidator.PathException, IOException, GenshinRecordFetcher.EmptyListException,
            GenshinRecordFetcher.EmptyPathException, GenshinRecordFetcher.MissingComponentException, ReadRecord.DirectoryError {

        Map<String, GenshinPlayerData> map = this.gameService.fetchAllData();
        GenshinPlayerData oldData = map.entrySet().iterator().next().getValue();
        InfoRecord infoRecord = oldData.info().withCurrentTime();

        saveToFile(new GenshinPlayerData(infoRecord, oldData.getRecords()));
    }


    // save this player's record to local
    private void saveToFile(GenshinPlayerData playerData) throws IOException {
        Path filePath = this.directory.resolve("Genshin-Analyzer V1.0 " + playerData.getInfo().getUid() + ".json");
        mapper.writeValue(filePath.toFile(), playerData);
    }

    public static void savePlayerData(GenshinPlayerData playerData, Path directory) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT);

        InfoRecord updatedInfo = playerData.getInfo().withCurrentTime();
        GenshinPlayerData payload = new GenshinPlayerData(updatedInfo, playerData.getRecords());

        Path filePath = directory.resolve("Genshin-Analyzer V1.0 " + payload.getInfo().getUid() + ".json");
        mapper.writeValue(filePath.toFile(), payload);
    }

}// end class
