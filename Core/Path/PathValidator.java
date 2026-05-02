package Core.Path;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.Queue;

/**
 * An immutable record class that validates the imported path.
 * @param dataFile private object for storing the path
 * @course Teacher: Daniel Vriesinga
 * @author Frank Fan at 2026/04/27
 */
public record PathValidator(Path dataFile) {// start class

    /**
        This method validates the path by calling other helper methods.

      * @return Queue<Path> a queue wrapping the path inside
        @throws IOException - attempting to open/get type of the file
        @throws PathException - throw when the path is invalid
     */
    public Queue<Path> getPathQueue() throws IOException, PathException {
        Queue<Path> pathQueue = new LinkedList<>();

        // see if the path is valid and correctly formatted
        if (!isPathValid(this.dataFile)) {
            throw new PathException("Invalid path: " + this.dataFile.toString());
        }
        if (!isFileJSON(this.dataFile)) {
            throw new PathException("This path does not provide a JSON file: " + this.dataFile.toString());
        }

        // return the apth
        pathQueue.add(dataFile);
        return pathQueue;
    }

    /** Checks if the file existed and readable from the provided path.
     *
     * @param dataFile the path of data file
     * @return Boolean true/false if the file exist and is readable
     */
    private boolean isPathValid(Path dataFile) {
        return Files.exists(dataFile) && Files.isReadable(dataFile);
    }

    /** Checks if the data file is a JSON file.
     *
     * @param dataFile the path of data file
     * @return Boolean true/false based on if the file is JSON
     * @throws IOException checked when trying to probe the content of the file
     */
    private boolean isFileJSON(Path dataFile) throws IOException {
        String contentType = Files.probeContentType(dataFile);
        if (contentType == null) {
            return dataFile.toString().toLowerCase().endsWith(".json");
        }

        if (contentType.isBlank()) {
            throw new IOException("Could not determine content type for: " + dataFile);
        }

        return contentType.equalsIgnoreCase("application/json"); // ← also fix this (see below)
    }

    /**
     * Customized exception for handling path conversion error
     */
    public static class PathException extends Exception {
        public PathException(String message) {
            super(message);
        }
    }

}// end class
