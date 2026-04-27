package core.Genshin;

import Model.GachaRecord;
import Model.InfoRecord;

import java.util.ArrayList;

public record GenshinPlayerData(InfoRecord info, ArrayList<GachaRecord> records){
    public InfoRecord getInfo() {return info;}
    public ArrayList<GachaRecord> getRecords() {return records;}


}
