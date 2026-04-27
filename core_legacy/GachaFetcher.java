package core_legacy;

import RecordTemplate.GachaRecord;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;

public class GachaFetcher {// start class

    private String apiUrl;

    // parameters used in requesting the record
    private static final int Page_Size = 20;
    private static final String[][] Banner_Type = {
            {"301", "Character Event Wish"},
            {"302", "Weapon Event Wish"},
            {"500", "Chronicled Wish"},
            {"200", "Standard Wish"},
            {"100", "Novice Wish"}
    };
    private static final int Page_Number = 1;

    // delay time constant in milliseconds
    private static final int Request_Delay = 300;

    // create jackson instance for parsing JSON
    private final ObjectMapper mapper;
    private final HttpClient client;

    // public constructor
    public GachaFetcher(){
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .build();
        this.mapper = new ObjectMapper();
    }

    // send request and parse all data
    public ArrayList<GachaRecord> getAllRecords(int Retry_Times) throws IOException, ServerConnectionError, URLgetter.AuthKeyExpiredException,
            URLgetter.ServerRegionMismatchException, URLgetter.InvalidAuthKeyException, URLgetter.TooFrequentRequestException,
            URLValidator.FailedConnectionException, URLgetter.LogNotFoundException {
        // set the API key
        setKeys();

        ArrayList<GachaRecord> gachaRecords = new ArrayList<>();
        // an integer tracking which banner causes freezing problem
        int currentBanner = 0;

        try {
            // iterating through all banners
            for (currentBanner = 0; currentBanner < Banner_Type.length; currentBanner++) {
                String type = Banner_Type[currentBanner][0];
                String name = Banner_Type[currentBanner][1];

                // fetch records for this banner type
                ArrayList<GachaRecord> currentBannerRecords = fetchSingleBanner(type, name, Retry_Times);
                gachaRecords.addAll(currentBannerRecords);
            }

            return gachaRecords;
        } catch (Exception e) {
            // try to request again
            if (Retry_Times > 0) {
                return getAllRecords(Retry_Times - 1);
            } else {
                throw new ServerConnectionError("Error occurred in reading the banner: " + Arrays.toString(Banner_Type[currentBanner]));
            }
        } finally {
            // close the HTTP client to prevent resources leak
            client.close();
        }
    }

    // fetch records for a single banner type
    private ArrayList<GachaRecord> fetchSingleBanner(String bannerType, String bannerName, int retryCount)
            throws IOException, InterruptedException, ServerConnectionError, APIKeyExpiredException {

        // since Hoyo's server requires ID, but never updates along with the JSON, we need to update it manually
        String endID = null;
        ArrayList<GachaRecord> currentBannerRecords = new ArrayList<>();

        try {
            // iterate until no more records
            while (true) {
                String requestUrl = processUrl(bannerType, endID);

                // create a request (we must have pseudo UA because the server will reject the request without it)
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(requestUrl))
                        .header("User-Agent",
                                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120 Safari/537.36")
                        .header("Referer", "https://webstatic.mihoyo.com/")
                        .GET()
                        .build();

                // receive the response
                HttpResponse<String> response = client.send(request,
                        HttpResponse.BodyHandlers.ofString());

                // since we already test the authKey previously, we don't guard it explicitly here
                if (response.statusCode() != 200) {
                    throw new ServerConnectionError("Error occurred in connecting with the server!");
                }

                JsonNode jsonNode = mapper.readTree(response.body());

                // check retcode (0 -> success; others -> failure)
                if (jsonNode.get("retcode").asInt() != 0) {
                    String message = jsonNode.get("message").asText();
                    if ("authkey timeout".equalsIgnoreCase(message)) {
                        throw new APIKeyExpiredException("User authentication expired");
                    }
                    throw new RuntimeException(message);
                }

                // get records and store them
                JsonNode data = jsonNode.get("data").get("List");
                ArrayList<GachaRecord> pageRecords = new ArrayList<>();

                if (data != null && data.isArray()) {
                    for (JsonNode item : data) {
                        GachaRecord records = new GachaRecord(
                                item.get("id").asText(),        // -> id    (each corresponds to the object in GachaRecord)
                                item.get("uid").asText(),       // -> uid
                                item.get("name").asText(),      // -> item_name
                                item.get("item_type").asText(), // -> item_type
                                item.get("rank_type").asInt(),  // -> rarity_level
                                item.get("gacha_type").asText(),// -> banner_type
                                item.get("time").asText()       // -> time
                        );
                        pageRecords.add(records);
                    }
                }

                // If no records returned, break the loop because there's no records anymore for the current banner
                if (pageRecords.isEmpty()) {
                    break;
                }

                // append this page to the current banner
                currentBannerRecords.addAll(pageRecords);

                // set endID for next page
                endID = pageRecords.getLast().getId();

                // avoid overhead
                Thread.sleep(Request_Delay);
            }

            return currentBannerRecords;

        } catch (APIKeyExpiredException e) {
            // if the API key is expired, requesting again is pointless
            throw e;
        } catch (Exception e) {
            if (retryCount > 0) {
                Thread.sleep(5000); // Wait before retry
                return fetchSingleBanner(bannerType, bannerName, retryCount - 1);
            } else {
                throw e;
            }
        }
    }

    // make new url for getting records from different banners
    private String processUrl (String bannerType, String endID){
        String url = apiUrl + "&gacha_type=" + bannerType + "&page=" + Page_Number +
                "&size=" + Page_Size;
        if (endID != null) {
            url += "&end_id=" + endID;
        }
        return url;
    }

    // get the URL with the authkey, regex it to extract valid API key
    private void setKeys () throws
            URLgetter.AuthKeyExpiredException, URLgetter.ServerRegionMismatchException, URLgetter.InvalidAuthKeyException,
            URLgetter.TooFrequentRequestException, URLValidator.FailedConnectionException, IOException, URLgetter.LogNotFoundException, ServerConnectionError {
        URLgetter urlgetter = new URLgetter();
        URL url = new URL(urlgetter.getAuthKeyUrl());

        // set the domain and query
        String apiDomain = url.getProtocol() + "://" + url.getHost();
        String queryString = url.getQuery();

        if (queryString.isEmpty()) {
            throw new ServerConnectionError("Failed to parse API domain or query from URL!");
        }

        this.apiUrl = apiDomain + "/event/gacha_info/api/getGachaLog?" + queryString;
    }



    // customized exception to handle error in requesting
    public static class ServerConnectionError extends Exception {
        public ServerConnectionError(String message){
            super(message);
        }
    }

    // handling api key exception when requesting the data
    public static class APIKeyExpiredException extends Exception {
        public APIKeyExpiredException(String message){
            super(message);
        }
    }

}// end class


