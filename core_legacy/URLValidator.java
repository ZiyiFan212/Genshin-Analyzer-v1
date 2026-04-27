package core_legacy;
/*
    Written by: FF
    Date: 2026/4/10
    Teacher: Daniel Vriesinga
    This class receives the raw URL that contains the authKey. Then, fixing it and attempting a testing request
    to the server.
 */
/*
    @resources:
    https://deepwiki.com/biuuu/genshin-wish-export/2.1-game-log-reading
    https://www.w3schools.com/java/java_regex.asp
    https://stackoverflow.com/questions/1359689/how-to-send-http-request-in-java
    https://dev.to/sadiul_hakim/jackson-tutorial-comprehensive-guide-with-examples-2gdj
    reminder: Jackson and its relative JAR is imported for parsing JSON.
 */

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


public class URLValidator {// start class

    /*
     * Check if the authkey contains "=" to ensure it is unencoded.
     * Check if th authkey contains "%" to ensure it is not already properly encoded
     * We encoded later through a function
     *
     * @param: a raw URL in string
     * @return a URL in string after fixing, or the parameter because it is already encoded.
     * @throw: -
     */
    private String fixAuthKey(String rawUrl) {
        // contains these characters, we need to encode it
        if(rawUrl.chars().anyMatch(c -> c == '=') && rawUrl.chars().noneMatch(c -> c == '%')){
            String authKeyValue = rawUrl.replaceFirst(".*authkey=([^&]+).*", "$1");
            // encode it for HTTP request
            return rawUrl.replaceFirst("authkey=[^&]+", "authkey=" + URLEncoder.encode(authKeyValue, StandardCharsets.UTF_8));
        }
        return rawUrl;// encoded already, return the original
    }

    /*
     * This function simply checks the domain of the URL.
     * Returning different server's domain name based on the host name
     *
     * @param: a raw URL in string
     * @return: the domain name in string
     * @throw: MalformedURLException -> malformed URL is presented
     */
    private String getDomain(String rawURL) throws MalformedURLException {
        String fixedUrl = fixAuthKey(rawURL);// encode it first

        // URl class to find the domain name
        URL url = new URL(fixedUrl);
        String host = url.getHost();

        // check the URL's domain name to identify the server type
        if (host.contains("webstatic-sea") ||
                host.contains("hk4e-api-os") ||
                host.contains("hoyoverse.com")) {
            return "https://public-operation-hk4e-sg.hoyoverse.com";// -> global server
        } else {
            return "https://public-operation-hk4e.mihoyo.com";// -> CN server only
        }
    }

    /*
     * This function get the authKey and send a testing request to the server.
     *
     * @param: rawURL in string
     * @return: a map that has a key to store the authKey in String, and a boolean value to indicate if it is valid
     * @throw: FailedConnectionException -> threw when failed to connect to the server, often due to expired or incorrect authkey
     *         IOException
     */
    public Map<String, Integer> liveValidation(String rawUrl) throws IOException, FailedConnectionException {
        Map<String, Integer> result = new LinkedHashMap<>();
        String apiUrl = apiUrlExtractor(rawUrl);

        try (HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .build();){
            // create a HTTP instance in try-with-resources


            // create a request (we must have pseudo UA because the server will reject the request without it)
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("User-Agent", 
                            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120 Safari/537.36")
                    .header("Referer", "https://webstatic.mihoyo.com/")
                    .GET()
                    .build();
            

            // send a request
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                // parse the JSON response
                ObjectMapper mapper = new ObjectMapper();
                JsonNode jsonNode = mapper.readTree(response.body());

                // check the retcode, return true when it is 0
                int retCode = jsonNode.get("retcode").asInt();
                result.put(apiUrl, retCode);
                return result;
            }

            throw new FailedConnectionException("Server returns status: " + response.statusCode());
        } catch (Exception e) {
            // throw when the connection is failed
            throw new FailedConnectionException("This authkey could not connect to the server!");
        }

    }

    /*
        Process to have a correct authKey for validation. Concatenating the domain name and the url.

        @param: raw url in string
        @return: the authkey in string
        @throw: MalformedURLException -> error presented in URL processing
     */
    private String apiUrlExtractor(String rawUrl) throws MalformedURLException {
        String domain = getDomain(rawUrl);
        String testUrl = rawUrl.replaceFirst("https://[^/]+", domain);// regex to joint with a correct domain name


        // extract query parameters from testUrl
        // (testUrl contains: api/event/getGachaLog. We don't want that, and we want to change it to api/getGachaLog, which is the correct API endpoint for testing)
        String apiUrl = testUrl.replaceFirst("https://[^/]+/.*?\\?", domain + "/gacha_info/api/getGachaLog?");
        // ensure we have page and size parameters for the API
        if (!apiUrl.contains("page=")) {
            apiUrl += "&page=1&size=6";
        }
        return apiUrl;
    }

    // customized exception class that explicitly handles failed connection in validating
    public static class FailedConnectionException extends Exception {
        public FailedConnectionException(String message){
            super(message);
        }
    }

    // unused extraction logic
    /*
    private String extractAuthkey(String urlString) throws MalformedURLException {
        String fixedUrl = fixAuthKey(urlString);
        URL url = new URL(fixedUrl);
        String query = url.getQuery();

        if (query != null) {
            String[] params = query.split("&");
            for (String param : params) {
                if (param.startsWith("authkey=")) {
                    return param.substring(8); // Remove "authkey=" prefix
                }
            }
        }
        return "";
    }

     */
}// end class
