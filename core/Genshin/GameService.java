package core.Genshin;

import Storage.ReadWrite.ReadRecord;
import core.PathValidator;

import java.io.IOException;
import java.util.Map;

public abstract class GameService {

    public abstract Map<String, GenshinPlayerData> fetchAllData ()
            throws IOException, GenshinRecordFetcher.EmptyPathException, PathValidator.PathException, GenshinRecordFetcher.EmptyListException, GenshinRecordFetcher.MissingComponentException, ReadRecord.DirectoryError;
}


