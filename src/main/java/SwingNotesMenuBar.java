import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.jthemedetecor.OsThemeDetector;
import org.drjekyll.fontchooser.FontDialog;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.print.PrinterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.prefs.Preferences;

public class SwingNotesMenuBar extends JMenuBar {

    public SwingNotesMenuBar(RSyntaxTextArea textArea, FileManager fileManager, Preferences prefs, JFrame window) {
        // Nazwy zmiennych menu zawierają w sobie suffix: -Menu (np. fileMenu) a podmenu zawierają suffix: -SubMenu
        // Nazwy zmiennych pozycji menu zawierają w sobie suffix: -Item (np. copyItem)

        // -=-=- Menus -=-=-
        JMenu fileMenu = new JMenu("Plik");
        fileMenu.setMnemonic(KeyEvent.VK_P);
        JMenu editMenu = new JMenu("Edycja");
        editMenu.setMnemonic(KeyEvent.VK_E);
        JMenu viewMenu = new JMenu("Widok");
        viewMenu.setMnemonic(KeyEvent.VK_O);
        JMenu helpMenu = new JMenu("Pomoc");
        helpMenu.setMnemonic(KeyEvent.VK_P);


        // -=-=- File menu items -=-=-
        JMenuItem newItem = new JMenuItem("Nowy plik", KeyEvent.VK_N);
        newItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK));
        newItem.addActionListener(e -> fileManager.newFile(textArea));

        JMenu recentlyOpenedSubMenu = new JMenu("Ostatnio otwierane");
        recentlyOpenedSubMenu.setMnemonic(KeyEvent.VK_R);
        makeRecentlyOpenedFilesMenuContent(textArea, fileManager, prefs, recentlyOpenedSubMenu, window);

        JMenuItem openItem = new JMenuItem("Otwórz plik", KeyEvent.VK_O);
        openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
        openItem.addActionListener(e -> {
            fileManager.openFile(textArea, null, prefs);
            makeRecentlyOpenedFilesMenuContent(textArea, fileManager, prefs, recentlyOpenedSubMenu, window);
        });

        JMenuItem saveItem = new JMenuItem("Zapisz", KeyEvent.VK_Z);
        saveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
        saveItem.addActionListener(e -> fileManager.saveFile(textArea));

        JMenuItem saveAsItem = new JMenuItem("Zapisz jako...");
        saveAsItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
                InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
        saveAsItem.addActionListener(e -> fileManager.saveAs(textArea));

        JMenuItem printItem = new JMenuItem("Drukuj");
        printItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_DOWN_MASK));
        printItem.addActionListener(e -> {
            try {
                textArea.print();
            } catch (PrinterException ex) {
                throw new RuntimeException(ex);
            }
        });

        JMenuItem quitItem = new JMenuItem("Zakończ", KeyEvent.VK_K);
        quitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK));
        quitItem.addActionListener(e -> Main.closeApp(fileManager, textArea));

        fileMenu.add(newItem);
        fileMenu.add(openItem);
        fileMenu.add(recentlyOpenedSubMenu);
        fileMenu.addSeparator();
        fileMenu.add(saveItem);
        fileMenu.add(saveAsItem);
        fileMenu.add(printItem);
        fileMenu.addSeparator();
        fileMenu.add(quitItem);


        // -=-=- Edit menu items -=-=-
        JMenuItem copyItem = new JMenuItem("Kopiuj");
        copyItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK));
        copyItem.addActionListener(e -> textArea.copy());

        JMenuItem cutItem = new JMenuItem("Wytnij");
        cutItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_DOWN_MASK));
        cutItem.addActionListener(e -> textArea.cut());

        JMenuItem pasteItem = new JMenuItem("Wklej");
        pasteItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK));
        pasteItem.addActionListener(e -> textArea.paste());

        JMenuItem undoItem = new JMenuItem("Cofnij");
        undoItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK));
        undoItem.addActionListener(e -> textArea.undoLastAction());

        JMenuItem redoItem = new JMenuItem("Ponów");
        redoItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_DOWN_MASK));
        redoItem.addActionListener(e -> textArea.redoLastAction());

        JMenuItem selectEverythingItem = new JMenuItem("Zaznacz wszystko");
        selectEverythingItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_DOWN_MASK));
        selectEverythingItem.addActionListener(e -> textArea.selectAll());

        JMenuItem deleteItem = new JMenuItem("Usuń");
        deleteItem.setAccelerator(KeyStroke.getKeyStroke((char) KeyEvent.VK_DELETE));
        deleteItem.addActionListener(e -> textArea.replaceSelection(""));

        JMenuItem findAndReplaceItem = new JMenuItem("Znajdź i zamień");
        findAndReplaceItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK));
        findAndReplaceItem.addActionListener(e -> new FindReplaceDialog(window, textArea, fileManager).setVisible(true));

        editMenu.add(copyItem);
        editMenu.add(cutItem);
        editMenu.add(pasteItem);
        editMenu.addSeparator();
        editMenu.add(undoItem);
        editMenu.add(redoItem);
        editMenu.addSeparator();
        editMenu.add(selectEverythingItem);
        editMenu.add(deleteItem);
        editMenu.addSeparator();
        editMenu.add(findAndReplaceItem);


        // -=-=- View menu items -=-=-
        JMenu themeSubMenu = new JMenu("Motyw");

        JMenuItem blackThemeItem = new JMenuItem("Czarny");
        blackThemeItem.addActionListener(e -> {
            prefs.put("theme", "dark");
            FlatDarkLaf.setup();
            Main.setTextAreaTheme("dark.xml", textArea);
            SwingUtilities.updateComponentTreeUI(window);
        });

        JMenuItem whiteThemeItem = new JMenuItem("Biały");
        whiteThemeItem.addActionListener(e -> {
            prefs.put("theme", "light");
            FlatLightLaf.setup();
            Main.setTextAreaTheme("default.xml", textArea);
            SwingUtilities.updateComponentTreeUI(window);
        });

        JMenuItem systemThemeItem = new JMenuItem("Systemowy");
        systemThemeItem.addActionListener(e -> {
            prefs.put("theme", "system");
            OsThemeDetector detector = OsThemeDetector.getDetector();
            if (detector.isDark()) FlatDarkLaf.setup();
            else FlatLightLaf.setup();
            SwingUtilities.updateComponentTreeUI(window);
        });

        JMenu syntaxThemeSubMenu = new JMenu("Motyw składni");

        String[] syntaxThemes = {"dark", "default", "druid", "eclipse", "idea", "monokai", "vs"};

        for (String syntaxThemeName : syntaxThemes) {
            JMenuItem syntaxThemeItem = new JMenuItem(syntaxThemeName);
            syntaxThemeItem.addActionListener(e -> {
                Main.setTextAreaTheme(syntaxThemeName + ".xml", textArea);
                prefs.put("syntaxTheme", syntaxThemeName + ".xml");
            });
            syntaxThemeSubMenu.add(syntaxThemeItem);
        }

        JCheckBoxMenuItem lineWrapItem = new JCheckBoxMenuItem("Zawijanie linii");
        lineWrapItem.setSelected(prefs.getBoolean("lineWrap", true));
        lineWrapItem.addActionListener(e -> {
            boolean wrap = lineWrapItem.isSelected();
            prefs.putBoolean("lineWrap", wrap);
            textArea.setLineWrap(wrap);
        });

        JMenuItem fontItem = new JMenuItem("Czcionka i rozmiar");
        fontItem.addActionListener(e -> {
            FontDialog dialog = new FontDialog(window, "Czcionka i rozmiar", true);
            dialog.setSelectedFont(textArea.getFont());
            dialog.setLocationRelativeTo(window);
            dialog.setVisible(true);
            if (!dialog.isCancelSelected()) {
                Font font = dialog.getSelectedFont();
                textArea.setFont(font);
                prefs.put("fontName", font.getName());
                prefs.putInt("fontSize", font.getSize());
            }
        });


        viewMenu.add(themeSubMenu);
        themeSubMenu.add(blackThemeItem);
        themeSubMenu.add(whiteThemeItem);
        themeSubMenu.add(systemThemeItem);
        viewMenu.add(syntaxThemeSubMenu);
        viewMenu.add(lineWrapItem);
        viewMenu.add(fontItem);


        // -=-=- Help menu items -=-=-
        JMenuItem aboutItem = new JMenuItem("O programie", KeyEvent.VK_N);
        aboutItem.addActionListener(e -> JOptionPane.showMessageDialog(window, "SwingNotes 1.0\n" + "Prosty notatnik napisany w Javie z użyciem biblioteki Swing.", "O programie", JOptionPane.INFORMATION_MESSAGE));

        helpMenu.add(aboutItem);

        add(fileMenu);
        add(editMenu);
        add(viewMenu);
        add(helpMenu);
    }

    private static void makeRecentlyOpenedFilesMenuContent(RSyntaxTextArea textArea, FileManager fileManager, Preferences prefs, JMenu pmnOstatnioOtwierane, JFrame window) {
        pmnOstatnioOtwierane.removeAll();
        String recents = prefs.get("recentFiles", "none");

        if(recents.equals("none")) {
            JMenuItem pzNone = new JMenuItem("Brak");
            pmnOstatnioOtwierane.add(pzNone);
        } else {
            String[] files = recents.split(",");
            for (String path : files) {
                JMenuItem pzPlik = new JMenuItem(path);
                pzPlik.addActionListener(e -> fileManager.openFile(textArea, path, prefs));
                pmnOstatnioOtwierane.add(pzPlik);
            }
        }

        pmnOstatnioOtwierane.addSeparator();

        JMenuItem pzHowManyRecentFilesDialog = new JMenuItem("Zmień ilość przechowywanych plików");
        pzHowManyRecentFilesDialog.addActionListener(e -> {
            String amount = JOptionPane.showInputDialog(window,
                    "Ile ostatnio otwieranych plików pamiętać?",
                    prefs.get("recentFilesMenuLength", "5"));
            if (amount != null) {
                try {
                    int newMax = Integer.parseInt(amount);
                    if (newMax < 1) throw new NumberFormatException();

                    prefs.put("recentFilesMenuLength", amount);

                    String recentFiles = prefs.get("recentFiles", "none");
                    if (!recentFiles.equals("none")) {
                        ArrayList<String> recentFilesList = new ArrayList<>(Arrays.asList(recentFiles.split(",")));
                        if (recentFilesList.size() > newMax) {
                            recentFilesList = new ArrayList<>(recentFilesList.subList(0, newMax));
                            prefs.put("recentFiles", String.join(",", recentFilesList));
                        }
                    }
                    makeRecentlyOpenedFilesMenuContent(textArea, fileManager, prefs, pmnOstatnioOtwierane, window);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(window, "Podaj liczbę więszą od 0!", "Błąd", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        JMenuItem pzClear = new JMenuItem("Wyczyść");
        pzClear.addActionListener(e -> {
            prefs.put("recentFiles", "none");
            makeRecentlyOpenedFilesMenuContent(textArea, fileManager, prefs, pmnOstatnioOtwierane, window);
        });

        pmnOstatnioOtwierane.add(pzHowManyRecentFilesDialog);
        pmnOstatnioOtwierane.add(pzClear);
    }
}
