package Renderer.Window;

import Renderer.Components.ProcessController;
import Renderer.Components.SidebarPanel;
import Renderer.Charts.JFXChart;
import Renderer.Charts.RetrieveRecord;
import Renderer.Charts.SwingChart;
import Model.Genshin.GenshinGachaStatSummary;
import Core.Genshin.GenshinPlayerData;
import Core.Genshin.Statistics;
import I18n.GUI.GUILanguageManager;
import I18n.General.GeneralMessageManager;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

import static Renderer.Components.Styling.*;

/**
 * The main window of the user interface, distributing services to the controller class.
 * @course: Teacher: Daniel Vriesinga
 * @author Frank Fan at 2026/04/27
 */
public class MainWindow extends JFrame {

    // a static block will operate when the class is called, forcing JFX to stay opened
    static {
        Platform.setImplicitExit(false);// stay open even the window is closed
    }

    // all sing components...
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentArea = new JPanel(cardLayout);
    private final SidebarPanel sidebar;// side bar
    private final JPanel welcomePanel = new JPanel(new GridBagLayout());
    private final JPanel overviewPanel = new JPanel(new BorderLayout());
    private final JPanel chartPanel = new JPanel(new BorderLayout());// chart container
    private final CardLayout chartModeLayout = new CardLayout();
    private final JPanel chartModeArea = new JPanel(chartModeLayout);
    private final JPanel swingChartContainer = new JPanel(new BorderLayout());
    private final JFXPanel jfxPanel = new JFXPanel();// for JFX chart
    private final JLabel overviewLabel = new JLabel();
    private JLabel label = new JLabel();

    // all instances
    private final ProcessController pc;
    private GenshinGachaStatSummary gts;

    // frequently used texts
    private static final String welcome = "welcome";
    private static final String sanserif = "SansSerif";
    private static final String chart = "chart";


    /**
     * Main window constructor, initialized here as the entry point.
     */
    public MainWindow() {
        super("Genshin-Analyzer V1.0");
        this.pc = new ProcessController(this);

        // initialize language (default: English)
        GUILanguageManager.load("en");

        // sidebar
        setLayout(new BorderLayout());
        sidebar = new SidebarPanel(pc);
        add(sidebar, BorderLayout.WEST);

        // content area config (panel with stats)
        contentArea.setBackground(backgroundDark);
        contentArea.add(buildWelcomePanel(), welcome);
        contentArea.add(buildOverviewPanel(), "overview");
        contentArea.add(buildChartPanel(), chart);
        add(contentArea, BorderLayout.CENTER);
        cardLayout.show(contentArea, welcome);

        // size configure (maintain at 800!)
        setMinimumSize(new Dimension(1200, 800));
        setLocationRelativeTo(null);
    }

    /**
     * A helper that builds the welcome panel
     * @return functional JPanel
     */
    private JPanel buildWelcomePanel() {
        // language, color, text font configure
        welcomePanel.setBackground(backgroundDark);
        label = new JLabel(GUILanguageManager.get("label.welcome.pane"));
        label.setForeground(textColor);
        label.setFont(new Font(sanserif, Font.PLAIN, 18));
        welcomePanel.add(label);
        return welcomePanel;
    }

    /**
     * Builds statistic overview panel
     * @return functional JPanel
     */
    private JPanel buildOverviewPanel() {
        overviewPanel.setBackground(backgroundDark);

        //
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(cardBackground);
        card.setBorder(new CompoundBorder( new LineBorder(new Color(60, 60, 60), 1),
                new EmptyBorder(30, 40, 30, 40)));

        //
        overviewLabel.setVerticalAlignment(SwingConstants.TOP);
        overviewLabel.setHorizontalAlignment(SwingConstants.LEFT);
        card.add(overviewLabel, BorderLayout.CENTER);

        //
        overviewPanel.setLayout(new GridBagLayout());
        overviewPanel.add(card, new GridBagConstraints());

        return overviewPanel;
    }

    /**
     * Builds a container for charts
     * @return the concurred chart
     */
    private JPanel buildChartPanel() {
        // normal configs
        chartPanel.setBackground(backgroundDark);
        chartPanel.setBorder(new EmptyBorder(12, 12, 12, 12));
        swingChartContainer.setBackground(Color.WHITE);

        // add two charts
        chartModeArea.add(swingChartContainer, "swing");
        chartModeArea.add(jfxPanel, "jfx");
        chartPanel.add(chartModeArea, BorderLayout.CENTER);
        return chartPanel;
    }

    /**
     * A public builder that switches the content area to welcome panel
     */
    public void showWelcome() { cardLayout.show(contentArea, welcome);}

    /**
     * Generates the statistics for {@code player} and displays it in
     * the overview panel
     * @param player the loaded player data, cannot not be null and must contain at least one gacha record
     */
    public void showOverview(GenshinPlayerData player) {
        // handle when the player is null and no records
        if (player == null || player.records() == null || player.records().isEmpty()) {
            JOptionPane.showMessageDialog(this, GUILanguageManager.get("dialog.error.statistics"));
            showWelcome();
            return;
        }

        // create summary with this player and display
        gts = Statistics.generateSummary(player);
        setOverviewText();
        cardLayout.show(contentArea, "overview");
    }

    /**
     * {@link SwingChart} Creates instances (one per banner type) and contain the chart in swing mode
     * @param player the loaded player data, must not be null
     */
    public void showSwingChart(GenshinPlayerData player) {
        if (player == null) {// handle when the player is null
            showWelcome();
            return;
        }

        // obtain the record and preferred language
        RetrieveRecord.prepareRecords(player.records());
        RetrieveRecord.setLang(GeneralMessageManager.getCurrentLang());

        // clear and repaint
        swingChartContainer.removeAll();
        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(Color.WHITE);
        tabs.setForeground(Color.BLACK);
        tabs.setFont(new Font(sanserif, Font.BOLD, 12));
        tabs.setOpaque(true);

        // create instance per banner for each tab
        tabs.addTab(GeneralMessageManager.get("banner.301"), new SwingChart("301"));
        tabs.addTab(GeneralMessageManager.get("banner.400"), new SwingChart("400"));
        tabs.addTab(GeneralMessageManager.get("banner.302"), new SwingChart("302"));
        tabs.addTab(GeneralMessageManager.get("banner.500"), new SwingChart("500"));
        tabs.addTab(GeneralMessageManager.get("banner.200"), new SwingChart("200"));
        tabs.addTab(GeneralMessageManager.get("banner.100"), new SwingChart("100"));
        for (int i = 0; i < tabs.getTabCount(); i++) {// color config
            tabs.setBackgroundAt(i, Color.WHITE);
            tabs.setForegroundAt(i, Color.BLACK);
        }

        // add it to the panel
        swingChartContainer.add(tabs, BorderLayout.CENTER);
        chartModeLayout.show(chartModeArea, "swing");
        swingChartContainer.revalidate();
        swingChartContainer.repaint();
        cardLayout.show(contentArea, chart);
    }

    /**
     * Renders the {@link JFXChart} on the swing thread.
     * @param player the loaded player data, must not be {@code null}
     */
    public void showJfxChart(GenshinPlayerData player) {
        if (player == null) {// handle null case
            showWelcome();
            return;
        }

        // prepare the record and lang
        RetrieveRecord.prepareRecords(player.records());
        RetrieveRecord.setLang(GeneralMessageManager.getCurrentLang());

        Platform.runLater(() -> {
            jfxPanel.setScene(new Scene(new JFXChart()));// embed it to swing
            SwingUtilities.invokeLater(() -> {
                // refresh and display
                chartModeLayout.show(chartModeArea, "jfx");
                chartPanel.revalidate();
                chartPanel.repaint();
                cardLayout.show(contentArea, chart);
            });
        });

    }

    /**
     * Updates the status message in the sidebar
     * @param msg status text in string
     */
    public void setStatus(String msg) { sidebar.setStatus(msg);}


    /**
     * Builds the HTML string for overview panel.
     *
     * <p>Layout uses an HTML table
     * Color: gold for totals/pity, purple for character/weapon counts, blue for primogem spend, green/red for win-rate performance.
     * Link: <a href="https://docs.oracle.com/javase/tutorial/uiswing/components/html.html">...</a>
     * <a href="https://www.w3schools.com/html/html_tables.asp">...</a>
     */
    private void setOverviewText() {
        // set text color
        String gold = "#FFD700";
        String purple = "#BB86FC";
        String blue = "#00BFFF";

        // string html that is used for displaying the text
        String html = "<html>" +
                "<div style='width: 500px; font-family: SansSerif; padding: 10px;'>" +
                "<div style='text-align: center; margin-bottom: 20px;'>" +
                "<h1 style='color: " + gold + "; margin: 0;'>" + String.format(GUILanguageManager.get("summary.uid"), gts.uid()) +
                "</h1>" + "<p style='color: #888888; font-size: 10px;'>Genshin Impact Gacha Analysis</p>" +
                "</div>" + "<hr noshade size='1' color='#444444'>" +

                // total wishes and spent primogems
                "<table style='width: 100%; margin-top: 20px;'>" + renderRow("summary.total_wishes", String.valueOf(gts.totalWishes()), "#FFFFFF") +
                renderRow("summary.primogems", String.valueOf(gts.totalPrimogem()), blue) +
                "<tr><td colspan='2' style='height: 10px;'></td></tr>" +
                "<tr><td colspan='2' style='border-bottom: 1px solid #333333;'></td></tr>" +
                "<tr><td colspan='2' style='height: 10px;'></td></tr>" +

                // five-star item details
                renderRow("summary.five_stars", String.valueOf(gts.fiveStarTotal()), gold) +
                renderRow("summary.five_star_chars", String.valueOf(gts.fiveStarCharacter()), purple) +
                renderRow("summary.five_star_weapons", String.valueOf(gts.fiveStarWeapon()), purple) +
                renderRow("summary.limited_five_stars", String.valueOf(gts.limitedFiveStarCharacter()), gold) +
                "<tr><td colspan='2' style='height: 10px;'></td></tr>" + "<tr><td colspan='2' style='border-bottom: 1px solid #333333;'></td></tr>" +
                "<tr><td colspan='2' style='height: 10px;'></td></tr>" +

                // win rate display
                renderRow("summary.win_rate", String.format("%.1f%%", gts.overallProb()), gts.overallProb() >= 50 ? "#00FF00" : "#FF4500") +
                renderRow("summary.avg_pity", String.format("%.1f", gts.overallPity()), gold) +
                "</table>" + "</div>" + "</html>";

        overviewLabel.setText(html);
    }

    /**
     * Returns an HTML colored cells for the statistics table
     * @param key the i18n property key in string
     * @param value the formatted value in string
     * @param color HTML hex color in string (e.g. {@code "#FFD700"})
     * @return HTML string with colorful texts
     */
    private String renderRow(String key, String value, String color) {
        // localized
        String fullLabel = GUILanguageManager.get(key);
        String cleanLabel = fullLabel.contains(":") ? fullLabel.split(":")[0] : fullLabel;
        return "<tr>" +
                "<td style='font-size: 13px; color: #AAAAAA; padding: 4px 0;'>" + cleanLabel + "</td>" +
                "<td style='font-size: 16px; text-align: right; font-weight: bold; color: " + color + ";'>" + value + "</td>" +
                "</tr>";
    }

    /**
     * A helper for refreshing the panel when the language is switched
     */
    public void refreshContentLanguage() {
        label.setText(GUILanguageManager.get("label.welcome.pane"));
        if (gts != null) {// only refresh overview text if data exists
            setOverviewText();
        }

        // repaint, as well as all components under the process controller!
        revalidate();
        repaint();
        pc.refreshContentLanguage();
    }
}
