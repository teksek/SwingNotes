import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.jthemedetecor.OsThemeDetector;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.Theme;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Objects;
import java.util.prefs.Preferences;
import java.util.List;

public class Main {
    private static JFrame window;
    private static Preferences prefs;
    public static JTabbedPane tabbedPane;
    private static JLabel statusBar;
    private static FileManager fileManager;
    private static Timer autoSaveTimer;
    private static WindowFocusListener autoSaveFocusListener = null;

    private static void makeWindow() {
        // -=- podstawowa inicjalizacja programu -=-
        setupBasicWindow();
        initLocalization();
        fileManager = new FileManager(window);

        // -=- komponenty UI -=-
        setupStatusBar();
        setupTabbedPane();

        // -=- konfiguracja okna -=-
        window.setJMenuBar(new SwingNotesMenuBar(fileManager, prefs, window));     // tworzenie okna z elementów
        setupWindowListeners();

        // -=- start aplikacji -=-
        addNewTab(); // pierwsza zakładka
        activateAutosave();
        finalizeWindowSetup();
    }

    private static void setupBasicWindow() {
        window = new JFrame("SwingNotes");
        window.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        window.setIconImage(new ImageIcon(Objects.requireNonNull(Main.class.getResource("/icon.png"))).getImage()); //ustawienie ikony programu
    }

    private static void initLocalization() {
        String savedLang = prefs.get("language", "system");
        if (savedLang.equals("system")) I18n.loadBundle(Locale.getDefault());
        else I18n.loadBundle(Locale.forLanguageTag(savedLang));
    }

    private static void setupStatusBar() {
        statusBar = new JLabel(I18n.get("statusBar.format", 0, 0, 1));
        statusBar.setBorder(BorderFactory.createEmptyBorder(3, 6, 3, 6));
    }

    private static void setupTabbedPane() {
        tabbedPane = new JTabbedPane();
        tabbedPane.addChangeListener(e -> updateActiveTabUI());

        setupTabReordering();
        setupDragAndDrop();
    }

    private static void setupTabReordering() {
        final int[] dragTabIndex = {-1}; //ustawienie indexu niemożliwego
        tabbedPane.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent cursorLocation) {
                // gdy kliknięto zakładkę pobierany jest na podstawie lokalizacji kursora myszy index przenoszonej zakładki
                dragTabIndex[0] = tabbedPane.indexAtLocation(cursorLocation.getX(), cursorLocation.getY());
            }

            @Override
            public void mouseReleased(MouseEvent e) { //zakończenie przenoszenia zakładki
                dragTabIndex[0] = -1; //ponowne ustawienie niemożliwego indexu zapobiegającego bugom
                tabbedPane.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)); //przywrócenie defaultowego kursora po upuszczeniu zakładki
            }
        });

        tabbedPane.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (dragTabIndex[0] != -1) { // jeśli mysz jest ustawiona na jakiejkolwiek zakładce
                    int currentTargetIndex = tabbedPane.indexAtLocation(e.getX(), e.getY()); // ustawienie docelowej pozycji zakładki
                    if (currentTargetIndex != -1 && currentTargetIndex != dragTabIndex[0]) { //jeśli kursor jest nad inną zakładką
                        reorderTab(dragTabIndex[0], currentTargetIndex); //zamiana zakładek miejscem w locie (żeby użytkownik widział, co się dzieje, a nie dopiero po upuszczeniu)
                        dragTabIndex[0] = currentTargetIndex; //zmiana indeksu ciągniętej zakładki
                    }
                    tabbedPane.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); //ustawienie kursora na wskazującego
                }
            }
        });
    }

    private static void setupDragAndDrop() {
        TransferHandler dropHandler = new TransferHandler() { // implementacja mechanizmu drag-and-drop
            @Override
            public boolean canImport(TransferSupport support) {
                return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
            }

            @SuppressWarnings("unchecked")
            @Override
            public boolean importData(TransferSupport support) {
                try {
                    List<File> files = (List<File>) support.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    if (!files.isEmpty()) {
                        fileManager.openFile(getActiveTab(), files.getFirst().getAbsolutePath(), prefs);
                    }
                    return true;
                } catch (Exception e) {
                    return false;
                }
            }
        };
        tabbedPane.setTransferHandler(dropHandler);     // dodanie obsługi mechanizmu drag-and-drop plików na header (tam, gdzie zakładki i menu)
    }

    private static void setupWindowListeners() {
        window.addWindowListener(new WindowAdapter() { // dodanie obsługi wyłączenia programu
            @Override
            public void windowClosing(WindowEvent e) {
                closeApp();
            }
        });
    }

    private static void finalizeWindowSetup() {
        window.add(tabbedPane, BorderLayout.CENTER);
        window.add(statusBar, BorderLayout.SOUTH);

        setThemeOnLaunch(getActiveTab().getTextArea()); // ustawienie motywów (UI i syntax) po utworzeniu wszystkich komponentów

        window.setSize(new Dimension(800, 600));
        window.setLocationRelativeTo(null);
        window.setVisible(true);
    }

    public static void activateAutosave() {
        if (autoSaveTimer != null) autoSaveTimer.stop(); autoSaveTimer = null; //zabijamy stary timer, jeśli jakiś działał
        if (autoSaveFocusListener != null) window.removeWindowFocusListener(autoSaveFocusListener); autoSaveFocusListener = null; //czyścimy stary listener, jeśli był
        String trigger = prefs.get("autosave-trigger", "never");
        if (trigger.equals("never")) return; //użytkownik nie zgodził się na autosave
        else if (trigger.equals("onFocusChange")) { //użytkownik wybrał autosave po utracie fokusu
            autoSaveFocusListener = new WindowAdapter() {
                @Override
                public void windowLostFocus(WindowEvent e) {
                    saveAllModifiedFiles();
                }
            };
            window.addWindowFocusListener(autoSaveFocusListener);
        } else {  //to znaczy, że użytkownik wybrał autosave po upływie danej ilości minut
            int delayValue = Integer.parseInt(trigger);
            autoSaveTimer = new Timer(delayValue * 60 * 1000, e -> saveAllModifiedFiles());
            autoSaveTimer.start();
        }
    }

    private static void saveAllModifiedFiles() {
        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
            Tab tab = (Tab) tabbedPane.getComponentAt(i);
            if (tab.isFileChanged() && tab.getFile() != null) {
                fileManager.saveFile(tab);
            }
        }
    }

    private static void reorderTab(int indexFrom, int indexTo) {
        Component component = tabbedPane.getComponentAt(indexFrom);   //skopiowanie treści (wnętrza) zakładki
        String title = tabbedPane.getTitleAt(indexFrom);              //skopiowanie tytułu zakładki
        Component tabComp = tabbedPane.getTabComponentAt(indexFrom);  //skopiowanie tytułu zakładki

        tabbedPane.remove(indexFrom); //usunięcie zakładki z wcześniejszego miejsca
        tabbedPane.insertTab(title, null, component, null, indexTo);  //wstawienie całej wcześniej skopiowanej zakładki w nowe miejsce
        tabbedPane.setTabComponentAt(indexTo, tabComp); //przywrócenie tytułu zakładki
        tabbedPane.setSelectedIndex(indexTo);           //utrzymanie zaznaczenia na przenoszonej zakładce
    }

    public static void addNewTab() {
        Tab tab = new Tab(fileManager, prefs, statusBar);
        tabbedPane.addTab(tab.getTitle(), tab);
        tabbedPane.setSelectedIndex(tabbedPane.getTabCount() - 1);
        applyTheme(isDarkFromPrefs(), tab.getTextArea());
    }

    public static Tab getActiveTab() {
        return (Tab) tabbedPane.getSelectedComponent();
    }

    public static void updateActiveTabUI() {
        int index = tabbedPane.getSelectedIndex();
        if (index >= 0) {
            tabbedPane.setTitleAt(index, getActiveTab().getTitle());
            window.setTitle("SwingNotes - " + getActiveTab().getTitle());

            // aktualizacja statusBaru przy zmianie zakładki
            int chars = getActiveTab().getCharsCount();
            int words = getActiveTab().getWordCount();
            int lines = getActiveTab().getLinesCount();
            statusBar.setText(I18n.get("statusBar.format", chars, words, lines));
        } else {
            window.setTitle("SwingNotes");
        }
    }

    public static void closeApp() {
        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
            Tab tab = (Tab) tabbedPane.getComponentAt(i);
            tabbedPane.setSelectedIndex(i); //pokazuje zakładkę, aby użytkownik wiedział, o który plik jest pytany
            if (!fileManager.canDiscardChanges(tab, "dialog.saveChangesBeforeClosing.msg")) return;
        }
        System.exit(0);
    }

    public static void setEditorTheme(String themeFileName, RSyntaxTextArea textArea) {
        try {
            Theme theme = Theme.load(Main.class.getResourceAsStream("/org/fife/ui/rsyntaxtextarea/themes/" + themeFileName));
            theme.apply(textArea);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void applyTheme(boolean isDark, RSyntaxTextArea textArea) {
        if (isDark) {
            FlatDarkLaf.setup();
            if (textArea != null) setEditorTheme(prefs.get("syntaxTheme", "dark.xml"), textArea);
        } else {
            FlatLightLaf.setup();
            if (textArea != null) setEditorTheme(prefs.get("syntaxTheme", "default.xml"), textArea);
        }
    }

    public static boolean isDarkFromPrefs() {
        String theme = prefs.get("theme", "system");
        if (theme.equals("dark")) return true;
        if (theme.equals("light")) return false;
        return OsThemeDetector.getDetector().isDark();
    }

    private static void setThemeOnLaunch(RSyntaxTextArea textArea) {
        OsThemeDetector detector = OsThemeDetector.getDetector(); //sprawdza, czy system ma ciemny motyw i automatycznie dobiera odpowiedni.
        String theme = prefs.get("theme", "system");

        switch (theme) {
            case "dark" -> applyTheme(true, textArea);
            case "light" -> applyTheme(false, textArea);
            default -> {
                applyTheme(detector.isDark(), textArea);

                detector.registerListener(isDark -> SwingUtilities.invokeLater(() -> {
                    applyTheme(isDark, getActiveTab().getTextArea());
                    if (window != null) SwingUtilities.updateComponentTreeUI(window); //odświeża wszystkie komponenty, żeby motyw zadziałał bez restartu
                }));
            }
        }
    }

    public static void main(String[] args) {
        prefs = Preferences.userNodeForPackage(Main.class);
        applyTheme(isDarkFromPrefs(), null);

        SwingUtilities.invokeLater(Main::makeWindow);
    }
}
