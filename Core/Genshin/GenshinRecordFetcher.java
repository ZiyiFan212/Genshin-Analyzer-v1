package Core.Genshin;

import Model.RecordTemplate.GachaRecord;
import Model.RecordTemplate.InfoRecord;
import Storage.ReadWrite.ReadRecord;
import Utilities.MergeRecords.MergeHelper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import Core.Path.PathValidator;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

/**
 * @course Teacher: Daniel Vriesinga
 * @author Frank Fan at 2026/04/24
 * <p></p>This class fetch the data by using external library {@link ObjectMapper}.
 * Stack and Queue are implemented.
 */
public class GenshinRecordFetcher extends GameService {// start class

    private final Path dataFile;// final import file path
    private final Path directory;// local data directory

    /** Public parametric constructor to obtain the path of the data file and directory.
     *
     * @param dataFile path of import json
     * @param directory path of local data
     */
    public GenshinRecordFetcher(Path dataFile, Path directory) {

        this.dataFile = dataFile;
        this.directory = directory;
    }

    // final object for reading JSON
    private static final ObjectMapper mapper = new ObjectMapper();

    /** A valid path is obtained from {@link PathValidator}, returning it for other usage.
     *
     * @return the path of the import file
     * @throws IOException general exception type in input/output
     * @throws PathValidator.PathException exception when the path is invalid {@link PathValidator}
     * @throws EmptyPathException exception when the provided path is empty
     */
    private Path filePath () throws IOException, PathValidator.PathException, EmptyPathException {
        final PathValidator pv = new PathValidator(this.dataFile);// final instance to get all data
        Queue<Path> temp = pv.getPathQueue();
        if (temp.isEmpty() || temp.peek() == null) {
            throw new EmptyPathException("Path queue is empty for: " + this.dataFile);
        }
        return temp.peek();// return the value
    }

    /**
     * Read the file through the provided path from filePate, extract the information
     * and list body and wrap them in stack.
     * @return stack of json node, containing both info and list body
     * @throws IOException general exception in input/output
     * @throws PathValidator.PathException exception when the path is invalid {@link PathValidator}
     * @throws EmptyPathException exception when the provided path is empty
     */
    private Stack<JsonNode> getListBody() throws IOException, PathValidator.PathException, EmptyPathException {
        Stack<JsonNode> stack = new Stack<>();

        // extract node from json by using object mapper (jackson)
        JsonNode node = mapper.readTree(filePath().toFile());
        JsonNode body = node.path("list");
        if (body.isMissingNode() || body.isNull()) {
            // support locally saved files that store records as "records"
            body = node.path("records");
        }

        // add list and info nodes to the stck
        stack.add(node.path("info"));
        stack.add(body);
        return stack;
    }



    /** Implementation of the abstract method. Fetch all records by parsing JSON nodes.
     *
     * @return A map of UID -> {@link GenshinPlayerData}.
     * @throws IOException general input/output exception
     * @throws PathValidator.PathException exception when the path or json format is invalid {@link PathValidator}
     * @throws EmptyPathException exception when the path is empty
     * @throws EmptyListException exception when the input json list is empty
     * @throws MissingComponentException missing either "list" or "info" body
     * @throws ReadRecord.DirectoryError {@link ReadRecord} exception in loading directory
     */
    @Override
    public Map<String, GenshinPlayerData> fetchAllData() throws IOException, PathValidator.PathException, EmptyPathException, EmptyListException,
            MissingComponentException, ReadRecord.DirectoryError {

        Map<String, GenshinPlayerData> map = new HashMap<>();// data map

        // retrieve the json node (Plz change to array next time!)
        Stack<JsonNode> stack = getListBody();
        JsonNode body = stack.pop();
        JsonNode info = stack.pop();

        // read value and store to the list
        ArrayList<GachaRecord> bodyList = mapper.readValue(mapper.treeAsTokens(body), new TypeReference<ArrayList<GachaRecord>>(){});// -> retain the generic data info
        InfoRecord infoRecord = mapper.treeToValue(info, InfoRecord.class);

        // validate both record and uid
        validateBodyList(bodyList);
        validateUID(infoRecord);
        bodyList.sort(Comparator.comparing(GachaRecord::getTime));// ascending time

        // get local data
        ReadRecord localData = new ReadRecord(this.directory);
        Map<String, GenshinPlayerData> local = localData.readRecords();

        // merge checker function
        ArrayList<GachaRecord> newList = mergeData(infoRecord.getUid(), bodyList, local);

        // add uid -> genshin player data
        map.put(infoRecord.getUid(), new GenshinPlayerData(infoRecord, newList));
        return map;
    }


    /** Checks if the imported data needs to be merged by comparing the UID with local data's
     *  UIDs.
     * @param newUID import data uid in string
     * @param newBodyList import gacha records array list
     * @param local local data as map (uid -> {@link GenshinPlayerData})
     * @return a newly merged data (original list if no same uid detected)
     */
    private ArrayList<GachaRecord> mergeData(String newUID, ArrayList<GachaRecord> newBodyList, Map<String, GenshinPlayerData> local) {
        // duplicate uid check
        if(!MergeHelper.isDuplicate(newUID, local.keySet().stream().toList())) {// stream() api convert key set to list
            return newBodyList;
        }

        // retrieve old data
        List<GachaRecord> localDataBodyList = MergeHelper.getOldData(newUID, local);

        // get merged data
        List<GachaRecord> mergedData = MergeHelper.mergeData(newBodyList, localDataBodyList);
        mergedData.sort(Comparator.comparing(GachaRecord::getTime));// sort again to ensure the ascending time
        return new ArrayList<>(mergedData);
    }

    /** Validates UID, ensuring it is not null or blank.
     *
     * @param rd {@link InfoRecord} information of the user
     * @throws MissingComponentException exception if the info node is missing
     */
    private void validateUID (InfoRecord rd) throws MissingComponentException {
        if(rd.getUid() == null || rd.getUid().isBlank()){
            throw new MissingComponentException("UID is missing from the importing JSON file!");
        }
    }

    /** Validate each record in the body list, ensuring no parameters are missing.
     *
     * @param bodyList array list of input data
     * @throws EmptyListException exception thrown when th list node is empty
     * @throws MissingComponentException exception when the component is missing
     */
    private void validateBodyList(ArrayList<GachaRecord> bodyList) throws EmptyListException, MissingComponentException {
        // check if the body list node is empty
        if (bodyList == null || bodyList.isEmpty()) {
            throw new EmptyListException("No records are in the list! Please check your JSON file!");
        }

        // stream() api and "::" for shorthand lambda expression -> convert each record by calling toString(), then call the helper function to check
        List<String> invalidRecord = bodyList.stream().filter(this::isRecordIncomplete).map(GachaRecord::toString).toList();

        if(!invalidRecord.isEmpty()){
            throw new MissingComponentException("Incompletion record: " + invalidRecord + "\n");
        }
    }

    /** Checks if any parameters of a record are missing.
     *
     * @param rd {@link GachaRecord} gacha record class of a pity
     * @return true/false if the record is complete
     */
    private boolean isRecordIncomplete(GachaRecord rd) {
        Set<Integer> rank_type = Set.of(3, 4, 5);// valid item rank tier
        return isBlankOrNull(rd.getName())
                || isBlankOrNull(rd.getGacha_type())
                || isBlankOrNull(rd.getItem_id())
                || isBlankOrNull(rd.getTime())
                || isBlankOrNull(rd.getId())
                || !rank_type.contains(rd.getRank_type());
    }

    /** A helper function checks if the input string is null or blank.
     *
     * @param string input string
     * @return true/false based on if the string is null or empty
     */
    private boolean isBlankOrNull(String string) {
        return string == null || string.isBlank();
    }

    // exception when the path is empty
    public static class EmptyPathException extends Exception {
        public EmptyPathException(String message) {
            super(message);
        }
    }

    // list node is empty, meaning no data is contained
    public static class EmptyListException extends Exception {
        public EmptyListException(String message) {super(message);}
    }

    // component is missing in the data
    public static class MissingComponentException extends Exception {
        public MissingComponentException(String message) {super(message);}
    }

}// end class
