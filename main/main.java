package main;

import javax.swing.*;
import Renderer.MainWindow;

public class main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Optional: Set system look and feel for a more native window style
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}

            new MainWindow().setVisible(true);
        });
    }

}
