package Main;

import javax.swing.*;
import Renderer.Window.MainWindow;
import Storage.Configuration.StorageConfig;

import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * <p>The entry point of the program, log any errors in operating and saved to local
 *
 * @course Teacher: Daniel Vriesinga
 * @author Frank Fan at 2026/04/28
 * */

public class Main {// start class


    public static void main(String[] args) {// start main

        SwingUtilities.invokeLater(() -> {
            // create log writer and file handler
            final Logger logger = Logger.getLogger("My Log");
            final FileHandler fh;

            // https://stackoverflow.com/questions/15758685/how-to-write-logs-in-text-file-when-using-java-util-logging-logger
            try {
                // configure the log and file handler in this block
                StorageConfig.initialize();
                fh = new FileHandler((StorageConfig.logPath.resolve("Genshin-analyzer log")).toString());
                logger.addHandler(fh);
                SimpleFormatter formatter = new SimpleFormatter();// create a human readable formatter for the lop
                fh.setFormatter(formatter);

                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception exception) {
                logger.log(Level.SEVERE, "Error caught in program running!\n", exception);
            }

            // create a new window
            new MainWindow().setVisible(true);
        });
    }

}
