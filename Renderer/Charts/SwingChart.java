package Renderer.Charts;

import Model.GachaRecord;
import i18n.GUI.GUILanguageManager;
import i18n.General.GeneralMessageManager;
import i18n.items.ItemTranslationManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static Assets.Resources.AssetsManager.getIcon;


public class SwingChart extends JScrollPane {

    // may use this to add other function is future
    private final ArrayList<GachaRecord> records;

    private final Map<Rectangle, GachaRecord> clickableBar = new HashMap<>();
    private Map<GachaRecord, Integer> pityForFiveStar;
    private final String banner;

    public SwingChart(String banner) {
       this.records = RetrieveRecord.getRecords();
       this.banner = banner;// must give "301, 302, 500 etc" in string!!

        // Title for current banner, shown above chart.
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(237, 242, 249));
        JLabel titleLabel = new JLabel(GeneralMessageManager.get(banner));
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        titleLabel.setForeground(new Color(35, 44, 66));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 10));

        headerPanel.add(titleLabel, BorderLayout.WEST);

        ChartContent content = new ChartContent();
        setViewportView(content);
        setColumnHeaderView(headerPanel);
        setBorder(BorderFactory.createEmptyBorder());
        getViewport().setBackground(Color.WHITE);
        getVerticalScrollBar().setUnitIncrement(16);
    }

    // https://stackoverflow.com/questions/35442917/java-swing-draw-multiple-click-able-shapes
    private class ChartContent extends JPanel {
        public ChartContent() {
            setBackground(Color.WHITE);
            pityForFiveStar = RetrieveRecord.calculatePity(banner);
            int totalHeight = 20 + (pityForFiveStar.size() + 1) * (40 + 15) + 50;
            setPreferredSize(new Dimension(400, totalHeight));

            this.addMouseListener(new MouseListener() {
                @Override
                public void mouseClicked(MouseEvent mouseEvent) {
                    final Point p = mouseEvent.getPoint();
                    for(Map.Entry<Rectangle, GachaRecord> entry : clickableBar.entrySet()){
                        if(entry.getKey().contains(p)){
                            popWindow(entry.getValue());
                        }
                    }
                }
                // ide is too mad, has to add
                @Override
                public void mousePressed(MouseEvent mouseEvent) {
                // todo
                }

                @Override
                public void mouseReleased(MouseEvent mouseEvent) {

                }

                @Override
                public void mouseEntered(MouseEvent mouseEvent) {

                }

                @Override
                public void mouseExited(MouseEvent mouseEvent) {

                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            clickableBar.clear();
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int startY = 20;
            int barHeight = 40;
            int gap = 15;
            int iconSize = 40;
            int maxBarWidth = SwingChart.this.getWidth() - 300;

            int i = 1;
            for (Map.Entry<GachaRecord, Integer> entry : pityForFiveStar.entrySet()) {
                GachaRecord rd = entry.getKey();
                int currentY = startY + i * (barHeight + gap);

                // draw Icon
                Image image = getIcon(rd.getItem_id());
                if (image != null) {
                    g2d.drawImage(image, 10, currentY, iconSize, iconSize, null);
                }

                // draw bar, remember the max pity is 90
                int pityValue = entry.getValue();
                int barWidth = (int) ((entry.getValue() / 90.0) * maxBarWidth);

                // 5 star -> gold (future: 4 star -> purple //new Color(147, 112, 219))
                g2d.setColor(rd.getRank_type() == 5 ? new Color(255, 215, 0) : new Color (0, 0, 0));
                Rectangle rect = new Rectangle(iconSize + 20, currentY + 10, Math.max(barWidth, 5), 20);
                g2d.fillRoundRect(rect.x, rect.y, rect.width, rect.height, 10, 10);
                clickableBar.put(rect, rd);

                // draw label
                g2d.setColor(Color.DARK_GRAY);
                g2d.setFont(new Font("SansSerif", Font.BOLD, 12));
                String name = ItemTranslationManager.returnName(rd.getItem_id(), RetrieveRecord.getLang());
                String label = String.format("%s (%d)", name, pityValue);
                g2d.drawString(label, iconSize + barWidth + 30, currentY + 25);

                i++;
            }
        }

        private void popWindow(GachaRecord rd) {
            String string = ItemTranslationManager.returnName(rd.getItem_id(), RetrieveRecord.getLang()) + "\n" +
                    rd.getItem_id() + "\n" + rd.getTime() + "\n" + rd.getItem_type();

            JOptionPane.showMessageDialog(this, string,
                    GUILanguageManager.get("GUI.chart.details.title"), JOptionPane.INFORMATION_MESSAGE);
        }
    }

}
