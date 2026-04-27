package core.Genshin;

import Model.GachaRecord;
import Model.InfoRecord;
import Storage.ReadWrite.ReadRecord;
import Utilities.MergeHelper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import core.PathValidator;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class GenshinRecordFetcher extends GameService {// start class

    private final Path dataFile;// final import file path
    private final Path directory;

    // public parametric constructor
    public GenshinRecordFetcher(Path dataFile, Path directory) {

        this.dataFile = dataFile;
        this.directory = directory;
    }

    // final object for reading JSON
    private static final ObjectMapper mapper = new ObjectMapper();

    // retrieve the path from the validator, handle any path exceptions
    private Path filePath () throws IOException, PathValidator.PathException, EmptyPathException {
        final PathValidator pv = new PathValidator(this.dataFile);
        Queue<Path> temp = pv.getPathQueue();
        if (temp.isEmpty() || temp.peek() == null) {
            throw new EmptyPathException("Path queue is empty for: " + this.dataFile);
        }
        return temp.peek();
    }

    // subtract the info and record body, wrapped in an array
    private Stack<JsonNode> getListBody() throws IOException, PathValidator.PathException, EmptyPathException {
        Stack<JsonNode> stack = new Stack<>();
        JsonNode node = mapper.readTree(filePath().toFile());
        JsonNode body = node.path("list");
        if (body.isMissingNode() || body.isNull()) {
            // Support locally saved files that store records under "records".
            body = node.path("records");
        }
        stack.add(node.path("info"));
        stack.add(body);
        return stack;
    }

    // implement abstract method
    @Override
    public Map<String, GenshinPlayerData> fetchAllData() throws IOException, PathValidator.PathException, EmptyPathException, EmptyListException, MissingComponentException, ReadRecord.DirectoryError {
        Map<String, GenshinPlayerData> map = new HashMap<>();

        Stack<JsonNode> stack = getListBody();
        JsonNode body = stack.pop();
        JsonNode info = stack.pop();

        ArrayList<GachaRecord> bodyList = mapper.readValue(mapper.treeAsTokens(body), new TypeReference<ArrayList<GachaRecord>>(){});
        InfoRecord infoRecord = mapper.treeToValue(info, InfoRecord.class);

        validateBodyList(bodyList);
        validateUID(infoRecord);
        bodyList.sort(Comparator.comparing(GachaRecord::getTime));// ascending time

        // get local data
        ReadRecord localData = new ReadRecord(this.directory);
        Map<String, GenshinPlayerData> local = localData.readRecords();

        // check if need to merge, if needed, merge them
        ArrayList<GachaRecord> newList = mergeData(infoRecord.getUid(), bodyList, local);


        map.put(infoRecord.getUid(), new GenshinPlayerData(infoRecord, newList));
        return map;
    }


    // check if need to merge. if yes, merge them
    private ArrayList<GachaRecord> mergeData(String newUID, ArrayList<GachaRecord> newBodyList, Map<String, GenshinPlayerData> local) {
        // duplicate check
        if(!MergeHelper.isDuplicate(newUID, local.keySet().stream().toList())) {
            return newBodyList;
        }

        // get old data
        List<GachaRecord> localDataBodyList = MergeHelper.getOldData(newUID, local);

        // get merged data
        List<GachaRecord> mergedData = MergeHelper.mergeData(newBodyList, localDataBodyList);
        mergedData.sort(Comparator.comparing(GachaRecord::getTime));
        return new ArrayList<>(mergedData);
    }

    // check if the input file has UID
    private void validateUID (InfoRecord rd) throws MissingComponentException {
        if(rd.getUid() == null || rd.getUid().isBlank()){
            throw new MissingComponentException("UID is missing from the importing JSON file!");
        }
    }

    // validate each record to see if any variables are missing
    private void validateBodyList(ArrayList<GachaRecord> bodyList) throws EmptyPathException, EmptyListException, MissingComponentException {
        if (bodyList == null || bodyList.isEmpty()) {
            throw new EmptyListException("No records are in the list! Please check your JSON file!");
        }

        List<String> invalidRecord = bodyList.stream().filter(this::isRecordIncomplete).map(GachaRecord::toString).toList();

        if(!invalidRecord.isEmpty()){
            throw new MissingComponentException("Incompletion record: " + invalidRecord + "\n");
        }
    }

    // helper function to check the name, time, type, etc.
    private boolean isRecordIncomplete(GachaRecord record) {
        Set<Integer> rank_type = Set.of(3, 4, 5);
        return isBlankOrNull(record.getName())
                || isBlankOrNull(record.getGacha_type())
                || isBlankOrNull(record.getItem_id())
                || isBlankOrNull(record.getTime())
                || isBlankOrNull(record.getId())
                || !rank_type.contains(record.getRank_type());
    }

    // helper function that returns if the string is null or blank
    private boolean isBlankOrNull(String string) {
        return string == null || string.isBlank();
    }


    public static class EmptyPathException extends Exception {
        public EmptyPathException(String message) {
            super(message);
        }
    }

    public static class EmptyListException extends Exception {
        public EmptyListException(String message) {super(message);}
    }

    public static class MissingComponentException extends Exception {
        public MissingComponentException(String message) {super(message);}
    }

}// end class
