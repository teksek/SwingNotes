import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.jthemedetecor.OsThemeDetector;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.undo.UndoManager;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.prefs.Preferences;

public class Main {
    private static JFrame window;
    private static Preferences prefs;

    private static void makeWindow() {
        window = new JFrame("SwingNotes");
        window.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        // obszar tekstu
        JTextArea textArea = new JTextArea();

        String fontName = prefs.get("fontName", "Monospaced");
        int fontSize = prefs.getInt("fontSize", 14);
        textArea.setFont(new Font(fontName, Font.PLAIN, fontSize));

        boolean lineWrap = prefs.getBoolean("lineWrap", true);
        textArea.setLineWrap(lineWrap);
        textArea.setWrapStyleWord(true);

        // ustawienie możliwości scrollowania obszaru tekstu
        JScrollPane scrollPane = new JScrollPane(textArea);

        // pasek statusu dokumentu na dole
        JLabel statusBar = new JLabel("Znaki: 0 | Słowa: 0 | Linie: 1");
        statusBar.setBorder(BorderFactory.createEmptyBorder(3, 6, 3, 6));

        UndoManager undoManager = new UndoManager();
        textArea.getDocument().addUndoableEditListener(undoManager);

        FileManager fileManager = new FileManager(window);
        window.setJMenuBar(new SwingNotesMenuBar(textArea, fileManager, prefs, window, undoManager));

        textArea.setComponentPopupMenu(new SwingNotesContextMenu(textArea, undoManager));

        // aktualizacja paska statusu przy każdej zmianie tekstu
        textArea.getDocument().addDocumentListener(new DocumentListener() {
            private void updateStatusBarOnChange() {
                int znaki = textArea.getText().length();
                int linie = textArea.getLineCount();
                int slowa = textArea.getText().split("\\s+").length;
                statusBar.setText("Znaki: " + znaki + " | Słowa: " + slowa + " | Linie: " + linie);
                fileManager.setFileChanged(true);
            }
            public void insertUpdate(DocumentEvent e) { updateStatusBarOnChange(); }
            public void removeUpdate(DocumentEvent e) { updateStatusBarOnChange(); }
            public void changedUpdate(DocumentEvent e) { updateStatusBarOnChange(); }
        });

        window.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if(fileManager.isFileChanged()) {
                    int choice = JOptionPane.showOptionDialog(window,
                            "Czy zapisać zmiany przed zamknięciem programu?",
                            "Zamykanie", JOptionPane.YES_NO_CANCEL_OPTION,
                            JOptionPane.QUESTION_MESSAGE, null,
                            new String[]{"Zapisz", "Nie zapisuj", "Anuluj"}, 0);
                    if (choice == 0) {
                        fileManager.saveAs(textArea);
                    } else if (choice == 2) {
                        return;
                    }
                }
                System.exit(0);
            }
        });

        window.add(scrollPane, BorderLayout.CENTER);
        window.add(statusBar, BorderLayout.SOUTH);

        window.setSize(new Dimension(800, 600));
        window.setLocationRelativeTo(null);
        window.setVisible(true);
    }

    public static void main(String[] args) {
        OsThemeDetector detector = OsThemeDetector.getDetector();
        prefs = Preferences.userNodeForPackage(Main.class);
        String theme = prefs.get("theme", "system");

        switch(theme) {
            case "dark" -> FlatDarkLaf.setup();
            case "light" -> FlatLightLaf.setup();
            default -> {
                if (detector.isDark()) FlatDarkLaf.setup();
                else FlatLightLaf.setup();

                detector.registerListener(isDark -> SwingUtilities.invokeLater(() -> {
                    if (isDark) FlatDarkLaf.setup();
                    else FlatLightLaf.setup();
                    if (window != null)
                        SwingUtilities.updateComponentTreeUI(window);
                }));
            }
        }

        SwingUtilities.invokeLater(Main::makeWindow);
    }
}