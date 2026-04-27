package Storage.ReadWrite;

import Model.GachaRecord;
import Model.InfoRecord;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import core.Genshin.GenshinPlayerData;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

public class ReadRecord {

    // a private path of local data directory
    private final Path directory;
    // a private mapper to parse JSON
    private final ObjectMapper mapper;

    // public parametric constructor
    public ReadRecord(Path directory) {
        this.directory = directory;
        this.mapper = new ObjectMapper();
    }

    // read all local data, stored in path
    public Map<String, GenshinPlayerData> readRecords() throws IOException, DirectoryError {
        Map<String, GenshinPlayerData> records = new LinkedHashMap<>();

        // guarded if file does not exist
        if(!Files.exists(this.directory)){
            return new HashMap<>();
        }

        if(!Files.isReadable(this.directory)){
            throw new DirectoryError("Files are not readable! " + this.directory);
        }

        // read JSON in directory
        try (Stream<Path> paths = Files.walk(this.directory)) {
            paths.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".json"))
                    .forEach(path -> {
                        try {
                            JsonNode jsonNode = mapper.readTree(path.toFile());
                            JsonNode info = jsonNode.get("info");
                            JsonNode body = jsonNode.get("list");
                            if (body == null) {
                                // Support locally saved files that use "records".
                                body = jsonNode.get("records");
                            }
                            if (info == null || body == null || body.isNull()) {
                                throw new IllegalArgumentException("missing info/list(records) fields");
                            }
                            ArrayList<GachaRecord> bodyList = mapper.readValue(mapper.treeAsTokens(body), new TypeReference<ArrayList<GachaRecord>>(){});
                            InfoRecord infoRecord = mapper.treeToValue(info, InfoRecord.class);

                            records.put(infoRecord.getUid(), new GenshinPlayerData(infoRecord, bodyList));
                        } catch (Exception e) {
                            System.err.println("Skipping corrupted file " + path + ": " + e.getMessage());
                        }
                    });
        }
        return records;
    }


    public static class DirectoryError extends Exception {
        public DirectoryError(String message) {super(message);}
    }
}
