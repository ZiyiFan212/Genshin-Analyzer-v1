package Renderer.Components;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

import static Renderer.Components.Styling.*;

public class SidebarPanel extends JPanel {
    private final ProcessController controller;
    private JComboBox<String> uidSelector;
    private JTextField searchField;
    private JButton exportExcelBtn;
    private JButton saveJsonBtn;
    private JLabel statusLabel;

    public SidebarPanel(ProcessController controller, CardLayout cardLayout, JPanel contentArea) {
        this.controller = controller;

        setLayout(new BorderLayout());
        add(buildSidebar(), BorderLayout.CENTER);
        controller.bindUI(uidSelector, searchField, exportExcelBtn, saveJsonBtn);
    }

    // SIDEBAR

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setBackground(panelBackground);
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Genshin Analyzer");
        title.setFont(new Font("Georgia", Font.BOLD, 16));
        title.setForeground(gold);
        title.setBorder(new EmptyBorder(24, 20, 4, 20));
        title.setAlignmentX(LEFT_ALIGNMENT);

        JLabel sub = new JLabel("Wish history viewer");
        sub.setFont(new Font("SansSerif", Font.PLAIN, 11));
        sub.setForeground(muted);
        sub.setBorder(new EmptyBorder(0, 20, 16, 20));
        sub.setAlignmentX(LEFT_ALIGNMENT);

        sidebar.add(title);
        sidebar.add(sub);
        sidebar.add(sep());

        sidebar.add(sectionLabel("Active player"));
        uidSelector = new JComboBox<>();
        uidSelector.setBackground(cardBackground);
        uidSelector.setForeground(textColor);
        uidSelector.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        uidSelector.setAlignmentX(LEFT_ALIGNMENT);
        sidebar.add(padded(uidSelector, 6, 16, 10, 16));
        sidebar.add(sep());

        sidebar.add(sectionLabel("Views"));
        JButton overviewBtn = actionBtn("Overview", cardBackground, accentColor);
        overviewBtn.addActionListener(e -> controller.showOverview());
        JButton chartSwingBtn = actionBtn("Pity chart (Swing)", cardBackground, accentColor);
        chartSwingBtn.addActionListener(e -> controller.showChart());
        JButton chartJfxBtn = actionBtn("Pity chart (JFX)", cardBackground, accentColor);
        chartJfxBtn.addActionListener(e -> controller.showJfxChart());
        sidebar.add(padded(overviewBtn, 6, 16, 4, 16));
        sidebar.add(padded(chartSwingBtn, 4, 16, 4, 16));
        sidebar.add(padded(chartJfxBtn, 4, 16, 10, 16));
        sidebar.add(Box.createVerticalStrut(4));
        sidebar.add(sep());

        sidebar.add(sectionLabel("Search UID"));
        searchField = new JTextField();
        searchField.setBackground(cardBackground);
        searchField.setForeground(textColor);
        searchField.setCaretColor(textColor);
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(cardBackground), new EmptyBorder(4, 8, 4, 8)));
        searchField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        searchField.setAlignmentX(LEFT_ALIGNMENT);

        JButton searchBtn = actionBtn("Search", cardBackground, accentColor);
        searchBtn.addActionListener(e -> controller.onSearch());
        sidebar.add(padded(searchField, 6, 16, 4, 16));
        sidebar.add(padded(searchBtn,0, 16, 10, 16));
        sidebar.add(sep());

        sidebar.add(sectionLabel("Actions"));
        JButton loadLocalBtn = actionBtn("Load local data", cardBackground, accentColor);
        JButton importFileBtn = actionBtn("Import JSON file", cardBackground, accentColor);
        saveJsonBtn = actionBtn("Save as JSON", new Color(24, 64, 36), accentColor);
        exportExcelBtn = actionBtn("Export as Excel", new Color(24, 64, 36), accentColor);
        JButton quitBtn = actionBtn("Quit", new Color(72, 34, 34), accentColor);
        saveJsonBtn.setEnabled(false);
        exportExcelBtn.setEnabled(false);

        loadLocalBtn.addActionListener(e -> controller.loadLocal());
        importFileBtn.addActionListener(e -> controller.onImportFile());
        saveJsonBtn.addActionListener(e -> controller.onSaveJson());
        exportExcelBtn.addActionListener(e -> controller.onExportExcel());
        quitBtn.addActionListener(e -> System.exit(0));

        sidebar.add(padded(loadLocalBtn,6, 16, 4, 16));
        sidebar.add(padded(importFileBtn,4, 16, 4, 16));
        sidebar.add(padded(saveJsonBtn,4, 16, 4, 16));
        sidebar.add(padded(exportExcelBtn,4, 16, 4, 16));
        sidebar.add(padded(quitBtn,4, 16, 10, 16));

        sidebar.add(Box.createVerticalGlue());
        sidebar.add(sep());
        statusLabel = new JLabel("Ready");
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        statusLabel.setForeground(textColor);
        statusLabel.setBorder(new EmptyBorder(10, 16, 16, 16));
        statusLabel.setAlignmentX(LEFT_ALIGNMENT);
        sidebar.add(statusLabel);

        return sidebar;
    }

    private JLabel sectionLabel(String text) {
        JLabel l = new JLabel(text.toUpperCase());
        l.setFont(new Font("SansSerif", Font.BOLD, 10));
        l.setForeground(muted);
        l.setBorder(new EmptyBorder(12, 16, 4, 16));
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }

    private JPanel padded(JComponent c, int top, int left, int bottom, int right) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(panelBackground);
        p.setBorder(new EmptyBorder(top, left, bottom, right));
        p.add(c);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, c.getPreferredSize().height + top + bottom));
        p.setAlignmentX(LEFT_ALIGNMENT);
        return p;
    }

    public void setStatus(String msg) {
        statusLabel.setText(msg);
    }

    private JButton actionBtn(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.BLACK);
        btn.setFont(new Font("SansSerif", Font.PLAIN, 12));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.BLACK, 1), new EmptyBorder(6, 12, 6, 12)));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        btn.setAlignmentX(LEFT_ALIGNMENT);
        return btn;
    }

    private JSeparator sep() {
        JSeparator s = new JSeparator();
        s.setForeground(cardBackground);
        s.setBackground(cardBackground);
        s.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        return s;
    }
}
