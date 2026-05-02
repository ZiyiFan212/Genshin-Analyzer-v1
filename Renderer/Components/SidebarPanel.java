package Renderer.Components;

import I18n.GUI.GUILanguageManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

import static Renderer.Components.Styling.*;

/**
 * @course Teacher: Daniel Vriesinga
 * @author Frank Fan at 2026/04/27
 * Left-side navigation bar
 */
public class SidebarPanel extends JPanel {

    // controller instance
    private final ProcessController controller;

    // all swing component...
    private JLabel titleLabel;
    private JComboBox<String> uidSelector;// drop down list
    private JTextField searchField;
    private JButton loadLocalBtn;
    private JButton importFileBtn;
    private JButton exportExcelBtn;
    private JButton saveJsonBtn;// function buttons...
    private JLabel subLabel;
    private JLabel activePlayerLabel;
    private JLabel viewsLabel;// some labels, for like headers
    private JLabel searchSectionLabel;
    private JButton searchBtn;
    private JLabel actionsLabel;
    private JButton overviewBtn;
    private JButton chartSwingBtn;
    private JButton chartJfxBtn;
    private JButton quitBtn;
    private JLabel statusLabel;

    // final static constants
    private static final String langIconPath = "/I18n/GUI/i18nIcon.png";
    private static final String sanserif = "SansSerif";

    /**
     * public parametric contractor, builds all child components with the supplied controller
     * @param controller the application controller
     */
    public SidebarPanel(ProcessController controller) {
        this.controller = controller;
        setLayout(new BorderLayout());
        add(buildSidebar(), BorderLayout.CENTER);
        controller.bindUI(uidSelector, searchField, exportExcelBtn, saveJsonBtn);
    }

    /**
     * create the sidebar panel with all five sections
     * @return the fully constructed sidebar
     */
    private JPanel buildSidebar() {
        // basic config..
        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setBackground(panelBackground);
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));

        // build -> title + lang button
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.X_AXIS));
        headerPanel.setOpaque(false);
        headerPanel.setAlignmentX(LEFT_ALIGNMENT);
        headerPanel.setBorder(new EmptyBorder(24, 20, 4, 10));
        headerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        // label settings
        titleLabel = new JLabel(GUILanguageManager.get("app.title"));
        titleLabel.setFont(new Font("Georgia", Font.BOLD, 16));
        titleLabel.setForeground(gold);
        titleLabel.setAlignmentY(CENTER_ALIGNMENT);

        // language button
        JButton langButton = new JButton();
        java.net.URL iconUrl = getClass().getResource(langIconPath);// retrieve the icon from the resources pac
        if (iconUrl != null) {
            ImageIcon raw = new ImageIcon(iconUrl);
            Image scaled = raw.getImage().getScaledInstance(18, 18, Image.SCALE_SMOOTH);
            langButton.setIcon(new ImageIcon(scaled));// display the icon
        } else {
            langButton.setText("🌐");// unable to obtain the resource, set fallback
            System.err.println("Warning: icon not found at " + langIconPath);
        }
        langButton.setBorderPainted(false);
        langButton.setContentAreaFilled(false);
        langButton.setFocusPainted(false);
        langButton.setPreferredSize(new Dimension(28, 28));
        langButton.setMaximumSize(new Dimension(28, 28));// limit the dimension, otherwise layout would be ruined
        langButton.setAlignmentY(CENTER_ALIGNMENT);
        langButton.addActionListener(e -> popupMenu(langButton));
        // add the title and the button
        headerPanel.add(titleLabel);
        headerPanel.add(Box.createHorizontalGlue());
        headerPanel.add(langButton);
        sidebar.add(headerPanel);

        // subtitle part
        subLabel = new JLabel(GUILanguageManager.get("app.subtitle"));
        subLabel.setFont(new Font(sanserif, Font.PLAIN, 11));
        subLabel.setForeground(muted);
        subLabel.setBorder(new EmptyBorder(2, 20, 16, 20));
        subLabel.setAlignmentX(LEFT_ALIGNMENT);
        sidebar.add(subLabel);
        sidebar.add(sep());// add a divider line

        // active player part
        activePlayerLabel = sectionLabel(GUILanguageManager.get("section.active_player"), 0);
        sidebar.add(activePlayerLabel);
        uidSelector = new JComboBox<>();// initialize dropdown
        uidSelector.setBackground(cardBackground);
        uidSelector.setForeground(textColor);
        uidSelector.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));// restrict the height, but not width
        uidSelector.setAlignmentX(LEFT_ALIGNMENT);
        sidebar.add(padded(uidSelector, 6, 16, 10, 16));
        sidebar.add(sep());

        // chart selection part
        viewsLabel = sectionLabel(GUILanguageManager.get("section.views"));// localized label
        sidebar.add(viewsLabel);
        overviewBtn = actionBtn(GUILanguageManager.get("view.overview"), cardBackground);
        overviewBtn.addActionListener(e -> controller.showOverview());// call the service
        sidebar.add(padded(overviewBtn, 6, 16, 4, 16));
        chartSwingBtn = actionBtn(GUILanguageManager.get("view.chart.swing"), cardBackground);
        chartSwingBtn.addActionListener(e -> controller.showChart());
        sidebar.add(padded(chartSwingBtn, 4, 16, 4, 16));
        chartJfxBtn = actionBtn(GUILanguageManager.get("view.chart.jfx"), cardBackground);
        chartJfxBtn.addActionListener(e -> controller.showJfxChart());
        sidebar.add(padded(chartJfxBtn, 4, 16, 10, 16));
        sidebar.add(Box.createVerticalStrut(4));// add space between buttons
        sidebar.add(sep());

        // searching part
        searchSectionLabel = sectionLabel(GUILanguageManager.get("section.search"));
        sidebar.add(searchSectionLabel);
        searchField = new JTextField();
        searchField.setBackground(cardBackground);
        searchField.setForeground(textColor);
        searchField.setCaretColor(textColor);// set color
        searchField.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(cardBackground),
                new EmptyBorder(4, 8, 4, 8)));// combines two borders together
        searchField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        searchField.setAlignmentX(LEFT_ALIGNMENT);
        sidebar.add(padded(searchField, 2, 16, 4, 16));
        searchBtn = actionBtn(GUILanguageManager.get("search.button"), cardBackground);
        searchBtn.addActionListener(e -> controller.onSearch());// call to search the player
        sidebar.add(padded(searchBtn, 0, 16, 10, 16));
        sidebar.add(sep());

        // buttons for all action service
        actionsLabel = sectionLabel(GUILanguageManager.get("section.actions"));// i18n label
        sidebar.add(actionsLabel);
        // buttons related to I/O
        loadLocalBtn = actionBtn(GUILanguageManager.get("action.load_local"), cardBackground);// helper function that returns a JButton
        loadLocalBtn.addActionListener(e -> controller.loadLocal());
        sidebar.add(padded(loadLocalBtn, 6, 16, 4, 16));
        importFileBtn = actionBtn(GUILanguageManager.get("action.import_json"), cardBackground);
        importFileBtn.addActionListener(e -> controller.onImportFile());
        sidebar.add(padded(importFileBtn, 4, 16, 4, 16));
        saveJsonBtn = actionBtn(GUILanguageManager.get("action.save_json"), new Color(24, 64, 36));
        saveJsonBtn.setEnabled(false);
        saveJsonBtn.addActionListener(e -> controller.onSaveJson());
        sidebar.add(padded(saveJsonBtn, 4, 16, 4, 16));
        exportExcelBtn = actionBtn(GUILanguageManager.get("action.export_excel"), new Color(24, 64, 36));
        exportExcelBtn.setEnabled(false);// false because the data file is the prerequisite to use saving and exporting
        exportExcelBtn.addActionListener(e -> controller.onExportExcel());
        sidebar.add(padded(exportExcelBtn, 4, 16, 4, 16));
        // quit button
        quitBtn = actionBtn(GUILanguageManager.get("action.quit"), new Color(72, 34, 34));
        quitBtn.addActionListener(e -> {
            javafx.application.Platform.exit();
            System.exit(0);// safely terminate
        });
        sidebar.add(padded(quitBtn, 4, 16, 10, 16));
        sidebar.add(Box.createVerticalGlue());
        sidebar.add(sep());

        // status label section
        statusLabel = new JLabel(GUILanguageManager.get("status.ready"));
        statusLabel.setFont(new Font(sanserif, Font.PLAIN, 11));
        statusLabel.setForeground(textColor);
        statusLabel.setBorder(new EmptyBorder(10, 16, 16, 16));
        statusLabel.setAlignmentX(LEFT_ALIGNMENT);
        sidebar.add(statusLabel);

        return sidebar;// return this functional sidebar
    }

    /**
     * Apply language translation to all components in the sidebar, refreshing called in the main window
     * {@link ProcessController#triggerMainWindowRefresh()}.
     */
    private void refreshLanguage() {
        // dynamically set all texts
        titleLabel.setText(GUILanguageManager.get("app.title"));
        statusLabel.setText(GUILanguageManager.get("status.ready"));
        subLabel.setText(GUILanguageManager.get("app.subtitle"));
        overviewBtn.setText(GUILanguageManager.get("view.overview"));
        chartSwingBtn.setText(GUILanguageManager.get("view.chart.swing"));
        activePlayerLabel.setText(GUILanguageManager.get("section.active_player").toUpperCase());
        viewsLabel.setText(GUILanguageManager.get("section.views").toUpperCase());
        searchSectionLabel.setText(GUILanguageManager.get("section.search").toUpperCase());
        actionsLabel.setText(GUILanguageManager.get("section.actions").toUpperCase());
        loadLocalBtn.setText(GUILanguageManager.get("action.load_local"));
        importFileBtn.setText(GUILanguageManager.get("action.import_json"));
        saveJsonBtn.setText(GUILanguageManager.get("action.save_json"));
        exportExcelBtn.setText(GUILanguageManager.get("action.export_excel"));
        chartJfxBtn.setText(GUILanguageManager.get("view.chart.jfx"));
        searchBtn.setText(GUILanguageManager.get("search.button"));
        quitBtn.setText(GUILanguageManager.get("action.quit"));

        // repaint
        controller.triggerMainWindowRefresh();
        revalidate();
        repaint();
    }


    /**
     * Updates the status bar text at the bottom of the sidebar.
     * @param msg the status message to display
     */
    public void setStatus(String msg) {
        statusLabel.setText(msg);
    }

    // overloaded example
    /**
     * set a default top padding of 12 px for the label
     *
     * @param text section heading text in string
     * @return styled section label
     */
    private JLabel sectionLabel(String text) {
        return sectionLabel(text, 12);
    }

    /**
     * Create a small label styled with the muted foreground color and bold 10 px font
     *
     * @param text section heading text; displayed in uppercase
     * @param topPad top padding in pixels
     * @return the configured {@link JLabel}
     */
    private JLabel sectionLabel(String text, int topPad) {
        JLabel label = new JLabel(text.toUpperCase());// convert to upper case
        label.setFont(new Font(sanserif, Font.BOLD, 10));
        label.setForeground(muted);// muted foreground
        label.setBorder(new EmptyBorder(topPad, 16, 4, 16));
        label.setAlignmentX(LEFT_ALIGNMENT);
        return label;
    }

    /**
     * Wraps a component with the specified insets
     * @param c the component to wrap
     * @param top top padding in pixels
     * @param left left padding in pixels
     * @param bottom bottom padding in pixels
     * @param right right padding in pixels
     * @return a {@link JPanel} with the padding
     * left and right are 16 pixels.
     */
    private JPanel padded(JComponent c, int top, int left, int bottom, int right) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(panelBackground);
        p.setBorder(new EmptyBorder(top, left, bottom, right));// set border with input pixel
        p.add(c);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, c.getPreferredSize().height + top + bottom));
        p.setAlignmentX(LEFT_ALIGNMENT);// consistent left alignment
        return p;
    }

    /**
     * Create action button for the sidebar
     * @param text the button label text in string
     * @param bg background color (refer to {@code cardBackground} for different actions
     * @return the configured button {@link JButton}
     */
    private JButton actionBtn(String text, Color bg) {
        JButton btn = new JButton(text);// create a button with the label
        btn.setBackground(bg);
        btn.setFont(new Font(sanserif, Font.PLAIN, 12));
        btn.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.BLACK, 1),
                new EmptyBorder(6, 12, 6, 12)));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));// use a new cursor
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        btn.setAlignmentX(LEFT_ALIGNMENT);
        btn.setForeground(Color.BLACK);
        return btn;
    }

    /**
     * Creates a horizontal separator line, used to divide the sidebar into sections
     * @return a thin line {@link JSeparator}
     */
    private JSeparator sep() {
        JSeparator s = new JSeparator();
        s.setForeground(cardBackground);
        s.setBackground(cardBackground);// consistent with the fore/background color
        s.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        return s;
    }

    /**
     * A helper function for language-selection popup menu, used by {@code langButton}
     * @param langButton the button that triggered the popup;
     */
    private void popupMenu(JButton langButton) {
        JPopupMenu popup = new JPopupMenu();

        // English selection
        JMenuItem english = new JMenuItem("English");
        english.addActionListener(e -> {
            GUILanguageManager.load("en");// relead service
            refreshLanguage();// reload GUI
        });
        // simplified Chinese
        JMenuItem simplifiedChinese = new JMenuItem("简体中文");
        simplifiedChinese.addActionListener(e -> {
            GUILanguageManager.load("zh");
            refreshLanguage();
        });

        // two languages are added to the menu
        popup.add(english);
        popup.addSeparator();
        popup.add(simplifiedChinese);
        popup.show(langButton, 0, langButton.getHeight());
    }
}