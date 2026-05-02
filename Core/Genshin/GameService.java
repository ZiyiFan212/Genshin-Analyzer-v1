package Core.Genshin;

import Storage.ReadWrite.ReadRecord;
import Core.Path.PathValidator;

import java.io.IOException;
import java.util.Map;

/**
 * @course: Teacher: Daniel Vriesinga
 * @author: Frank Fan at 2026/04/26
 * <p>An abstract class extended by {@link GenshinRecordFetcher}, ruled that all data must
 * be fetched and returned in a map.
 */
public abstract class GameService {

    // abstract method return the UID and game records in a map
    public abstract Map<String, GenshinPlayerData> fetchAllData ()

            throws IOException, GenshinRecordFetcher.EmptyPathException, PathValidator.PathException, GenshinRecordFetcher.EmptyListException,
            GenshinRecordFetcher.MissingComponentException, ReadRecord.DirectoryError;
}


