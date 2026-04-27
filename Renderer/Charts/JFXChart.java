package Renderer.Charts;

import Assets.Resources.AssetsManager;
import Model.GachaRecord;
import i18n.General.GeneralMessageManager;
import i18n.items.ItemTranslationManager;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

import java.util.Map;

public class JFXChart extends StackPane {

    private static final String[] bannerCodes = {"301", "302", "500", "200", "100"};

    public JFXChart() {
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        for (String code : bannerCodes) {
            String localizedTitle = GeneralMessageManager.get(code);
            Tab tab = new Tab(localizedTitle);
            tab.setContent(buildBannerPane(code));
            tabPane.getTabs().add(tab);
        }

        this.getChildren().add(tabPane);
    }

    private ScrollPane buildBannerPane(String bannerCode) {
        VBox vbox = new VBox(12); // Gap between rows
        vbox.setPadding(new Insets(15));
        vbox.setStyle("-fx-background-color: white;");

        Map<GachaRecord, Integer> pityMap = RetrieveRecord.calculatePity(bannerCode);

        for (Map.Entry<GachaRecord, Integer> entry : pityMap.entrySet()) {
            vbox.getChildren().add(createRecordRow(entry.getKey(), entry.getValue()));
        }

        ScrollPane scrollPane = new ScrollPane(vbox);
        scrollPane.setFitToWidth(true); // Ensures bars stretch with window
        return scrollPane;
    }

    private HBox createRecordRow(GachaRecord rd, int pityValue) {
        HBox row = new HBox(15);
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        // get icon
        ImageView iconView = new ImageView();
        try {
            // JFX Image loading
            Image img = new Image(AssetsManager.getIconPath(rd.getItem_id()));
            iconView.setImage(img);
            iconView.setFitWidth(40);
            iconView.setFitHeight(40);
        } catch (Exception e) {
            // fall back to red cross
            Image img2 = new Image("/Assets/Resources/missingWeaponIcon.png");
            iconView.setImage(img2);
            iconView.setFitWidth(40);
            iconView.setFitHeight(40);
        }

        ProgressBar bar = new ProgressBar(pityValue / 90.0);
        bar.setPrefWidth(200); // Base width
        HBox.setHgrow(bar, Priority.ALWAYS); // Stretch bar to fill space

        // colorful text
        String color = (rd.getRank_type() == 5) ? "#FFD700" : "#9370DB";
        bar.setStyle("-fx-accent: " + color + ";");

        // create label
        String name = ItemTranslationManager.returnName(rd.getItem_id(), RetrieveRecord.getLang());
        Label label = new Label(name + " (" + pityValue + ")");
        label.setMinWidth(150);

        row.getChildren().addAll(iconView, bar, label);

        // Click Logic (Built-in to JFX nodes!)
        row.setOnMouseClicked(e -> popWindow(rd, pityValue));
        row.setStyle("-fx-cursor: hand;");

        return row;
    }

    private void popWindow(GachaRecord rd, int pity) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(GeneralMessageManager.get("GUI.chart.details.title"));
        alert.setContentText(String.format("Item: %s\nPity: %d\nDate: %s",
                ItemTranslationManager.returnName(rd.getItem_id(), RetrieveRecord.getLang()),
                pity, rd.getTime()));
        alert.showAndWait();
    }
}
