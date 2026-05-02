package Storage.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Teacher: Daniel Vriesinga
 * Frank Fan at 2024/04/23
 *
 * A configuration class that creates and initializes a file to store data, excel, and logs.
 */
public class StorageConfig {// start class

    // create a path for local data
    public static final Path dataPath = Path.of(System.getProperty("user.home"), "GenshinAnalyzer", "data");
    // create a path for export excel
    public static final Path excelPath = Path.of(System.getProperty("user.home"), "GenshinAnalyzer", "Excel");
    // create a path for error log, used in main
    public static final Path logPath = Path.of(System.getProperty("user.home"), "GenshinAnalyzer", "log");

    /** create a directory to store records
     *
     * @throws IOException general exception caught in creating directories
     */
    public static void initialize() throws IOException {
        Files.createDirectories(dataPath);
        Files.createDirectories(excelPath);
        Files.createDirectories(logPath);
    }
}// end class
