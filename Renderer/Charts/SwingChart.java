package Renderer.Charts;

import Model.RecordTemplate.GachaRecord;
import I18n.GUI.GUILanguageManager;
import I18n.General.GeneralMessageManager;
import I18n.items.ItemTranslationManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static Assets.Resources.AssetsManager.getIcon;

/**
 * Teacher: Daniel Vriesinga
 * Frank Fan at 2026/04/26
 * This class extends the scroll panel (JFrame) to paint the swing chart {@link JScrollPane}
 */
public class SwingChart extends JScrollPane {

    // may use this to add other function is future
    private final ArrayList<GachaRecord> records;

    // private fields
    private final Map<Rectangle, GachaRecord> clickableBar = new HashMap<>();
    private Map<GachaRecord, Integer> pityForFiveStar;
    private final String banner;
    private int trailingCounter;
    private static final String sansSerif = "SansSerif";


    /**
     * A public, parametric constructor that assigns the banner code to the local variable
     * @param banner
     */
    public SwingChart(String banner) {
       this.records = RetrieveRecord.getRecords();
       this.banner = banner;// must give "301, 302, 500 etc" in string!!

        // title for current banner, shown above the chart
        // font, size, color, and other settings
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(237, 242, 249));
        JLabel titleLabel = new JLabel(GeneralMessageManager.get(banner));
        titleLabel.setFont(new Font(sansSerif, Font.BOLD, 16));
        titleLabel.setForeground(new Color(35, 44, 66));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 10));

        // add the label to the panel
        headerPanel.add(titleLabel, BorderLayout.WEST);

        // background, scrollable panel, and other configuration
        ChartContent content = new ChartContent();
        setViewportView(content);
        setColumnHeaderView(headerPanel);// fixed on the top
        setBorder(BorderFactory.createEmptyBorder());
        getViewport().setBackground(Color.WHITE);
        getVerticalScrollBar().setUnitIncrement(16);
    }

    // https://stackoverflow.com/questions/35442917/java-swing-draw-multiple-click-able-shapes
    /**
     * Chart is painted in this class using graphic2d customization
     */
    private class ChartContent extends JPanel {

        // public constructor
        public ChartContent() {
            setBackground(Color.WHITE);

            // static method to get the pity for each banner, including trailing pity
            RetrieveRecord.pityWithTrailing pwt = RetrieveRecord.calculatePity(banner);
            pityForFiveStar = pwt.pityForFiveStar();
            trailingCounter = pwt.trailing();

            // configure size
            int totalHeight = 20 + (pityForFiveStar.size() + 1) * (40 + 15) + 50;
            setPreferredSize(new Dimension(400, totalHeight));

            //
            this.addMouseListener(new MouseListener() {
                @Override
                public void mouseClicked(MouseEvent mouseEvent) {
                    // using point for the location of clicking, then iterate through the map to find which bar is clicked
                    final Point p = mouseEvent.getPoint();
                    for(Map.Entry<Rectangle, GachaRecord> entry : clickableBar.entrySet()){
                        if(entry.getKey().contains(p)){
                            popWindow(entry.getValue());// show the window
                        }
                    }
                }
                // ide is too mad for not implementing, has to add (use adaptor in the future!!!)
                public void mousePressed(MouseEvent e) { /* TODO document why this method is empty */ }
                public void mouseReleased(MouseEvent e) { /* TODO document why this method is empty */ }
                public void mouseEntered(MouseEvent e) { /* TODO document why this method is empty */ }
                public void mouseExited(MouseEvent e) { /* TODO document why this method is empty */ }
            });
        }

        // we create the customized horizontal bar chart here
        @Override
        protected void paintComponent(Graphics g) {
            // pre-painting configure
            super.paintComponent(g);
            clickableBar.clear();// clear the map
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // size configuration
            int startY = 20;
            int barHeight = 40;
            int gap = 15;
            int iconSize = 40;
            int maxBarWidth = SwingChart.this.getWidth() - 300;

            // set the initial column index as 1, painting the trailing pity and five-star pities
            int i = 1;
            // trailing bar
            if (trailingCounter > 0) {
                int trailingY = startY + i * (barHeight + gap);
                // draw trailing bar
                drawTrailingBar(g2d, trailingCounter, trailingY, iconSize, maxBarWidth);
                i++;

                // draw a line separator
                g2d.setColor(Color.LIGHT_GRAY);
                int lineY = startY + i * (barHeight + gap) - (gap / 2);
                g2d.drawLine(10, lineY, iconSize + maxBarWidth + 150, lineY);
            }
            // five-star bars
            for (Map.Entry<GachaRecord, Integer> entry : pityForFiveStar.entrySet()) {
                GachaRecord rd = entry.getKey();
                int currentY = startY + i * (barHeight + gap);

                // draw icon
                Image image = getIcon(rd.getItem_id());
                if (image != null) {
                    g2d.drawImage(image, 10, currentY, iconSize, iconSize, null);// null observer because the icon doesn't take too long to load
                }

                // draw bars, max pity = 90
                int pityValue = entry.getValue();
                int barWidth = (int) ((entry.getValue() / 90.0) * maxBarWidth);

                // 5 star -> gold new Color(147, 112, 219)
                g2d.setColor(new Color(255, 215, 0));
                Rectangle rect = new Rectangle(iconSize + 20, currentY + 10, Math.max(barWidth, 5), 20);
                g2d.fillRoundRect(rect.x, rect.y, rect.width, rect.height, 10, 10);
                clickableBar.put(rect, rd);// add it to the map

                // generating the label and attached it with the bar
                g2d.setColor(Color.DARK_GRAY);
                g2d.setFont(new Font(sansSerif, Font.BOLD, 12));
                String name = ItemTranslationManager.returnName(rd.getItem_id(), RetrieveRecord.getLang());
                String label = String.format("%s (%d)", name, pityValue);
                g2d.drawString(label, iconSize + barWidth + 30, currentY + 25);

                i++;
            }
        }

        /**
         * A helper function displaying the detail of the pity in a new window
         * @param rd gacha record
         */
        private void popWindow(GachaRecord rd) {
            String string = ItemTranslationManager.returnName(rd.getItem_id(), RetrieveRecord.getLang()) + "\n" +
                    rd.getItem_id() + "\n" + rd.getTime() + "\n" + rd.getItem_type();// concate the string

            JOptionPane.showMessageDialog(this, string,
                    GUILanguageManager.get("GUI.chart.details.title"), JOptionPane.INFORMATION_MESSAGE);
        }

        /**
         * Simply draws the trailing bar
         * @param g2d painting component
         * @param pityValue trailing pity in integer
         * @param y y-coordinate, important to keep chart organized
         * @param iconSize size of icon, need this to create an empty placeholder
         * @param maxBarWidth size parameter
         */
        private void drawTrailingBar(Graphics2D g2d, int pityValue, int y, int iconSize, int maxBarWidth) {
            // size configuration
            int barWidth = (int) ((pityValue / 90.0) * maxBarWidth);
            int barX = iconSize + 20; // align the bar with 20 more pixels because no icon is inserted
            int barY = y + 10;

            // draw the bar
            g2d.setColor(Color.LIGHT_GRAY);
            Rectangle rect = new Rectangle(barX, barY, Math.max(barWidth, 5), 20);
            g2d.fillRoundRect(rect.x, rect.y, rect.width, rect.height, 10, 10);

            // draw the Label
            g2d.setColor(Color.DARK_GRAY);
            g2d.setFont(new Font(sansSerif, Font.BOLD, 12));

            // finally, add them!!!!!!
            String fullLabel = GUILanguageManager.get("label.trailing") + " (" + pityValue + ")";
            g2d.drawString(fullLabel, barX + barWidth + 10, y + 25);
        }
    }

}
