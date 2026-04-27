package Renderer;

import Renderer.Components.ProcessController;
import Renderer.Components.SidebarPanel;
import Renderer.Charts.JFXChart;
import Renderer.Charts.RetrieveRecord;
import Renderer.Charts.SwingChart;
import Model.Genshin.GenshinGachaStatSummary;
import core.Genshin.GenshinPlayerData;
import core.Genshin.Statistics;
import i18n.General.GeneralMessageManager;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

import static Renderer.Components.Styling.*;

public class MainWindow extends JFrame {

    // a static block will operate when the class is called
    // force JFX to stay opened
    static {
        Platform.setImplicitExit(false);
    }

    // i love swing
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentArea = new JPanel(cardLayout);
    private final ProcessController pc;
    private final SidebarPanel sidebar;
    private final JPanel welcomePanel = new JPanel(new GridBagLayout());
    private final JPanel overviewPanel = new JPanel(new BorderLayout());
    private final JPanel chartPanel = new JPanel(new BorderLayout());
    private final CardLayout chartModeLayout = new CardLayout();
    private final JPanel chartModeArea = new JPanel(chartModeLayout);
    private final JPanel swingChartContainer = new JPanel(new BorderLayout());
    private final JFXPanel jfxPanel = new JFXPanel();
    private final JTextArea overviewText = new JTextArea();

    private static final String welcome = "welcome";
    private static final String sanserif = "SansSerif";
    private static final String chart = "chart";

    public MainWindow() {
        super("Genshin-Analyzer V1.0");
        this.pc = new ProcessController(this);

        setLayout(new BorderLayout());
        sidebar = new SidebarPanel(pc, cardLayout, contentArea);
        add(sidebar, BorderLayout.WEST);

        contentArea.setBackground(backgroundDark);
        contentArea.add(buildWelcomePanel(), welcome);
        contentArea.add(buildOverviewPanel(), "overview");
        contentArea.add(buildChartPanel(), chart);

        add(contentArea, BorderLayout.CENTER);
        cardLayout.show(contentArea, welcome);

        setSize(1100, 720);
        setLocationRelativeTo(null);
    }

    private JPanel buildWelcomePanel() {
        welcomePanel.setBackground(backgroundDark);
        JLabel label = new JLabel("Load local data or import JSON to start.");
        label.setForeground(textColor);
        label.setFont(new Font(sanserif, Font.PLAIN, 18));
        welcomePanel.add(label);
        return welcomePanel;
    }

    private JPanel buildOverviewPanel() {
        overviewPanel.setBackground(backgroundDark);
        overviewPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        overviewText.setEditable(false);
        overviewText.setLineWrap(true);
        overviewText.setWrapStyleWord(true);
        overviewText.setBackground(cardBackground);
        overviewText.setForeground(textColor);
        overviewText.setBorder(new EmptyBorder(14, 14, 14, 14));
        overviewText.setFont(new Font(sanserif, Font.PLAIN, 14));

        overviewPanel.add(new JScrollPane(overviewText), BorderLayout.CENTER);
        return overviewPanel;
    }

    private JPanel buildChartPanel() {
        chartPanel.setBackground(backgroundDark);
        chartPanel.setBorder(new EmptyBorder(12, 12, 12, 12));
        swingChartContainer.setBackground(Color.WHITE);
        chartModeArea.add(swingChartContainer, "swing");
        chartModeArea.add(jfxPanel, "jfx");
        chartPanel.add(chartModeArea, BorderLayout.CENTER);
        return chartPanel;
    }

    public void showWelcome() {
        cardLayout.show(contentArea, welcome);
    }

    // display the gacha info
    public void showOverview(GenshinPlayerData player) {
        if (player == null) {
            showWelcome();
            return;
        }

        GenshinGachaStatSummary summary = Statistics.generateSummary(player);
        overviewText.setText(
                "UID: " + summary.uid() + "\n\n" +
                "Total wishes: " + summary.totalWishes() + "\n" +
                "Primogems spent: " + summary.totalPrimogem() + "\n" +
                "Five-stars: " + summary.fiveStarTotal() + "\n" +
                "5-start characters: " + summary.fiveStarCharacter() + "\n" +
                "5-start weapons: " + summary.fiveStarWeapon() + "\n" +
                "Limited 5-star chars: " + summary.limitedFiveStarCharacter() + "\n" +
                "50/50 win rate: " + String.format("%.1f%%", summary.overallProb()) + "\n" +
                "Average pity: " + String.format("%.1f", summary.overallPity())
        );
        cardLayout.show(contentArea, "overview");
    }

    // display swing chart
    public void showSwingChart(GenshinPlayerData player) {
        if (player == null) {
            showWelcome();
            return;
        }

        RetrieveRecord.prepareRecords(player.getRecords());
        RetrieveRecord.setLang(GeneralMessageManager.getCurrentLang());

        swingChartContainer.removeAll();
        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(Color.WHITE);
        tabs.setForeground(Color.BLACK);
        tabs.setFont(new Font(sanserif, Font.BOLD, 12));
        tabs.setOpaque(true);
        tabs.addTab("Character event", new SwingChart("301"));
        tabs.addTab("Weapon event", new SwingChart("302"));
        tabs.addTab("Chronicled", new SwingChart("500"));
        tabs.addTab("Standard", new SwingChart("200"));
        tabs.addTab("Novice", new SwingChart("100"));
        for (int i = 0; i < tabs.getTabCount(); i++) {
            tabs.setBackgroundAt(i, Color.WHITE);
            tabs.setForegroundAt(i, Color.BLACK);
        }

        swingChartContainer.add(tabs, BorderLayout.CENTER);
        chartModeLayout.show(chartModeArea, "swing");
        swingChartContainer.revalidate();
        swingChartContainer.repaint();
        cardLayout.show(contentArea, chart);
    }

    // display JFX chart
    public void showJfxChart(GenshinPlayerData player) {
        if (player == null) {
            showWelcome();
            return;
        }

        RetrieveRecord.prepareRecords(player.getRecords());
        RetrieveRecord.setLang(GeneralMessageManager.getCurrentLang());

        Platform.runLater(() -> jfxPanel.setScene(new Scene(new JFXChart())));
        chartModeLayout.show(chartModeArea, "jfx");
        chartPanel.revalidate();
        chartPanel.repaint();
        cardLayout.show(contentArea, chart);
    }

    public void setStatus(String msg) {
        sidebar.setStatus(msg);
    }
}










