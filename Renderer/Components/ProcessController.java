package Renderer.Components;

import Renderer.ServiceAction.ExcelWriting;
import Renderer.ServiceAction.LoadDirectory;
import Renderer.ServiceAction.Search;
import Storage.ExcelExport.ExcelExporter;
import Storage.ReadWrite.ReadRecord;
import Storage.ReadWrite.StoreRecord;
import Storage.StorageConfig;
import core.Genshin.GenshinPlayerData;
import core.Genshin.GenshinRecordFetcher;
import core.PathValidator;
import i18n.General.GeneralMessageManager;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import Renderer.MainWindow;

public class ProcessController {

    private Map<String, GenshinPlayerData> loadedData = new LinkedHashMap<>();
    private GenshinPlayerData activePlayer;
    private JComboBox<String> uidSelector;
    private JTextField searchField;
    private JButton exportExcelBtn;
    private JButton saveJsonBtn;
    private final MainWindow mainWindow;
    private String currentView = "welcome";

    public ProcessController(MainWindow mainWindow) {
        this.mainWindow = mainWindow;
    }

    // control method of the program
    protected void loadLocal() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Load local data");
        fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("JSON files", "json"));
        fc.setAcceptAllFileFilterUsed(true);
        fc.setCurrentDirectory(StorageConfig.dataPath.toFile().exists()
                ? StorageConfig.dataPath.toFile() : new File(System.getProperty("user.home")));
        if (fc.showOpenDialog(mainWindow) == JFileChooser.APPROVE_OPTION) {
            File selected = fc.getSelectedFile();
            if (selected == null) return;
            java.nio.file.Path target = selected.isDirectory() ? selected.toPath() : selected.toPath().getParent();
            if (target != null) loadLocalData(target);
        }
    }

    protected void onImportFile() {

        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("JSON files", "json"));
        if (fc.showOpenDialog(mainWindow) != JFileChooser.APPROVE_OPTION) return;
        File file = fc.getSelectedFile();
        setStatus("Importing " + file.getName() + "...");

        try {
            GenshinRecordFetcher fetcher = new GenshinRecordFetcher(file.toPath(), StorageConfig.dataPath);
            mergeIntoLoaded(fetcher.fetchAllData());
            setStatus("Imported: " + file.getName());
        } catch (PathValidator.PathException | IOException |
                 GenshinRecordFetcher.EmptyPathException |
                 GenshinRecordFetcher.EmptyListException |
                 GenshinRecordFetcher.MissingComponentException ex) {
            showError("Import failed", ex.getMessage());
            setStatus("Import failed.");
        } catch (ReadRecord.DirectoryError e) {
            showError("Import failed", e.getMessage());
            setStatus("Import failed.");
        } catch (RuntimeException ex) {
            showError("Import failed", "Unsupported JSON format. Use \"Load local data\" for saved local files.");
            setStatus("Import failed.");
        }
    }

    protected void onExportExcel() {
        if (activePlayer == null) return;
        setStatus("Exporting Excel...");
        String val = ExcelWriting.writingAction(loadedData, GeneralMessageManager.getCurrentLang());
        JOptionPane.showMessageDialog(mainWindow, val, "Export complete", JOptionPane.INFORMATION_MESSAGE);
    }

    protected void onSaveJson() {
        if (activePlayer == null) return;
        setStatus("Saving JSON...");
        try {
            StoreRecord.savePlayerData(activePlayer, StorageConfig.dataPath);
            setStatus("Saved JSON to: " + StorageConfig.dataPath);
            JOptionPane.showMessageDialog(mainWindow,
                    "JSON saved to:\n" + StorageConfig.dataPath,
                    "Save complete", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            showError("Save failed", ex.getMessage());
            setStatus("Save failed.");
        }
    }

    protected void onSearch() {
        String uid = searchField.getText().trim();
        if (uid.isEmpty() || loadedData.isEmpty()) return;
        Map<String, GenshinPlayerData> result = Search.binarySearch(uid, loadedData);
        if (result.isEmpty()) {
            showError("Not found", "No record for UID: " + uid);
            setStatus("UID not found: " + uid);
        } else {
            String found = result.keySet().iterator().next();
            uidSelector.setSelectedItem(found);
            setStatus("Found: " + found);
        }
    }

    public void onUIDSelected() {
        Object sel = uidSelector.getSelectedItem();
        if (sel == null) return;
        activePlayer = loadedData.get(sel.toString());
        if (activePlayer == null) return;
        exportExcelBtn.setEnabled(true);
        saveJsonBtn.setEnabled(true);
        refreshActiveView();
    }

    private void showError(String title, String msg) {
        JOptionPane.showMessageDialog(mainWindow, msg, title, JOptionPane.ERROR_MESSAGE);
    }

    private void refreshActiveView() {
        if ("chart_jfx".equals(currentView)) {
            mainWindow.showJfxChart(activePlayer);
            return;
        }
        if ("chart_swing".equals(currentView)) {
            mainWindow.showSwingChart(activePlayer);
            return;
        }
        mainWindow.showOverview(activePlayer);
        currentView = "overview";
    }

    public void showOverview() {
        if (activePlayer == null) {
            mainWindow.showWelcome();
            return;
        }
        currentView = "overview";
        mainWindow.showOverview(activePlayer);
    }

    public void showChart() {
        if (activePlayer == null) {
            mainWindow.showWelcome();
            return;
        }
        currentView = "chart_swing";
        mainWindow.showSwingChart(activePlayer);
    }

    public void showJfxChart() {
        if (activePlayer == null) {
            mainWindow.showWelcome();
            return;
        }
        currentView = "chart_jfx";
        mainWindow.showJfxChart(activePlayer);
    }


    private void loadLocalData(java.nio.file.Path dir) {
        setStatus("Loading from " + dir + "...");
        try {
            LoadDirectory.LoadResult res = new LoadDirectory().getFromDirectory(dir);
            mergeIntoLoaded(res.data());
            setStatus(res.message());
        } catch (Exception ex) {
            setStatus("Load failed: " + ex.getMessage());
        }
    }

    private void mergeIntoLoaded(Map<String, GenshinPlayerData> incoming) {
        loadedData.putAll(incoming);
        String prev = (String) uidSelector.getSelectedItem();
        uidSelector.removeAllItems();
        loadedData.keySet().forEach(uidSelector::addItem);
        if (prev != null && loadedData.containsKey(prev)) uidSelector.setSelectedItem(prev);
        else if (!loadedData.isEmpty()) uidSelector.setSelectedIndex(0);
    }


    public void bindUI(JComboBox<String> uidSelector,
                       JTextField searchField,
                       JButton exportExcelBtn,
                       JButton saveJsonBtn) {

        this.uidSelector = uidSelector;
        this.searchField = searchField;
        this.exportExcelBtn = exportExcelBtn;
        this.saveJsonBtn = saveJsonBtn;
        uidSelector.addActionListener(e -> onUIDSelected());
    }

    private void setStatus(String message) {
        mainWindow.setStatus(message);
    }

}
