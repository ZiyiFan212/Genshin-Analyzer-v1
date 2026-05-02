package Model.RecordTemplate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * A record class acting liking an immutable object to store information record
 *
 * @param uid UID in string
 * @param lang file language in string
 * @param export_time export time in pretty format
 * @param export_timestamp timestamp in string
 * @param export_app -
 * @param export_app_version -
 * @param uigf_version banner type code
 * @param region_time_zone time zome code based on import data
 *
 * Resource: <a href="https://stackoverflow.com/questions/37722152/set-jackson-objectmapper-class-not-to-use-scientific-notation-for-double">...</a>
 * <a href="https://sigpwned.com/2024/09/22/generating-java-record-classes-with-jackson-annotations-to-map-json-using-chatgpt/">...</a>
 */
public record InfoRecord(String uid, String lang, String export_time, long export_timestamp, String export_app,
                         String export_app_version, String uigf_version, int region_time_zone) {

    // parametric constructor to store the info of importing record
    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public InfoRecord(@JsonProperty("uid") String uid, @JsonProperty("lang") String lang, @JsonProperty("export_time") String export_time,
                      @JsonProperty("export_timestamp") long export_timestamp, @JsonProperty("export_app") String export_app, @JsonProperty("export_app_version") String export_app_version,
                      @JsonProperty("uigf_version") String uigf_version, @JsonProperty("region_time_zone") int region_time_zone) {
        this.uid = uid;
        this.lang = lang;
        this.export_time = export_time;
        this.export_timestamp = export_timestamp;
        this.export_app = export_app;
        this.export_app_version = export_app_version;
        this.uigf_version = uigf_version;
        this.region_time_zone = region_time_zone;

    }

    // JSON getter annotation is used to serialize objects to JSON
    @JsonGetter("uid")
    public String getUid() {return this.uid;}
    @JsonGetter("lang")
    public String getLang() {return this.lang;}
    @JsonGetter("export_time")
    public String getExport_time() {return this.export_time;}
    @JsonGetter("export_timestamp")
    public long getExport_timestamp() {return this.export_timestamp;}
    @JsonGetter("export_app")
    public String getExport_app() { return this.export_app; }
    @JsonGetter("export_app_version")
    public String getExport_app_version() {return this.export_app_version;}
    @JsonGetter("uigf_version")
    public String getUigf_version() {return this.uigf_version;}
    @JsonGetter("region_time_zone")
    public int getRegion_time_zone() {return this.region_time_zone;}

    // override toString() for string concatenation
    @Override
    public String toString() {
        return this.uid + " " + this.lang + " " + this.export_time + " " + this.export_timestamp + " " + this.export_app + " " +
                this.export_app_version + " " + this.uigf_version + " " + this.region_time_zone;
    }

    /**
     * Return a new {@link InfoRecord} object because it is immutable record class
     * Time is overridden based on the time saving is triggered
     * @return InfoRecord as a new info
     */
    public InfoRecord withCurrentTime(){
        // format the local time with the timezone
        LocalDateTime now = LocalDateTime.now();
        String formattedTime = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        long timestamp = now.atZone(ZoneId.systemDefault()).toEpochSecond();

        // return a NEW instance with updated time but same UID/Lang
        return new InfoRecord(this.uid, this.lang, formattedTime, timestamp,
                this.export_app, this.export_app_version, this.uigf_version, this.region_time_zone);

    }
}
