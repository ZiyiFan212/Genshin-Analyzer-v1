package Renderer.Charts;

import Assets.Resources.AssetsManager;
import Model.RecordTemplate.GachaRecord;
import I18n.GUI.GUILanguageManager;
import I18n.General.GeneralMessageManager;
import I18n.items.ItemTranslationManager;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

import java.util.Map;

/**
 *  A class that draws the javaFX chart with imported data.
 *  Resources: <a href="https://gluonhq.com/products/javafx/">...</a>
 *  <a href="https://www.youtube.com/watch?v=M3pvHuwZIZM&t=10s">...</a>
 * @course Teacher: Daniel Vriesinga
 * @author Frank Fan at 2026/04/25
  */
public class JFXChart extends StackPane {

    // static final object of banner code and fall back url
    private static final String[] bannerCodes = {"301", "302", "400", "500", "200", "100"};
    private static final String fallbackURL = "/Assets/Resources/missingWeaponIcon.png";

    /**
     * A public, non-parametric constructor for initializing the javaFX chart
     */
    public JFXChart() {
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // iterate through each banner
        for (String code : bannerCodes) {
            // naming tab with localized translation
            String localizedTitle = GeneralMessageManager.get(code);
            Tab tab = new Tab(localizedTitle);
            tab.setContent(buildBannerPane(code));// build a tab with the given code
            tabPane.getTabs().add(tab);
        }

        this.getChildren().add(tabPane);
    }

    /**
     * This method build the tab for each banner, given the banner code
     * @param bannerCode string of banner code
     * @return a scrollable panel with bars
     */
    private ScrollPane buildBannerPane(String bannerCode) {
        // set a vertical layout, 12 pixel
        VBox vbox = new VBox(12);
        vbox.setPadding(new Insets(15));
        vbox.setStyle("-fx-background-color: white;");

        // get pity from the record class with given banner code (fix: calculate trailing pity)
        RetrieveRecord.pityWithTrailing pwt = RetrieveRecord.calculatePity(bannerCode);
        Map<GachaRecord, Integer> pityMap = pwt.pityForFiveStar();
        int trailing = pwt.trailing();

        // giving a condition so it only paints the bar when trailing pity > 0
        if(trailing > 0) {
            vbox.getChildren().add(createTrailingRow(trailing));
            // a line separator because trailing doesn't belongs to any item!!
            javafx.scene.control.Separator sep = new javafx.scene.control.Separator();
            sep.setPadding(new Insets(5, 0, 5, 0));
            vbox.getChildren().add(sep);
        }

        // iterate the list using Entry, adding bar by calling a function
        for (Map.Entry<GachaRecord, Integer> entry : pityMap.entrySet()) {
            vbox.getChildren().add(createRecordRow(entry.getKey(), entry.getValue()));
        }

        // ensure the bar stretches with the window
        ScrollPane scrollPane = new ScrollPane(vbox);
        scrollPane.setFitToWidth(true);
        return scrollPane;
    }

    /**
     * Create a horizontal box that shows the trailing pity
     * A trailing pity is defined as wishes after the last five star item in a specific banner
     * @param pityValue trailing pity in integer
     * @return a hbox displaying the current pity in scale of 0 - 90
     */
    private HBox createTrailingRow(int pityValue){
        HBox row = new HBox(15);
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        // create an empty area to align with other bars because trailing pity doesn't have icons
        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        spacer.setMinWidth(40); // same as the icon view in createRecordRow
        spacer.setPrefWidth(40);

        // progress bar showing the pity
        ProgressBar bar = new ProgressBar(pityValue / 90.0);
        bar.setPrefWidth(200);
        HBox.setHgrow(bar, Priority.ALWAYS); // https://stackoverflow.com/questions/21865044/javafx-sethgrow-doesnt-work

        // still create a localized label, matching with other bars
        Label label = new Label(GUILanguageManager.get("label.trailing") + " (" + pityValue + ")");
        label.setMinWidth(150);
        row.getChildren().addAll(spacer, bar, label);// add them
        row.setStyle("-fx-cursor: hand;");
        return row;
    }

    /**
     * Create a horizontal box that shows the five-star pity
     * A five-star pity is defined as wishes required to obtain a limited item
     * @param rd gacha record class {@link GachaRecord}
     * @param pityValue number of pity for this five-star item
     * @return a pretty hbox in scale of 0 - 90
     */
    private HBox createRecordRow(GachaRecord rd, int pityValue) {
        HBox row = new HBox(15);
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        // create an icon
        ImageView iconView = new ImageView();
        try {
            // universal resource locater to point the image from the assets file
            java.net.URL url = JFXChart.class.getResource(AssetsManager.getIconPath(rd.getItem_id()));
            Image img = (url != null) ? new Image (url.toExternalForm()) : fallbackImage();

            // set icon size
            iconView.setImage(img);
            iconView.setFitWidth(40);
            iconView.setFitHeight(40);
        } catch (Exception e) {
            System.err.println("Error in loading message: " + e.getMessage());
        }

        // set the progress bar
        ProgressBar bar = new ProgressBar(pityValue / 90.0);
        bar.setPrefWidth(200);
        HBox.setHgrow(bar, Priority.ALWAYS);

        // colorful text
        String color = "#FFD700"; // -> a bright, warm ,metallic yellow
        bar.setStyle("-fx-accent: " + color + ";");

        // create label and add everything!!
        String name = ItemTranslationManager.returnName(rd.getItem_id(), RetrieveRecord.getLang());
        Label label = new Label(name + " (" + pityValue + ")");
        label.setMinWidth(150);
        row.getChildren().addAll(iconView, bar, label);

        // bar should be clickable, so using lambda to denote the return type
        row.setOnMouseClicked(e -> popWindow(rd, pityValue));
        row.setStyle("-fx-cursor: hand;");

        return row;
    }

    /**
     * A popout window that contains details of each pity
     * @param rd gacha record
     * @param pity pity value
     */
    private void popWindow(GachaRecord rd, int pity) {
        // instead of JOptionPane in swing, JFX uses alert.
        Alert alert = new Alert(Alert.AlertType.INFORMATION);

        // set title and body text
        alert.setTitle(GeneralMessageManager.get("GUI.chart.details.title"));
        alert.setContentText(String.format("Item: %s\nPity: %d\nDate: %s",
                ItemTranslationManager.returnName(rd.getItem_id(), RetrieveRecord.getLang()), pity, rd.getTime()));
        alert.showAndWait();
    }

    /**
     * A helper that return the fall back image
     * @return fallback image
     */
    private Image fallbackImage(){
        return new Image(fallbackURL);
    }
}
