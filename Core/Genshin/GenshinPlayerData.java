package Core.Genshin;

import Model.RecordTemplate.GachaRecord;
import Model.RecordTemplate.InfoRecord;

import java.util.ArrayList;

/**
 * @course: Teacher: Daniel Vriesinga
 * @author: Frank Fan at 2026/04/26
 * <p></p>A record class acts like a container of the user information and the record list
 */
public record GenshinPlayerData(InfoRecord info, ArrayList<GachaRecord> records){}
