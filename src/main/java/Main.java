import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.jthemedetecor.OsThemeDetector;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
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
    private static JSplitPane splitPane;
    private static JEditorPane markdownPreview;
    private static boolean isPreviewVisible = false;
    public static JCheckBoxMenuItem mdPreviewItem;

    private static void makeWindow() {
        // -=- podstawowa inicjalizacja programu -=-
        setupBasicWindow();
        initLocalization();
        fileManager = new FileManager(window, prefs);

        // -=- komponenty UI -=-
        setupStatusBar();
        setupTabbedPane();

        // -=- konfiguracja okna -=-
        window.setJMenuBar(new AppMenuBar(fileManager, prefs, window));     // tworzenie okna z elementów
        setupWindowListeners();

        // -=- start aplikacji -=-
        if(!prefs.getBoolean("restoreSession", false)) addNewTab(); // pierwsza zakładka, jeśli nie ma zapisywania sesji włączonego
        else restoreSession();
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
                        fileManager.openFile(getActiveTab(), files.getFirst().getAbsolutePath());
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

    private static void restoreSession() {
        String session = prefs.get("open-files-session", "");
        if(!session.isEmpty()) {
            String[] paths = session.split(";");
            for (String path : paths) {
                if(!path.isEmpty()) {
                    File file = new File(path);
                    if(file.exists()) {
                        addNewTab();
                        fileManager.openFile(getActiveTab(), path);
                    }
                }
            }

            int lastActiveIndex = prefs.getInt("active-tab-index", 0);
            if (lastActiveIndex >= 0 && lastActiveIndex < tabbedPane.getTabCount()) {
                tabbedPane.setSelectedIndex(lastActiveIndex);
            }
        }

        if (tabbedPane.getTabCount() == 0) { //jeśli nic się nie otworzyło (np. pliki usunięte)
            addNewTab();
        }
    }

    public static void saveSession() {
        if (!prefs.getBoolean("restoreSession", false)) { //jeśli użytkownik wyłączył przywracanie sesji
            prefs.remove("open-files-session");
            prefs.remove("active-tab-index");
            return;
        }

        StringBuilder sessionPaths = new StringBuilder();
        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
            Tab tab = (Tab) tabbedPane.getComponentAt(i);
            if (tab.getFile() != null) { //sprawdzenie, czy zakładka pracuje na zapisanym pliku
                sessionPaths.append(tab.getFile().getAbsolutePath()).append(";");
            }
        }

        prefs.put("open-files-session", sessionPaths.toString()); //znajdują się tu ścieżki do plików otwartych w zakładkach
        prefs.putInt("active-tab-index", tabbedPane.getSelectedIndex()); //przechowuje index aktywnej zakładki
    }

    public static void activateAutosave() {
        if (autoSaveTimer != null) autoSaveTimer.stop(); autoSaveTimer = null; //zabijamy stary timer, jeśli jakiś działał
        if (autoSaveFocusListener != null) window.removeWindowFocusListener(autoSaveFocusListener); autoSaveFocusListener = null; //czyścimy stary listener, jeśli był
        String trigger = prefs.get("autosave-trigger", "never");
        if (trigger.equals("never")) return; //użytkownik nie zgodził się na autosave
        if (trigger.equals("onFocusChange")) { //użytkownik wybrał autosave po utracie fokusu
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

    public static void toggleMarkdownPreview() {
        if(!isPreviewVisible) { //włączamy podgląd markdown po prawej stronie
            if (markdownPreview == null) {
                markdownPreview = new JEditorPane();
                markdownPreview.setEditable(false);
                markdownPreview.setContentType("text/html");
                markdownPreview.addHyperlinkListener(e -> openInBrowser(e.getURL(), e.getEventType()));
            }

            //przeniesienie tabbedPane do splitPane
            window.remove(tabbedPane);
            splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tabbedPane, new JScrollPane(markdownPreview));
            splitPane.setDividerLocation(window.getWidth() / 2); //na środku okna pionowo

            window.add(splitPane, BorderLayout.CENTER);
            isPreviewVisible = true;
            updateMarkdownPreview();
        } else { //wyłączamy podgląd
            window.remove(splitPane);
            window.add(tabbedPane, BorderLayout.CENTER);
            isPreviewVisible = false;
        }
        window.revalidate();
        window.repaint();
    }

    public static void updateMarkdownPreview() {
        if (!isPreviewVisible) return;

        Tab activeTab = getActiveTab();
        String fileName = activeTab.getTitle().toLowerCase();

        if (fileName.endsWith(".md")) {
            String rawText = activeTab.getText();
            String htmlBody = parseMarkdownToHtml(rawText);
            String css = "code { padding: 2px 4px; border-radius: 3px; font-family: monospace; font-size: 9px; } " +
                    "pre { padding: 10px; border-radius: 5px; } " +
                    "blockquote { border-left: 4px solid #ccc; margin-left: 0; padding-left: 10px; }";
            String fullHtml = "<html><head><style>" + css + "</style></head><body>" + htmlBody + "</body></html>";
            markdownPreview.setText(fullHtml);
        }
    }

    private static String parseMarkdownToHtml(String rawText) {
        Parser parser = Parser.builder().build();
        Node document = parser.parse(rawText);
        HtmlRenderer renderer = HtmlRenderer.builder().build();

        return renderer.render(document);
    }

    public static void openInBrowser(URL url, HyperlinkEvent.EventType eventType) {
        if(eventType == HyperlinkEvent.EventType.ACTIVATED && url != null) {
            try {
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                    Desktop.getDesktop().browse(url.toURI());
                } else {
                    String os = System.getProperty("os.name").toLowerCase();
                    String urlString = url.toString();
                    if (os.contains("mac")) {
                        Runtime.getRuntime().exec(new String[]{"open", urlString});
                    } else if (os.contains("nix") || os.contains("nux")) {
                        Runtime.getRuntime().exec(new String[]{"xdg-open", urlString});
                    } else if (os.contains("win")) {
                        Runtime.getRuntime().exec(new String[]{"rundll32", "url.dll,FileProtocolHandler", urlString});
                    }
                }
            } catch (Exception ex) {
                //noinspection CallToPrintStackTrace
                ex.printStackTrace();
            }
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
            Tab activeTab = getActiveTab();
            tabbedPane.setTitleAt(index, activeTab.getTitle());
            window.setTitle("SwingNotes - " + activeTab.getTitle());

            // aktualizacja statusBaru przy zmianie zakładki
            int chars = activeTab.getCharsCount();
            int words = activeTab.getWordCount();
            int lines = activeTab.getLinesCount();
            statusBar.setText(I18n.get("statusBar.format", chars, words, lines));

            // podgląd markdown
            if(mdPreviewItem != null) {
                boolean isMarkdown = activeTab.getTitle().toLowerCase().endsWith(".md");
                mdPreviewItem.setEnabled(isMarkdown); // wyszarza opcje w menu, jeśli to nie jest plik .md
                if(!isMarkdown && isPreviewVisible) { // jeśli aktualna zakładka nie jest plikiem .md a preview markdownu pozostał (np. po przełączeniu na zakładkę z plikiem .txt)
                    toggleMarkdownPreview();
                    mdPreviewItem.setSelected(false);
                }
                if(isMarkdown && isPreviewVisible) {
                    updateMarkdownPreview();
                }
            }
        } else {
            window.setTitle("SwingNotes");
            if(mdPreviewItem != null) { //zabezpieczenie po zamknięciu wszystkich kart
                mdPreviewItem.setEnabled(false);
                if (isPreviewVisible) {
                    toggleMarkdownPreview();
                    mdPreviewItem.setSelected(false);
                }
            }
        }
    }

    public static void closeApp() {
        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
            Tab tab = (Tab) tabbedPane.getComponentAt(i);
            tabbedPane.setSelectedIndex(i); //pokazuje zakładkę, aby użytkownik wiedział, o który plik jest pytany
            if (!fileManager.canDiscardChanges(tab, "dialog.saveChangesBeforeClosing.msg")) return;
        }
        saveSession();
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
