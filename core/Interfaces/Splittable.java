package core.Interfaces;

import Model.GachaRecord;
import Model.InfoRecord;

import java.util.List;

public interface Splittable {
    InfoRecord getInfo();
    List<GachaRecord> getRecords();
}
