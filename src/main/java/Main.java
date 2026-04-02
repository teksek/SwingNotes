import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.jthemedetecor.OsThemeDetector;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.Theme;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Objects;
import java.util.prefs.Preferences;

public class Main {
    private static JFrame window;
    private static Preferences prefs;
    public static JTabbedPane tabbedPane;
    private static JLabel statusBar;
    private static FileManager fileManager;

    private static void makeWindow() {
        window = new JFrame("SwingNotes");
        window.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        window.setIconImage(new ImageIcon(Objects.requireNonNull(Main.class.getResource("/icon.png"))).getImage()); //ustawienie ikony programu

        // lokalizacja
        String savedLang = prefs.get("language", "system");
        if (savedLang.equals("system")) I18n.loadBundle(Locale.getDefault());
        else I18n.loadBundle(Locale.forLanguageTag(savedLang));

        fileManager = new FileManager(window);

	    // pasek statusu dokumentu na dole
        statusBar = new JLabel(I18n.get("statusBar.format", 0, 0, 1));
        statusBar.setBorder(BorderFactory.createEmptyBorder(3, 6, 3, 6));

        tabbedPane = new JTabbedPane();
        tabbedPane.addChangeListener(e -> updateActiveTabUI());

        // implementacja mechanizmu drag-and-drop
        TransferHandler dropHandler = new TransferHandler() { // implementacja mechanizmu drag-and-drop
            @Override
            public boolean canImport(TransferSupport support) {
                return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
            }

            @SuppressWarnings("unchecked")
            @Override
            public boolean importData(TransferSupport support) {
                try {
                    java.util.List<File> files = (java.util.List<File>) support.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
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

        addNewTab(); // pierwsza zakładka

        window.setJMenuBar(new SwingNotesMenuBar(fileManager, prefs, window));     // tworzenie okna z elementów

        window.addWindowListener(new WindowAdapter() { // dodanie obsługi wyłączenia programu
            @Override
            public void windowClosing(WindowEvent e) {
                closeApp();
            }
        });

	    // tworzenie okna z elementów
        window.add(tabbedPane, BorderLayout.CENTER);
        window.add(statusBar, BorderLayout.SOUTH);

        setThemeOnLaunch(getActiveTab().getTextArea()); // ustawienie motywów (UI i syntax) po utworzeniu wszystkich komponentów

        window.setSize(new Dimension(800, 600));
        window.setLocationRelativeTo(null);
        window.setVisible(true);
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
            if (tab.isFileChanged()) {
                tabbedPane.setSelectedIndex(i);
                int choice = JOptionPane.showOptionDialog(window,
                        I18n.get("dialog.saveChangesBeforeClosing.msg", tab.getTitle()),
                        I18n.get("dialog.saveChangesBeforeClosing.app.title"),
                        JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                        new String[]{I18n.get("msg.option.save"), I18n.get("msg.option.dontSave"), I18n.get("msg.option.cancel")}, 0);
                if (choice == 0) fileManager.saveFile(tab);
                else if (choice == 2 || choice == JOptionPane.CLOSED_OPTION) return;
            }
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
