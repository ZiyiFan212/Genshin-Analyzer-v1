package Storage.ReadWrite;

import Model.RecordTemplate.GachaRecord;
import Model.RecordTemplate.InfoRecord;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import Core.Genshin.GenshinPlayerData;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Teacher: Daniel Vriesinga
 * Frank Fan at 2026/04/23
 *
 * This class majorly focuses on reading records from locally saved file, returning it for external usages
 */
public class ReadRecord {// start class

    // a private path of local data directory
    private final Path directory;
    // a private mapper to parse JSON
    private final ObjectMapper mapper;

    /** public parametric constructor
     *
     * @param directory path of local file directory
     */
    public ReadRecord(Path directory) {
        this.directory = directory;
        this.mapper = new ObjectMapper();
    }

    /** read all local data that is originally stored in path, return the map for external usage
     *
     * @return a map (UID -> Genshin player data) containing player data
     * @throws IOException caught in reading and parsing data
     * @throws DirectoryError exception in opening files
     *
     * Resource: <a href="https://www.geeksforgeeks.org/java/java-method-references/">...</a>
     *      <a href="https://stackoverflow.com/questions/67866342/what-is-typereference-in-java-which-is-used-while-converting-a-json-script-to-ma">...</a>
     */
    public Map<String, GenshinPlayerData> readRecords() throws IOException, DirectoryError {
        Map<String, GenshinPlayerData> records = new LinkedHashMap<>();

        // guarded if file does not exist
        if(!Files.exists(this.directory)){
            return records;
        }
        // guarded if the file inside the directory is unreadable
        if(!Files.isReadable(this.directory)){
            throw new DirectoryError("Files are not readable! " + this.directory);
        }

        // read JSON in directory with filter
        try (Stream<Path> paths = Files.walk(this.directory)) {
            paths.filter(Files::isRegularFile).filter(p -> p.toString().endsWith(".json")).forEach(// filt non-JSON file
                    path -> {
                        // try-catch to guard errors in parsing JSON
                        try {
                            JsonNode jsonNode = mapper.readTree(path.toFile());
                            JsonNode info = jsonNode.get("info");
                            JsonNode body = jsonNode.get("list");

                            // handle the potential nullable cases
                            if (body == null) {
                                // support locally saved files that use "records".
                                body = jsonNode.get("records");
                            }
                            if (info == null || body == null || body.isNull()) {
                                throw new IllegalArgumentException("missing info/list(records) fields");
                            }

                            // new type reference to retain and return generic type data
                            ArrayList<GachaRecord> bodyList = mapper.readValue(mapper.treeAsTokens(body), new TypeReference<ArrayList<GachaRecord>>(){});
                            InfoRecord infoRecord = mapper.treeToValue(info, InfoRecord.class);

                            // add this local record to the map (uid -> data)
                            records.put(infoRecord.getUid(), new GenshinPlayerData(infoRecord, bodyList));
                        } catch (Exception e) {
                            System.err.println("Skipping corrupted file " + path + ": " + e.getMessage());
                        }
                    });
        }
        return records;
    }

    // customized exception handling error related to the directory (initialization, invalid, etc.)
    public static class DirectoryError extends Exception {
        public DirectoryError(String message) {super(message);}
    }
}// end class
