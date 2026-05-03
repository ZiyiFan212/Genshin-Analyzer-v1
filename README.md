# Genshin-Analyzer-v1
### Wish History Viewer & Statistics Tool
A Java desktop application for analyzing Genshin Impact gacha (wish) records.  
Built with **Swing + JavaFX**, supporting dual-language switching (English / 简体中文), and local data inspection.

---

## Project Structure Overview

Here's the outline of the structure, helping navigate throughout the program.

```
src/
├── Renderer/                        # All GUI components and related service actions
│   ├── Window
│   │   └── MainWindow.java          # The entry point of the GUI
│   ├── Components/
│   │   ├── SidebarPanel.java        # Functional sidebar with all actions
│   │   ├── ProcessController.java   # Controller class for bridging UI and service 
│   │   └── Styling.java             # Constant color palette for UI
│   ├── Charts/
│   │   ├── SwingChart.java          # Swing-based pity horizontal bar chart
│   │   ├── JFXChart.java            # JavaFX-based pity horizontal bar chart 
│   │   └── RetrieveRecord.java      # Shared chart data layer with pity calculation
│   └── ServiceAction/
│       ├── Search.java              # Searches the player data with UID
│       ├── LoadDirectory.java       # Loads all JSON files from the local directory
│       └── ExcelWriting.java        # Writes and export Excel to the directory
│
├── core/
│   ├── Genshin/
│   │   ├── GenshinPlayerData.java     # Holds player info and records
│   │   ├── GenshinRecordFetcher.java  # Validates imported JSON and fetch all records
│   │   ├── GameService.java           # Contracting the fetching service 
│   │   └── Statistics.java            # Gacha statistics algorithm
│   ├── Interface/
│   │   └── GenshinPlayerData.java     # An interface implemented by the player data
│   └── Path/
│       └── PathValidator.java         # Validates import file's path
│
├── Model/
│   ├── RecordTemplate/
│   │      ├── GachaRecord.java           # Holds a single wish record
│   │      └── InfoRecord.java            # An immutable class for storing the record information
│   └── Genshin/
│       └── GenshinGachaStatSummary.java  # Stores computed statistics
│
├── Storage/
│   ├── Excel Export/
│   │   └── ExcelExporter.java       # Writes player data to an Excel
│   ├── ReadWrite/
│   │   ├── ReadRecord.java          # Reads saved JSON from local storage
│   │   └── StoreRecord.java         # Writes player data to local JSON
│   └── Configuration/
│       └── StorageConfig.java       # Initializes storage path 
│
├── Utilities/
│   ├── StandardItemDetector/
│   │   └──Detector.java             # An utility class for detecting standard set item
│   └── MergeRecords/
│       └──MergeHelper.java          # Generic merge new and old data + insertion sort
│
├── i18n/
│   ├── Items/
│   │   ├── ItemTranslationManager.java      # Manages item translation (en/zh)
│   │   ├── Character.json                   # A JSON file containing character translation
│   │   └── Weapon.json                      # A JSON file containing weapon translation
│   ├── GUI/
│   │   ├── GUILanguageManager.java          # Manages UI string bundles (en/zh)
│   │   └── GUImessage_en/zh.properties      # UI string resources
│   └── General/
│       ├── GeneralMessageManager.java       # Manages service string bundles (en/zh)
│       └── message_en/zh.properties         # Content string resources
│
└── Assets/
    ├── AssetsManager.java           # Resolves icon paths by item ID
    └── Character & WeaponIcons/     # Contains all item icons
```

---

## How to Run

### Requirements
Current version needs to be run on the IDE. Future release will bundle all resources
with a jar file.
- JDK 16 or above
- JavaFX SDK (required for JFX chart tab)
- Apache POI (required for Excel writing)
- Jackson (required for parsing JSON files)

### Steps
1. Clone the project
2. Open in IntelliJ IDEA, Eclipse, or VS Code
3. Add all resources listed above to the module path if needed
4. Run `Main.java` 

---

## Features

### Data Import
- **Import JSON** — imports a Genshin wish export JSON file
- **Load local data** — loads previously saved `.json` files from the local directory
- Duplicate records are automatically deduplicated by record ID
- Records from multiple devices/exports are merged without data loss
- The local directory can be found via /User/GenshinAnalyzer

### Views
- **Overview** — displays total wishes, primogems spent, 5-star counts, 50/50 win rate, and average pity in HTML format.
- **Pity Chart (Swing)** — bar chart showing pity count per 5-star pull per banner, with trailing pity indicator
- **Pity Chart (JFX)** — same bar chart but is rendered in JavaFX

### Export
- **Save as JSON** — persists current player data to local storage
- **Export as Excel** — exports full wish history to `.xlsx`

### Language Support
- Able to switch between **English** and **简体中文** at runtime via the 🌐 button next to the title
- All UI labels, status messages, dialogs, and chart content will update

---

## Other
Pseudo code file is also uploaded, named as "Pseudo code."
Unit and system test tables are submitted separately along with the zip. file. This is for convenient navigation.
Being a gamer and interested in programming, this project far exceeds the final assignment. In the forseeble future,
the author will continously maintain and update this program. An upgrade list is attached below:
1. Use proxy to extract the API key for full automation
2. Refactor current program with a cleaner process
3. Abandon Swing/JavaFX, designing a more interactive interface
---

*Developed as a final assignment project. All gacha data processing is performed locally — no network requests are made.*
