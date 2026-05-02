package Renderer.Components;

import Model.RecordTemplate.GachaRecord;
import Renderer.Charts.RetrieveRecord;
import Renderer.ServiceAction.ExcelWriting;
import Renderer.ServiceAction.LoadDirectory;
import Renderer.ServiceAction.Search;
import Storage.ReadWrite.ReadRecord;
import Storage.ReadWrite.StoreRecord;
import Storage.Configuration.StorageConfig;
import Utilities.MergeRecords.MergeHelper;
import Core.Genshin.GenshinPlayerData;
import Core.Genshin.GenshinRecordFetcher;
import Core.Path.PathValidator;
import I18n.General.GeneralMessageManager;
import I18n.GUI.GUILanguageManager;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import Renderer.Window.MainWindow;

/**
 * The central controller class of the whole program.
 * @course Teacher: Daniel Vriesinga
 * @author Frank Fan at 2026/04/27
 *
 */
public class ProcessController extends JPanel {

    // all instances
    private Map<String, GenshinPlayerData> loadedData = new LinkedHashMap<>();// loaded player data map
    private GenshinPlayerData activePlayer;
    private final MainWindow mainWindow;
    // a little bit of swing
    private JComboBox<String> uidSelector;
    private JTextField searchField;
    private JButton exportExcelBtn;
    private JButton saveJsonBtn;
    // some objects
    private String currentView = "welcome";
    private static final String dialogErrorImport = "dialog.error.import.title";
    private static final String statusErrorImport = "status.import_failed";

    /**
     * Public parametric constructor to instantiate the {@link MainWindow} object.
     * acting as the controller and stores a reference to the window. UI components are not available yet atp.
     * @param mainWindow the application's main window, should not be null
     */
    public ProcessController(MainWindow mainWindow) {
        this.mainWindow = mainWindow;
    }

    /**
     * Uses {@link JFileChooser} to accept either a JSON file or a directory,
     * loads all player data found with the path via {@link #loadLocalData(java.nio.file.Path)}.
     */
    protected void loadLocal() {
        // configure a new file chooser
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle(GUILanguageManager.get("filechooser.load.title"));
        fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        // set filter -> directory JSON or JSON file
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("JSON files", "json"));
        fc.setAcceptAllFileFilterUsed(true);
        // check if there's existed directory, if not, created a new one.
        fc.setCurrentDirectory(StorageConfig.dataPath.toFile().exists()
                ? StorageConfig.dataPath.toFile() : new File(System.getProperty("user.home")));

        if (fc.showOpenDialog(mainWindow) == JFileChooser.APPROVE_OPTION) {
            File selected = fc.getSelectedFile();
            if (selected == null) return;// prevent null path
            java.nio.file.Path target = selected.isDirectory() ? selected.toPath() : selected.toPath().getParent();
            if (target != null) loadLocalData(target);
        }
    }

    /**
     * Uses {@link JFileChooser} to only accept the JSON files,
     * the selected file is imported through {@link Core.Genshin.GenshinRecordFetcher}
     * Exceptions:
     * {@link java.io.IOException}
     * {@link Storage.ReadWrite.ReadRecord.DirectoryError}
     * {@link RuntimeException} f
     */
    protected void onImportFile() {
        // same as loadLocal, a new file choose is used with filters
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("JSON files", "json"));
        if (fc.showOpenDialog(mainWindow) != JFileChooser.APPROVE_OPTION) return;
        File file = fc.getSelectedFile();
        setStatus(MessageFormat.format(GUILanguageManager.get("status.importing"), file.getName()));// set status

        // try-catch block
        try {
            // passing both imported path and local data path, fetching all records
            GenshinRecordFetcher fetcher = new GenshinRecordFetcher(file.toPath(), StorageConfig.dataPath);
            mergeIntoLoaded(fetcher.fetchAllData());
            setStatus(MessageFormat.format(GUILanguageManager.get("status.imported"), file.getName()));

            // multiple catching to handle all cases exception
        } catch (PathValidator.PathException | IOException | GenshinRecordFetcher.EmptyPathException |
                 GenshinRecordFetcher.EmptyListException | GenshinRecordFetcher.MissingComponentException |
                 ReadRecord.DirectoryError ex) {
            showError(GUILanguageManager.get(dialogErrorImport), ex.getMessage());
            setStatus(GUILanguageManager.get(statusErrorImport));
        } catch (RuntimeException _) {
            showError(GUILanguageManager.get(dialogErrorImport),
                    GUILanguageManager.get("dialog.error.import.unsupported"));
            setStatus(GUILanguageManager.get(statusErrorImport));
        }
    }

    /**
     * Exports the active player's gacha records to an Excel file using {@link Renderer.ServiceAction.ExcelWriting#writingAction}
     * Does nothing if {@link #activePlayer} is null
     */
    protected void onExportExcel() {
        if (activePlayer == null) return;

        // player is not null, call the action class to perform writing
        setStatus(GUILanguageManager.get("status.exporting_excel"));
        String val = ExcelWriting.writingAction(activePlayer, GeneralMessageManager.getCurrentLang());
        JOptionPane.showMessageDialog(mainWindow,
                val,// message
                GUILanguageManager.get("dialog.export.title"),
                JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Persists the active player's data to the configured storage path via {@link Storage.ReadWrite.StoreRecord#savePlayerData}.
     * Does nothing if {@link #activePlayer} is null
     */
    protected void onSaveJson() {
        if (activePlayer == null) return;

        // player is not null, perform this
        setStatus(GUILanguageManager.get("status.saving_json"));
        try {
            StoreRecord.savePlayerData(activePlayer, StorageConfig.dataPath);

            // dialogs..
            setStatus(MessageFormat.format(GUILanguageManager.get("status.saved_json"), StorageConfig.dataPath));
            JOptionPane.showMessageDialog(mainWindow,
                    MessageFormat.format(GUILanguageManager.get("dialog.save.message"), StorageConfig.dataPath),
                    GUILanguageManager.get("dialog.save.title"),
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            showError(GUILanguageManager.get("dialog.error.save.title"), ex.getMessage());
            setStatus(GUILanguageManager.get("status.save_failed"));
        }
    }

    /**
     * Reads the UID from {@link #searchField} and use {@link Renderer.ServiceAction.Search#searchData} to find the player
     * If no match is found, an error dialog is shown
     */
    protected void onSearch() {
        // get and check if uid is not null or empty
        String uid = searchField.getText().trim();
        if (uid.isEmpty()) {
            JOptionPane.showMessageDialog(mainWindow, GUILanguageManager.get("dialog.error.search.noUID"));
            return;
        }

        Map<String, GenshinPlayerData> result = Search.searchData(uid, loadedData);
        // nothing is found
        if (result.isEmpty()) {
            showError(GUILanguageManager.get("search.not_found.title"),
                    MessageFormat.format(GUILanguageManager.get("search.not_found.message"), uid));
            setStatus(MessageFormat.format(GUILanguageManager.get("search.status.not_found"), uid));
        } else {// display the result
            String found = result.keySet().iterator().next();
            uidSelector.setSelectedItem(found);
            setStatus(MessageFormat.format(GUILanguageManager.get("search.status.found"), found));
        }
    }

    /**
     * Responds to UID selection change in {@link #uidSelector}, then rerender for the selected player.
     */
    public void onUIDSelected() {
        Object item = uidSelector.getSelectedItem();
        if (item == null) return;// guarded here
        activePlayer = loadedData.get(item.toString());
        if (activePlayer == null) return;

        // enable export and save action
        exportExcelBtn.setEnabled(true);
        saveJsonBtn.setEnabled(true);
        refreshActiveView();
    }

    /**
     * Switches to the statistics overview panel for the active player. Falls back to the welcome panel if no player is loaded.
     */
    public void showOverview() {
        if (activePlayer == null) { mainWindow.showWelcome(); return;}// fallback
        currentView = "overview";
        mainWindow.showOverview(activePlayer);// show HTML
    }

    /**
     * Switches to the Swing-rendered chart panel for the active player. Falls back to the welcome panel if no player is loaded.
     */
    public void showChart() {
        if (activePlayer == null) { mainWindow.showWelcome(); return; }
        currentView = "chart_swing";
        mainWindow.showSwingChart(activePlayer);
    }

    /**
     * Switches to the JavaFX-rendered chart panel for the active player. Falls back to the welcome panel if no player is loaded.
     */
    public void showJfxChart() {
        if (activePlayer == null) { mainWindow.showWelcome(); return; }
        currentView = "chart_jfx";
        mainWindow.showJfxChart(activePlayer);
    }



    /**
     * Re-renders the panel for {@link #activePlayer}
     */
    private void refreshActiveView() {
        // re-render with the current ACTIVE player
        if ("chart_jfx".equals(currentView)) { mainWindow.showJfxChart(activePlayer); return; }
        if ("chart_swing".equals(currentView)) { mainWindow.showSwingChart(activePlayer); return; }
        mainWindow.showOverview(activePlayer);
        currentView = "overview";
    }

    /**
     * Loads all player JSON files from {@link Renderer.ServiceAction.LoadDirectory},
     * then merges the result into a map
     * @param dir the directory  path
     */
    private void loadLocalData(java.nio.file.Path dir) {
        setStatus(MessageFormat.format(GUILanguageManager.get("status.loading"), dir));

        // try-catch for reading directories
        try {
            LoadDirectory.LoadResult lr = new LoadDirectory().getFromDirectory(dir);
            mergeIntoLoaded(lr.data());
            setStatus(lr.message());
        } catch (Exception e) {
            setStatus(MessageFormat.format(GUILanguageManager.get("status.load_failed"), e.getMessage()));
        }
    }

    /**
     * Merges the player data into {@link #loadedData} and refreshes the dropdown {@link #uidSelector}
     * @param incoming a map of records (UID → player data)
     */
    private void mergeIntoLoaded(Map<String, GenshinPlayerData> incoming) {
        // lambda with for each hehehe
        incoming.forEach((uid, fresh) -> {
            GenshinPlayerData previous = loadedData.get(uid);
            // no same player record is found, add it to the loaded data
            if(previous == null) {
                loadedData.put(uid, fresh);
                return;
            }
            // duplicate found, call the merge helper
            List<GachaRecord> mergedData = MergeHelper.mergeData(fresh.records(), previous.records());
            loadedData.put(uid, new GenshinPlayerData(fresh.info(), new ArrayList<>(mergedData)));
        });

        // uid selecting, reload the dropdown
        String pre = (String) uidSelector.getSelectedItem();
        uidSelector.removeAllItems();
        loadedData.keySet().forEach(uidSelector::addItem);
        if (pre != null && loadedData.containsKey(pre)) {
            uidSelector.setSelectedItem(pre);
        } else if (!loadedData.isEmpty()) {
            uidSelector.setSelectedIndex(0);
        }
    }

    /**
     * Displays a localized error dialog, used by {@link #mainWindow}.
     * @param title the localized dialog title
     * @param msg the localized error message body
     */
    private void showError(String title, String msg) { JOptionPane.showMessageDialog(mainWindow, msg, title, JOptionPane.ERROR_MESSAGE);}

    /**
     * Set a status message to the sidebar {@link MainWindow#setStatus(String)}.
     * @param message the status text in string
     */
    private void setStatus(String message) { mainWindow.setStatus(message);}



    /**
     * Wires the sidebar's interactive control
     * @param uidSelector the UID dropdown selector
     * @param searchField the search text field
     * @param exportExcelBtn the Excel export button
     * @param saveJsonBtn the JSON save button
     */
    public void bindUI(JComboBox<String> uidSelector, JTextField searchField, JButton exportExcelBtn, JButton saveJsonBtn) {
        this.uidSelector = uidSelector;
        this.searchField = searchField;
        this.exportExcelBtn = exportExcelBtn;
        this.saveJsonBtn = saveJsonBtn;
        uidSelector.addActionListener(e -> onUIDSelected());
    }

    /**
     * Re-render the active view if the language is changed.
     * <p>Returns if {@link #activePlayer} is {@code null} to void {@link NullPointerException},
     * as there is nothing rendered to refresh yet.
     */
    public void refreshContentLanguage() {
        if (activePlayer == null) return; // nothing initialized yet, skip safely

        RetrieveRecord.setLang(GUILanguageManager.getCurrentLang());
        GeneralMessageManager.load(GUILanguageManager.getCurrentLang());
        refreshActiveView(); // just re-render the current view with new lang
    }

    /**
     * Triggered by {@link MainWindow#refreshContentLanguage()} if the language is changed.
     * This ensures the main window's labels are also updated.
     *
     * <p>Since sidebar can't call {@link MainWindow} directly to refresh panel,
     * it goes through the controller instead.
     * <p>Use this design to avoid {@link StackOverflowError} by breaking the circular dependency.
     */
    public void triggerMainWindowRefresh() {
        mainWindow.refreshContentLanguage();
    }
}