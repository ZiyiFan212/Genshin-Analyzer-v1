package core_legacy;
/*
    Written by: FF
    Date: 2026/4/10
    Teacher: Daniel Vriesinga
    This class gets the complete log path from LogReader.java, then regex to find the authKey.
    AuthKey is stored with private modifier. Validator.java is called to validate extracted API key.
 */
/*
    @resources:
    https://deepwiki.com/biuuu/genshin-wish-export/2.1-game-log-reading
    https://www.w3schools.com/java/java_regex.asp
 */


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class URLgetter {// start class

    // we declare two private objects to store the log's path and the authkey
    private String url;
    private Path LOG_PATH;


    // since the authkey is private, we need a getter for other classes to use
    public String getUrl(){
        return this.url;
    }

    // the setter allows the user to manually input the authkey that is generated from
    // other similar program (e.g. Paimon-moe, genshin-wish-export, etc.)
    public void setUrl(String url){
        this.url = url;
    }

    /*
        This function calls createPath to get the log path, then reads and stores all log contents in a string.
        Regex then search it to find the cache file, which contains the authKey.

        @param: -
        @return: the path of the cache file
        @throw: LogNotFoundException -> error presented in getting/reading log
                IOException
     */
    private Path getGameDataPath() throws LogNotFoundException, IOException {
        createPath();// get the log's path

        // check if the path is null
        if(this.LOG_PATH == null){
            throw new LogNotFoundException("The log path is null! Please check if the game is insatlled!");
        }

        // check if the path points to the existed log
        if(!this.LOG_PATH.toFile().exists()){
            throw new LogNotFoundException("The log path does not exist!");
        }

        // check if the log is readable
        boolean readable = Files.isReadable(this.LOG_PATH);
        if(!readable){ throw new LogNotFoundException("The log is not readable!"); }

        // validation passed. Now we read and store the everything from the log
        String contents = Files.readString(this.LOG_PATH);

        // compile the regex pattern
        Pattern gamePathPattern = Pattern.compile("\\w:\\/.+(GenshinImpact_Data|YuanShen_Data)");
        // scan to find the matching expression
        Matcher matcher = gamePathPattern.matcher(contents);
        if(matcher.find()){
            String gamePath = matcher.group(0);
            // now we joint the cache file's path
            return Path.of(gamePath + "/webCaches/Cache/Cache_Data/data_2");
        }

        throw new LogNotFoundException("Game data path not found in log!");
    }


    /*
        This function reads the cache file to find the raw URL that contains the authKey.
        Calling a helper class to validate this authKey.

        @param: -
        @return: a valid authKey that can be used for requesting
        @throw: LogNotFoundException -> error presented in log processing
                FailedConnectionException -> fail validating the authkey. Reason: expired or incorrect authkey
                IOException
     */
    public String getAuthKeyUrl() throws IOException, LogNotFoundException,
                URLValidator.FailedConnectionException, ServerRegionMismatchException, TooFrequentRequestException,
                        InvalidAuthKeyException, AuthKeyExpiredException {

        // get the cache file's path and read its content
        Path cachePath = getGameDataPath();
        String cacheContents = Files.readString(cachePath);

        // compile the authkey regex pattern
        Pattern authkeyPattern = Pattern.compile("https.+?auth_appid=webview_gacha.+?authkey=.+?game_biz=hk4e_\\w+");
        Matcher matcher = authkeyPattern.matcher(cacheContents);

        // find all matches and return the last one, which is also the latest one
        String lastMatch = "";
        while (matcher.find()) {
            lastMatch = matcher.group(0);
        }
        // throw exception if we can't find the authkey
        if(lastMatch.isEmpty()){ throw new LogNotFoundException("The auth key is not found from the game's cache!"); }

        // call the validator to see if this authkey is valid
        URLValidator validator = new URLValidator();

        Map<String, Integer> result = validator.liveValidation(lastMatch);

        int retCode = result.entrySet().iterator().next().getValue();
        switch(retCode){
            case 0:
                this.url = result.entrySet().iterator().next().getKey();// get the authKey and set it
                return this.url;

            case -100:
                throw new InvalidAuthKeyException("Invalid key is invalid. Please try to get the URL again!");

            case -101:
                throw new AuthKeyExpiredException("The AuthKey is expired. Please re-open the wish history and attempt again!");

            case -5:
                throw new TooFrequentRequestException("Requesting too many times! Please try again later!");

            case -201:
                throw new ServerRegionMismatchException("The region is mismatch. Please check the URL");

            default:
                throw new RuntimeException("Unsupported error!");
        }

    }


    // get the path from the loader and can be used for other methods
    private void createPath(){
        if (this.LOG_PATH != null) return;// guard it when trying to poll something again
        LogReader logReader = new LogReader();
        Queue<Path> paths = logReader.getPath();
        this.LOG_PATH = paths.poll();// set the log path
    }

    // customized exception, explicitly handling log process
    public static class LogNotFoundException extends Exception {
        public LogNotFoundException(String message){
            super(message);
        }
    }

    // specific exception to handle different cases
    public static class AuthKeyExpiredException extends Exception {
        public AuthKeyExpiredException(String message){
            super(message);
        }
    }

    public static class InvalidAuthKeyException extends Exception {
        public InvalidAuthKeyException(String message){
            super(message);
        }
    }

    public static class ServerRegionMismatchException extends Exception {
        public ServerRegionMismatchException(String message){
            super(message);
        }
    }

    public static class TooFrequentRequestException extends Exception {
        public TooFrequentRequestException(String message){
            super(message);
        }
    }


}// end class
