package core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.Queue;

/**
 * @param dataFile private object for storing the path
 */
public record PathValidator(Path dataFile) {// start class

    /*
        This functions validates the path

        @Param: a data file path
        @Return: a queue with the type of path
        @Throw: IOException - attempting to open/get type of the file
     */
    public Queue<Path> getPathQueue() throws IOException, PathException {
        Queue<Path> pathQueue = new LinkedList<>();

        // validate the path
        if (!isPathValid(this.dataFile)) {
            throw new PathException("Invalid path: " + this.dataFile.toString());
        }
        if (!isFileJSON(this.dataFile)) {
            throw new PathException("This path does not provide a JSON file: " + this.dataFile.toString());
        }

        pathQueue.add(dataFile);
        return pathQueue;
    }

    // return if the file exists and is readable
    private boolean isPathValid(Path dataFile) {
        return Files.exists(dataFile) && Files.isReadable(dataFile);
    }

    // return if the data file is JSON
    private boolean isFileJSON(Path dataFile) throws IOException {
        String contentType = Files.probeContentType(dataFile);
        if (contentType == null) {
            return toString().toLowerCase().endsWith(".json");
        }

        if (contentType.isBlank()) {
            throw new IOException("Could not determine content type for: " + dataFile);
        }

        return contentType.equalsIgnoreCase("application/json"); // ← also fix this (see below)
    }

    public static class PathException extends Exception {
        public PathException(String message) {
            super(message);
        }
    }

}// end class
