package Storage.ExcelExport;

import Assets.Resources.AssetsManager;
import Model.RecordTemplate.GachaRecord;
import Model.RecordTemplate.InfoRecord;
import Storage.Configuration.StorageConfig;
import Core.Genshin.GenshinPlayerData;
import I18n.General.GeneralMessageManager;
import I18n.items.ItemTranslationManager;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Teacher: Daniel Vriesinga
 * Frank Fan at 2026/04/24
 *
 * This class uses third-party library (Apache POI) to export gacha data into an Excel file
 */
public class ExcelExporter {

    // string array of banner type number
    static final String[] bannerArr = {"banner.301", "banner.400", "banner.302", "banner.500", "banner.200", "banner.100"};
    static final String[] bannerCodes = {"301", "400", "302", "500", "200", "100"};

    /**
     * This method is the main body of exporting into an Excel file, saving to local directory.
     *
     * @param genshinPlayerData player's data with both info and data list {@link GenshinPlayerData}
     * @param lang current langauage in string, either 'zh' or 'en'
     * @throws IOException caught in writing to Excel
     *
     * Resource: <a href="https://dev.to/sadiul_hakim/comprehensive-apache-poi-tutorial-excel-file-handling-in-java-7mf">...</a>
     */
    public static void exportExcel(GenshinPlayerData genshinPlayerData, String lang) throws IOException {
        // get the data list and the UID
        ArrayList<GachaRecord> records = new ArrayList<>(genshinPlayerData.records());
        InfoRecord info = genshinPlayerData.info();
        String uid = info.getUid();

        // Export newest first for easier recent-history review.
        // comparator with lambda usage
        records.sort((a, b) -> {
            LocalDateTime ta = parseTime(a.getTime());
            LocalDateTime tb = parseTime(b.getTime());

            // compare the time
            int byTime = tb.compareTo(ta);
            if (byTime != 0) return byTime;

            // compare the ID if the time is exactly the same
            return compareId(b.getId(), a.getId());
        });

        // don't explode the computer, we create a cache map
        Map<String, Integer> imageCache = new HashMap<>();

        // export logic
        try(Workbook wb = new XSSFWorkbook()){
            XSSFFont purpleFont = (XSSFFont) wb.createFont();
            purpleFont.setColor(IndexedColors.VIOLET.getIndex());

            XSSFFont goldenFont = (XSSFFont) wb.createFont();
            goldenFont.setColor(IndexedColors.GOLD.getIndex());

            // iterate through all banners, one per sheet
            for(int i = 0; i < bannerArr.length; i++) {
                Sheet sheet = wb.createSheet(GeneralMessageManager.get(bannerArr[i]));
                Drawing<?> drawing = sheet.createDrawingPatriarch();// drawing container for shapes and images

                // make icon not looks like compressed
                sheet.setDefaultRowHeightInPoints(34);
                sheet.setColumnWidth(0, 6 * 256);
                sheet.setColumnWidth(1, 28 * 256);
                sheet.setColumnWidth(2, 22 * 256);
                sheet.setColumnWidth(3, 18 * 256);
                sheet.setColumnWidth(4, 10 * 256);
                sheet.setColumnWidth(5, 10 * 256);

                // create the header by default language
                Row headerRow = sheet.createRow(0);
                headerRow.createCell(0).setCellValue(GeneralMessageManager.get("header.icon"));
                headerRow.createCell(1).setCellValue(GeneralMessageManager.get("header.name"));
                headerRow.createCell(2).setCellValue(GeneralMessageManager.get("header.time"));
                headerRow.createCell(3).setCellValue(GeneralMessageManager.get("header.type"));
                headerRow.createCell(4).setCellValue(GeneralMessageManager.get("header.rarity"));
                headerRow.createCell(5).setCellValue(GeneralMessageManager.get("header.pity"));

                int rowNum = 1;
                // iterate through the list
                for (GachaRecord rd : records) {
                    // not belongs to this banner, get out!
                    if(!rd.getGacha_type().equals(bannerCodes[i])) continue;

                    Row row = sheet.createRow(rowNum);

                    // add Icon to the Excel with a helper function
                    addIconToCell(wb, drawing, rd.getItem_id(), rowNum, imageCache);

                    // fix: localized string
                    String localizedName = ItemTranslationManager.returnName(rd.getItem_id(), lang);
                    String str = rd.getItem_type(); // could be "角色" or "Character", which crashes the manager if treated as a key
                    String localizedItemType = GeneralMessageManager.getLocalizedType(str);

                    // insert data to the column
                    int rank = rd.getRank_type();
                    if (rank != 3){
                        row.createCell(1).setCellValue(colorfulText(rank, localizedName, purpleFont, goldenFont)); // rich color used
                        row.createCell(2).setCellValue(colorfulText(rank, rd.getTime(), purpleFont, goldenFont));
                        row.createCell(3).setCellValue(colorfulText(rank, localizedItemType, purpleFont, goldenFont));
                        row.createCell(4).setCellValue(colorfulText(rank, String.valueOf(rank), purpleFont, goldenFont));
                    } else {
                        row.createCell(1).setCellValue(localizedName);// no rich text and color
                        row.createCell(2).setCellValue(rd.getTime());
                        row.createCell(3).setCellValue(localizedItemType);
                        row.createCell(4).setCellValue(String.valueOf(rank));
                    }

                    // don't paint this!!
                    row.createCell(5).setCellValue(rowNum);

                    rowNum++;
                }
            }

            // write to file, try with resource deployed.
            String fileName = GeneralMessageManager.get("storage.excel.title") + uid + " " + localTime() + ".xlsx";
            Path outputFile = StorageConfig.excelPath.resolve(fileName);
            try (FileOutputStream fileOut = new FileOutputStream(outputFile.toFile())) {
                wb.write(fileOut);
            }
        }

    }

    // https://stackoverflow.com/questions/28238078/insert-image-in-column-to-excel-using-apache-poi
    /**
     * This helper class simply add the item's icon to the cell
     * @param workbook primary container for excel
     * @param drawing drawing interface for inserting shapes/icons
     * @param itemId item ID in string
     * @param rowNum number of row when the function is called
     * @param cache cache map see if any images are stored there already
     */
    private static void addIconToCell(Workbook workbook, Drawing<?> drawing, String itemId, int rowNum, Map<String, Integer> cache) {
        try {
            boolean haveIcon = cache.containsKey(itemId);
            int pictureIdx = -1;

            // if the icon is used, retrieve from the map
            if (haveIcon) {
                pictureIdx = cache.get(itemId);
            } else {
                // get path from the AssetsManager
                String resourcePath = AssetsManager.getIconPath(itemId);
                InputStream is = ExcelExporter.class.getResourceAsStream(resourcePath);

                // did retrieve the path, paint and add it to the cache map
                // resource -> https://stackoverflow.com/questions/3211156/how-to-convert-image-to-byte-array-in-java
                if (is != null) {
                    try(is){
                        byte[] bytes = IOUtils.toByteArray(is);
                        pictureIdx = workbook.addPicture(bytes, Workbook.PICTURE_TYPE_PNG);
                        cache.put(itemId, pictureIdx);// store the icon
                        haveIcon = true;
                    }
                }
            }

            // helpers to position and size images
            CreationHelper helper = workbook.getCreationHelper();
            ClientAnchor anchor = helper.createClientAnchor();

            // icon stays in column 0
            anchor.setCol1(0);
            anchor.setRow1(rowNum);
            anchor.setCol2(1);
            anchor.setRow2(rowNum + 1);
            // set x-coordinate
            anchor.setDx1(20000);
            anchor.setDy1(20000);
            anchor.setDx2(-20000);
            anchor.setDy2(-20000);

            // create a picture with position and picture byte arr
            drawing.createPicture(anchor, pictureIdx);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    /** return the colorful text based on different colors
     *
     * @param rankType item rank type. In this case must be rank 4 and 5
     * @param string text string
     * @param purple purple font
     * @param gold golden font (used in workbook)
     * @return a colorful rich text string
     */
    private static XSSFRichTextString colorfulText(int rankType, String string, XSSFFont purple, XSSFFont gold) {
        XSSFRichTextString richString = new XSSFRichTextString(string);

        // type check, deciding color
        if (rankType == 4) {
            richString.applyFont(0, string.length(), purple);
        } else if (rankType == 5) {
            richString.applyFont(0, string.length(), gold);
        }

        return richString;
    }

    /** a helper function to get the local time in pretty format
     *
     * @return the string of the local time in a pretty format
     */
    private static String localTime(){
        LocalDateTime now = LocalDateTime.now();

        // define a "pretty" pattern to format the time
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
        return now.format(formatter);
    }

    /**
     * A helper to convert string local time to actual {@link LocalDateTime} object
     * @param time input string time
     * @return LocalDateTime being converted
     */
    private static LocalDateTime parseTime(String time) {
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return LocalDateTime.parse(time, df);
    }

    /**
     * ID comparator used when the time are exactly the same, in rare case
     * @param a string ID on the left
     * @param b String ID on the right
     * @return
     */
    private static int compareId(String a, String b) {
        // handle extremely rare case...
        if (a == null && b == null) return 0;
        if (a == null) return -1;
        if (b == null) return 1;

        // another extremely rare case...
        if (a.length() != b.length()) return Integer.compare(a.length(), b.length());
        return a.compareTo(b);// return the compared result
    }
}

