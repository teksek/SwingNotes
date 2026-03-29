import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.jthemedetecor.OsThemeDetector;
import org.fife.rsta.ac.LanguageSupportFactory;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.HyperlinkEvent;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.prefs.Preferences;

public class Main {
    private static JFrame window;
    private static Preferences prefs;

    private static void makeWindow() {
        window = new JFrame("SwingNotes");
        window.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        // obszar tekstu
        RSyntaxTextArea textArea = new RSyntaxTextArea();

        LanguageSupportFactory.get().register(textArea);

        String fontName = prefs.get("fontName", "Monospaced");
        int fontSize = prefs.getInt("fontSize", 14);
        textArea.setFont(new Font(fontName, Font.PLAIN, fontSize));

        boolean lineWrap = prefs.getBoolean("lineWrap", true);
        textArea.setLineWrap(lineWrap);
        textArea.setWrapStyleWord(true);

        // ustawienie możliwości scrollowania obszaru tekstu
        RTextScrollPane mainPane = new RTextScrollPane(textArea);

        // pasek statusu dokumentu na dole
        JLabel statusBar = new JLabel("Znaki: 0 | Słowa: 0 | Linie: 1");
        statusBar.setBorder(BorderFactory.createEmptyBorder(3, 6, 3, 6));

        FileManager fileManager = new FileManager(window);
        window.setJMenuBar(new SwingNotesMenuBar(textArea, fileManager, prefs, window));

        textArea.setComponentPopupMenu(new SwingNotesContextMenu(textArea));

        // aktualizacja paska statusu przy każdej zmianie tekstu
        textArea.getDocument().addDocumentListener(new DocumentListener() {
            private void updateStatusBarOnChange() {
                int charsCount = textArea.getText().length();
                int linesCount = textArea.getLineCount();
                int wordCount = textArea.getText().split("\\s+").length;
                statusBar.setText("Znaki: " + charsCount + " | Słowa: " + wordCount + " | Linie: " + linesCount);
                fileManager.setFileChanged(charsCount != 0);
            }
            public void insertUpdate(DocumentEvent e) { updateStatusBarOnChange(); }
            public void removeUpdate(DocumentEvent e) { updateStatusBarOnChange(); }
            public void changedUpdate(DocumentEvent e) { updateStatusBarOnChange(); }
        });

        window.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                closeApp(fileManager, textArea);
            }
        });

        TransferHandler dropHandler = new TransferHandler() {
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
                        fileManager.openFile(textArea, files.getFirst().getAbsolutePath(), prefs);
                    }
                    return true;
                } catch (Exception e) {
                    return false;
                }
            }
        };

        textArea.setTransferHandler(dropHandler);
        window.setTransferHandler(dropHandler);

        textArea.addHyperlinkListener(e -> {
            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                if(Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                    try {
                        Desktop.getDesktop().browse(e.getURL().toURI());
                    } catch (IOException | URISyntaxException ex) {
                        throw new RuntimeException(ex);
                    }
                } else {
                    String os = System.getProperty("os.name").toLowerCase();
                    if(os.contains("mac")) {
                        try {
                            Runtime.getRuntime().exec(new String[]{"open", e.getURL().toString()});
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                    } else { //linux
                        try {
                            Runtime.getRuntime().exec(new String[]{"xdg-open", e.getURL().toString()});
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                }
            }
        });

        setThemeOnLaunch(textArea);

        window.add(mainPane, BorderLayout.CENTER);
        window.add(statusBar, BorderLayout.SOUTH);

        window.setSize(new Dimension(800, 600));
        window.setLocationRelativeTo(null);
        window.setVisible(true);
    }

    public static void closeApp(FileManager fileManager, RSyntaxTextArea textArea) {
        if(fileManager.isFileChanged()) {
            int choice = JOptionPane.showOptionDialog(window,
                    "Czy zapisać zmiany przed zamknięciem programu?",
                    "Zamykanie", JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE, null,
                    new String[]{"Zapisz", "Nie zapisuj", "Anuluj"}, 0);
            if (choice == 0) fileManager.saveAs(textArea);
            else if (choice == 2 || choice == JOptionPane.CLOSED_OPTION) return;
        }
        System.exit(0);
    }

    public static void setTextAreaTheme(String themeFileName, RSyntaxTextArea textArea) {
        try {
            Theme theme = Theme.load(Main.class.getResourceAsStream("/org/fife/ui/rsyntaxtextarea/themes/" + themeFileName));
            theme.apply(textArea);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void applyTheme(boolean isDark, RSyntaxTextArea textArea) {
        if (isDark) {
            FlatDarkLaf.setup();
            if (textArea != null) {
                String syntaxTheme = prefs.get("syntaxTheme", "dark.xml");
                setTextAreaTheme(syntaxTheme, textArea);
            }
        } else {
            FlatLightLaf.setup();
            if (textArea != null){
                String syntaxTheme = prefs.get("syntaxTheme", "default.xml");
                setTextAreaTheme(syntaxTheme, textArea);
            }
        }
    }

    private static void setThemeOnLaunch(RSyntaxTextArea textArea) {
        OsThemeDetector detector = OsThemeDetector.getDetector(); //sprawdza, czy system ma ciemny motyw i automatycznie dobiera odpowiedni.
        String theme = prefs.get("theme", "system");

        switch(theme) {
            case "dark" -> applyTheme(true, textArea);
            case "light" -> applyTheme(false, textArea);
            default -> {
                applyTheme(detector.isDark(), textArea);

                detector.registerListener(isDark -> SwingUtilities.invokeLater(() -> {
                    applyTheme(detector.isDark(), textArea);
                    if (window != null) SwingUtilities.updateComponentTreeUI(window); //odświeża wszystkie komponenty, żeby motyw zadziałał bez restartu
                }));
            }
        }
    }

    private static boolean isDarkFromPrefs() {
        String theme = prefs.get("theme", "system");
        if (theme.equals("dark")) return true;
        if (theme.equals("light")) return false;
        return OsThemeDetector.getDetector().isDark();
    }

    public static void main(String[] args) {
        prefs = Preferences.userNodeForPackage(Main.class);
        applyTheme(isDarkFromPrefs(), null);

        SwingUtilities.invokeLater(Main::makeWindow);
    }
}