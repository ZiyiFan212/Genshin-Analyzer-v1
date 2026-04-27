package Storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class StorageConfig {

    // create a path for local data
    public static final Path dataPath = Path.of(System.getProperty("user.home"), "GenshinAnalyzer", "data");
    // create a path for export excel
    public static final Path excelPath = Path.of(System.getProperty("user.home"), "GenshinAnalyzer", "Excel");

    // create a directory to store records
    public static void initialize() throws IOException {
        Files.createDirectories(dataPath);
        Files.createDirectories(excelPath);
    }
}
