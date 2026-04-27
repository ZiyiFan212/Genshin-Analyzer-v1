package Storage.ExcelExport;

import Assets.Resources.AssetsManager;
import Model.GachaRecord;
import Model.InfoRecord;
import Storage.StorageConfig;
import core.Genshin.GenshinPlayerData;
import i18n.General.GeneralMessageManager;
import i18n.items.ItemTranslationManager;
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
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class ExcelExporter {

    // string array of banner type number
    static final String[] bannerArr = {"banner.301", "banner.302", "banner.500", "banner.200", "banner.100"};
    static final String[] bannerCodes = {"301", "302", "500", "200", "100"};

    public static void exportExcel(GenshinPlayerData genshinPlayerData, String lang) throws IOException {
        ArrayList<GachaRecord> records = new ArrayList<>(genshinPlayerData.getRecords());
        InfoRecord info = genshinPlayerData.getInfo();
        String uid = info.getUid();

        // Export newest first for easier recent-history review.
        records.sort((a, b) -> {
            LocalDateTime ta = parseTime(a.getTime());
            LocalDateTime tb = parseTime(b.getTime());
            int byTime = tb.compareTo(ta);
            if (byTime != 0) return byTime;
            return compareId(b.getId(), a.getId());
        });

        // don't explode the computer
        Map<String, Integer> imageCache = new HashMap<>();

        // export logic
        try(Workbook wb = new XSSFWorkbook()){
            XSSFFont purpleFont = (XSSFFont) wb.createFont();
            purpleFont.setColor(IndexedColors.VIOLET.getIndex());

            XSSFFont goldenFont = (XSSFFont) wb.createFont();
            goldenFont.setColor(IndexedColors.GOLD.getIndex());


            for(int i = 0; i < bannerArr.length; i++) {
                Sheet sheet = wb.createSheet(GeneralMessageManager.get(bannerArr[i]));
                Drawing<?> drawing = sheet.createDrawingPatriarch();

                sheet.setDefaultRowHeightInPoints(34);
                // Keep icon cell closer to square to avoid "compressed" look.
                sheet.setColumnWidth(0, 6 * 256);
                sheet.setColumnWidth(1, 28 * 256);
                sheet.setColumnWidth(2, 22 * 256);
                sheet.setColumnWidth(3, 18 * 256);
                sheet.setColumnWidth(4, 10 * 256);
                sheet.setColumnWidth(5, 10 * 256);

                Row headerRow = sheet.createRow(0);
                headerRow.createCell(0).setCellValue(GeneralMessageManager.get("header.icon"));
                headerRow.createCell(1).setCellValue(GeneralMessageManager.get("header.name"));
                headerRow.createCell(2).setCellValue(GeneralMessageManager.get("header.time"));
                headerRow.createCell(3).setCellValue(GeneralMessageManager.get("header.type"));
                headerRow.createCell(4).setCellValue(GeneralMessageManager.get("header.rarity"));
                headerRow.createCell(5).setCellValue(GeneralMessageManager.get("header.pity"));

                int rowNum = 1;
                for (GachaRecord rd : records) {
                    // not this banner, gto
                    if(!rd.getGacha_type().equals(bannerCodes[i])) continue;

                    Row row = sheet.createRow(rowNum);

                    // add Icon to the Excel
                    addIconToCell(wb, drawing, sheet, rd.getItem_id(), rowNum, imageCache);

                    // localized string
                    String localizedName = ItemTranslationManager.returnName(rd.getItem_id(), lang);
                    String str = rd.getItem_type(); // Could be "角色" or "Character", which crashes the manager
                    String localizedItemType = GeneralMessageManager.getLocalizedType(str);

                    int rank = rd.getRank_type();
                    if (rank != 3){
                        row.createCell(1).setCellValue(colorfulText(rank, localizedName, purpleFont, goldenFont));
                        row.createCell(2).setCellValue(colorfulText(rank, rd.getTime(), purpleFont, goldenFont));
                        row.createCell(3).setCellValue(colorfulText(rank, localizedItemType, purpleFont, goldenFont));
                        row.createCell(4).setCellValue(colorfulText(rank, String.valueOf(rank), purpleFont, goldenFont));
                    } else {
                        row.createCell(1).setCellValue(localizedName);
                        row.createCell(2).setCellValue(rd.getTime());
                        row.createCell(3).setCellValue(localizedItemType);
                        row.createCell(4).setCellValue(String.valueOf(rank));
                    }


                    // don't paint this!!
                    row.createCell(5).setCellValue(rowNum);

                    rowNum++;
                }

            }

            // write to file
            String fileName = "Genshin-Analyzer " + uid + " " + localTime() + ".xlsx";
            Path outputFile = StorageConfig.excelPath.resolve(fileName);
            try (FileOutputStream fileOut = new FileOutputStream(outputFile.toFile())) {
                wb.write(fileOut);
            }
        }

    }

    // https://stackoverflow.com/questions/28238078/insert-image-in-column-to-excel-using-apache-poi
    private static void addIconToCell(Workbook workbook, Drawing<?> drawing, Sheet sheet, String itemId, int rowNum, Map<String, Integer> cache) {
        try {
            int pictureIdx = 0;

            // if the icon is used, retrieve from the map
            if (cache.containsKey(itemId)) {
                pictureIdx = cache.get(itemId);
            } else {
                // Get path from your AssetsManager
                String resourcePath = AssetsManager.getIconPath(itemId);
                InputStream is = ExcelExporter.class.getResourceAsStream(resourcePath);

                if (is != null) {
                    byte[] bytes = IOUtils.toByteArray(is);
                    pictureIdx = workbook.addPicture(bytes, Workbook.PICTURE_TYPE_PNG);
                    cache.put(itemId, pictureIdx);// store the icon
                }
            }


            CreationHelper helper = workbook.getCreationHelper();
            ClientAnchor anchor = helper.createClientAnchor();

            // icon stays in column 0
            anchor.setCol1(0);
            anchor.setRow1(rowNum);
            anchor.setCol2(1);
            anchor.setRow2(rowNum + 1);
            anchor.setDx1(20000);
            anchor.setDy1(20000);
            anchor.setDx2(-20000);
            anchor.setDy2(-20000);

            drawing.createPicture(anchor, pictureIdx);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    // return the colorful text based on different colors
    private static XSSFRichTextString colorfulText(int rankType, String string, XSSFFont purple, XSSFFont gold) {
        XSSFRichTextString richString = new XSSFRichTextString(string);

        if (rankType == 4) {
            richString.applyFont(0, string.length(), purple);
        }  else if (rankType == 5) {
            richString.applyFont(0, string.length(), gold);
        }

        return richString;
    }

    // a helper function to get the local time in pretty format
    private static String localTime(){
        LocalDateTime now = LocalDateTime.now();

        // Define a "pretty" pattern
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

        // Format the date time to a string
        return now.format(formatter);
    }

    private static LocalDateTime parseTime(String time) {
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return LocalDateTime.parse(time, df);
    }

    private static int compareId(String a, String b) {
        if (a == null && b == null) return 0;
        if (a == null) return -1;
        if (b == null) return 1;
        if (a.length() != b.length()) return Integer.compare(a.length(), b.length());
        return a.compareTo(b);
    }
}
