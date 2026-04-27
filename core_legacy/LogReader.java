package core_legacy;
/*
    Written by: FF
    Date: 2026/4/10
    Teacher: Daniel Vriesinga
    This class gets the user home path, then tries to locate the in-game log file.
    If the log's path is found, it is stored and can be used for finding the authKey.
 */
/*
    @resources:
    https://deepwiki.com/biuuu/genshin-wish-export/2.1-game-log-reading
    https://stackoverflow.com/questions/17552299/how-to-get-the-path-string-from-a-java-nio-path
 */

import java.nio.file.*;
import java.util.*;

public class LogReader {// start class

    /*
     * A map that corresponds the log's path to different servers.
     * These path are predefined in the game file.
     */
    private static final Map<GameType, String> LOG_PATH;
    static {
        LOG_PATH = new HashMap<>();
        LOG_PATH.put(GameType.CHINESE,"AppData/LocalLow/miHoYo/原神/output_log.txt");
        LOG_PATH.put(GameType.GLOBAL,"AppData/LocalLow/miHoYo/Genshin Impact/output_log.txt");
        LOG_PATH.put(GameType.CLOUD,"AppData/Local/miHoYo/GenshinImpactCloudGame/config/logs/MiHoYoSDK.log");
    }

    /*
     * We determine the type of the game by checking if different servers' log exist.
     * If it does, we add the server name (an enum object) to the map.
     *
     * @param: -
     * @return: An arraylist with the name of the user's server type
     * @throw: -
     */
    private ArrayList<String> getGame_type(){
        // we use an arraylist to store the server type (CN, EN, or cloud)
        ArrayList<String> game_type = new ArrayList<>();
        Path userHome = Path.of(getUserPath());

        // iterate through every entry in the map
        for (Map.Entry<GameType, String> entry : LOG_PATH.entrySet()) {
            Path fullPath = userHome.resolve(entry.getValue());// joint the home path and log path in the game file
            if (Files.exists(fullPath)) {
                game_type.add(entry.getKey().toString());// if this log exists, add the server name to the list
            }
        }
        return game_type;
    }

    /*
     * It compares the server type with every key value in the map,
     * then wrap the corresponding log path into a queue.
     *
     * @param: -
     * @return: A queue that includes the user's complete log path
     * @throw: -
     */
    public Queue<Path> getPath(){
        Queue<Path> allPath = new LinkedList<>();
        Path userHome = Path.of(getUserPath());

        for (Map.Entry<GameType, String> entry : LOG_PATH.entrySet()) {
            // if the server type == a key in the map, we add its value to the queue
            if (getGame_type().contains(entry.getKey().toString())) {
                allPath.add(userHome.resolve(entry.getValue()));
            }
        }

        return allPath;
    }

    // a helper function to get the user's home path, return it in String
    private String getUserPath(){
        // return the user home path in string (e.g. C://userName...)
        return Paths.get(System.getProperty("user.home")).toString();
    }

    // an enum class to define different server type
    private enum GameType {
        CHINESE,    // -> CN server
        GLOBAL,     // -> EN server
        CLOUD;      // -> cloud server
    }

}// end class
