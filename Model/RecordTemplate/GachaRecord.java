package Model.RecordTemplate;
/*
    Written by Frank Fan
    Teacher: Daniel Vriesinga
    Date: 2026/04/09

    This is the template of each pity, including necessary objects for each wish to
    be distinguished by the algorithm.
 */


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import Core.Interfaces.Mergeable;

/**
 * A record class acts like an immutable object for each gacha record, as each pity should never
 * change after the record in imported
 * Using third-party library Jackson to parse input data.
 *
 * @param gacha_type banner code of gacha type (e.g. 301, 400)
 * @param time time of this pity (hh:mm:ss)
 * @param name name of the item (either zh or en)
 * @param item_type item type in string
 * @param item_id item id in string
 * @param rank_type rank type of the item (3, 4, or 5)
 * @param id special ID of the pity, differ to UID and item ID
 * @param uigf_gacha_type character banner -> 301, 400; weapon -> 302; others are the same as the gacha type
 *
 * @interface Mergeable interface contracting the gacha record
 *
 * Resource: <a href="https://stackoverflow.com/questions/37722152/set-jackson-objectmapper-class-not-to-use-scientific-notation-for-double">...</a>
 * <a href="https://www.baeldung.com/jackson-annotations">...</a>
 */
public record GachaRecord (String gacha_type, String time, String name, String item_type, String item_id, int rank_type,
                          String id, String uigf_gacha_type) implements Mergeable  {// start class

    /** we have a constructor to store all data, fine tuning the deserialization process
     *
     * @param gacha_type banner code of gacha type (e.g. 301, 400)
     * @param time time of this pity (hh:mm:ss)
     * @param name name of the item (either zh or en)
     * @param item_type item type in string
     * @param item_id item id in string
     * @param rank_type rank type of the item (3, 4, or 5)
     * @param id special ID of the pity, differ to UID and item ID
     * @param uigf_gacha_type character banner -> 301, 400; weapon -> 302; others are the same as the gacha type
     *
     */
    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public GachaRecord(@JsonProperty("gacha_type") String gacha_type, @JsonProperty("time") String time, @JsonProperty("name") String name,
                       @JsonProperty("item_type") String item_type, @JsonProperty("item_id") String item_id, @JsonProperty("rank_type") int rank_type,
                       @JsonProperty("id") String id, @JsonProperty("uigf_gacha_type") String uigf_gacha_type) {

        this.gacha_type = gacha_type;
        this.time = time;
        this.name = name;
        this.item_type = item_type;
        this.item_id = item_id;
        this.rank_type = rank_type;
        this.id = id;
        this.uigf_gacha_type = uigf_gacha_type;

    }

    // Jackson getter annotation as a part of JsonProperty, allowing the object to serialize to JSON again
    @JsonGetter("gacha_type")
    public String getGacha_type() {return this.gacha_type;}
    @JsonGetter("time")
    public String getTime() {return this.time;}
    @JsonGetter("name")
    public String getName() {return this.name;}
    @JsonGetter("item_type")
    public String getItem_type() {return this.item_type;}
    @JsonGetter("item_id")
    public String getItem_id() {return this.item_id;}
    @JsonGetter("rank_type")
    public int getRank_type() {return this.rank_type;}
    @JsonGetter("id")
    public String getId() {return this.id;}
    @JsonGetter("uigf_gacha_type")
    public String getUigf_gacha_type() {return this.uigf_gacha_type;}

    // in pretty format, overriding toString()
    @Override
    public String toString() {
        return this.gacha_type + " " + this.time + " " + this.name + " " + this.item_type +
                " " + this.item_id + " " + this.rank_type + " " + this.id + " " + this.uigf_gacha_type;
    }

    // implementing interface abstract methods, used for generic method
    @Override
    public String Time(){return this.time;}
    @Override
    public String ID(){return this.id;}
}// end class
