import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.jthemedetecor.OsThemeDetector;
import org.drjekyll.fontchooser.FontDialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.print.PrinterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.prefs.Preferences;

public class SwingNotesMenuBar extends JMenuBar {
    private final FileManager fileManager;
    private final Preferences prefs;
    private final JFrame window;

    // zawsze zwraca aktualnie aktywną zakładkę
    private Tab tab() {
        return Main.getActiveTab();
    }

    public SwingNotesMenuBar(FileManager fileManager, Preferences prefs, JFrame window) {
        this.fileManager = fileManager;
        this.prefs = prefs;
        this.window = window;

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

        // -=-=- File menu items -=-=-
        JMenuItem newItem = new JMenuItem("Nowy plik", KeyEvent.VK_N);
        newItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK));
        newItem.addActionListener(e -> fileManager.newFile(tab()));

        JMenuItem newTabItem = new JMenuItem("Nowa zakładka");
        newTabItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.CTRL_DOWN_MASK));
        newTabItem.addActionListener(e -> Main.addNewTab());

        JMenuItem closeTabItem = new JMenuItem("Zamknij zakładkę");
        closeTabItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.CTRL_DOWN_MASK));
        closeTabItem.addActionListener(e -> {
            if (Main.tabbedPane.getTabCount() <= 1) return;
            Tab currentTab = tab();
            if (currentTab.isFileChanged()) {
                int choice = JOptionPane.showOptionDialog(window,
                        "Czy zapisać zmiany w \"" + currentTab.getTitle() + "\" przed zamknięciem?",
                        "Zamknij zakładkę", JOptionPane.YES_NO_CANCEL_OPTION,
                        JOptionPane.QUESTION_MESSAGE, null,
                        new String[]{"Zapisz", "Nie zapisuj", "Anuluj"}, 0);
                if (choice == 0) fileManager.saveFile(currentTab);
                else if (choice == 2 || choice == JOptionPane.CLOSED_OPTION) return;
            }
            Main.tabbedPane.removeTabAt(Main.tabbedPane.getSelectedIndex());
        });

        JMenu recentlyOpenedSubMenu = new JMenu("Ostatnio otwierane");
        makeRecentlyOpenedFilesMenuContent(recentlyOpenedSubMenu);

        JMenuItem openItem = new JMenuItem("Otwórz plik", KeyEvent.VK_O);
        openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
        openItem.addActionListener(e -> {
            fileManager.openFile(tab(), null, prefs);
            makeRecentlyOpenedFilesMenuContent(recentlyOpenedSubMenu);
        });

        JMenuItem saveItem = new JMenuItem("Zapisz", KeyEvent.VK_Z);
        saveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
        saveItem.addActionListener(e -> fileManager.saveFile(tab()));

        JMenuItem saveAsItem = new JMenuItem("Zapisz jako...");
        saveAsItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
                InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
        saveAsItem.addActionListener(e -> fileManager.saveAs(tab()));

        JMenuItem printItem = new JMenuItem("Drukuj");
        printItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_DOWN_MASK));
        printItem.addActionListener(e -> {
            try {
                tab().getTextArea().print();
            } catch (PrinterException ex) {
                throw new RuntimeException(ex);
            }
        });

        JMenuItem quitItem = new JMenuItem("Zakończ", KeyEvent.VK_K);
        quitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK));
        quitItem.addActionListener(e -> Main.closeApp());

        fileMenu.add(newItem);
        fileMenu.add(newTabItem);
        fileMenu.add(closeTabItem);
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
        copyItem.addActionListener(e -> tab().getTextArea().copy());

        JMenuItem cutItem = new JMenuItem("Wytnij");
        cutItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_DOWN_MASK));
        cutItem.addActionListener(e -> tab().getTextArea().cut());

        JMenuItem pasteItem = new JMenuItem("Wklej");
        pasteItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK));
        pasteItem.addActionListener(e -> tab().getTextArea().paste());

        JMenuItem undoItem = new JMenuItem("Cofnij");
        undoItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK));
        undoItem.addActionListener(e -> tab().getTextArea().undoLastAction());

        JMenuItem redoItem = new JMenuItem("Ponów");
        redoItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_DOWN_MASK));
        redoItem.addActionListener(e -> tab().getTextArea().redoLastAction());

        JMenuItem selectEverythingItem = new JMenuItem("Zaznacz wszystko");
        selectEverythingItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_DOWN_MASK));
        selectEverythingItem.addActionListener(e -> tab().getTextArea().selectAll());

        JMenuItem deleteItem = new JMenuItem("Usuń");
        deleteItem.addActionListener(e -> tab().getTextArea().replaceSelection(""));

        JMenuItem findAndReplaceItem = new JMenuItem("Znajdź i zamień");
        findAndReplaceItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK));
        findAndReplaceItem.addActionListener(e -> new FindReplaceDialog(window, tab().getTextArea(), fileManager, tab()).setVisible(true));

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
            applyEditorThemeToAllTabs();
            SwingUtilities.updateComponentTreeUI(window);
        });

        JMenuItem whiteThemeItem = new JMenuItem("Biały");
        whiteThemeItem.addActionListener(e -> {
            prefs.put("theme", "light");
            FlatLightLaf.setup();
            applyEditorThemeToAllTabs();
            SwingUtilities.updateComponentTreeUI(window);
        });

        JMenuItem systemThemeItem = new JMenuItem("Systemowy");
        systemThemeItem.addActionListener(e -> {
            prefs.put("theme", "system");
            OsThemeDetector detector = OsThemeDetector.getDetector();
            if (detector.isDark()) FlatDarkLaf.setup();
            else FlatLightLaf.setup();
            applyEditorThemeToAllTabs();
            SwingUtilities.updateComponentTreeUI(window);
        });

        JMenu syntaxThemeSubMenu = new JMenu("Motyw składni");
        String[] syntaxThemes = {"dark", "default", "druid", "eclipse", "idea", "monokai", "vs"};
        for (String syntaxThemeName : syntaxThemes) {
            JMenuItem syntaxThemeItem = new JMenuItem(syntaxThemeName);
            syntaxThemeItem.addActionListener(e -> {
                prefs.put("syntaxTheme", syntaxThemeName + ".xml");
                applyEditorThemeToAllTabs();
            });
            syntaxThemeSubMenu.add(syntaxThemeItem);
        }

        JCheckBoxMenuItem lineWrapItem = new JCheckBoxMenuItem("Zawijanie linii");
        lineWrapItem.setSelected(prefs.getBoolean("lineWrap", true));
        lineWrapItem.addActionListener(e -> {
            boolean wrap = lineWrapItem.isSelected();
            prefs.putBoolean("lineWrap", wrap);
            tab().getTextArea().setLineWrap(wrap);
        });

        JMenuItem fontItem = new JMenuItem("Czcionka i rozmiar");
        fontItem.addActionListener(e -> {
            FontDialog dialog = new FontDialog(window, "Czcionka i rozmiar", true);
            dialog.setSelectedFont(tab().getTextArea().getFont());
            dialog.setLocationRelativeTo(window);
            dialog.setVisible(true);
            if (!dialog.isCancelSelected()) {
                Font font = dialog.getSelectedFont();
                tab().getTextArea().setFont(font);
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
        JMenuItem aboutItem = new JMenuItem("O programie");
        aboutItem.addActionListener(e -> JOptionPane.showMessageDialog(window,
                "SwingNotes\nProsty notatnik napisany w Javie z użyciem biblioteki Swing.",
                "O programie", JOptionPane.INFORMATION_MESSAGE));
        helpMenu.add(aboutItem);

        add(fileMenu);
        add(editMenu);
        add(viewMenu);
        add(helpMenu);
    }

    private void applyEditorThemeToAllTabs() {
        for (int i = 0; i < Main.tabbedPane.getTabCount(); i++) {
            Tab tab = (Tab) Main.tabbedPane.getComponentAt(i);
            Main.applyTheme(Main.isDarkFromPrefs(), tab.getTextArea());
        }
    }

    private void makeRecentlyOpenedFilesMenuContent(JMenu recentlyOpenedSubMenu) {
        recentlyOpenedSubMenu.removeAll();
        String recents = prefs.get("recentFiles", "none");

        if (recents.equals("none")) {
            recentlyOpenedSubMenu.add(new JMenuItem("Brak"));
        } else {
            for (String path : recents.split(",")) {
                JMenuItem fileItem = new JMenuItem(path);
                fileItem.addActionListener(e -> fileManager.openFile(tab(), path, prefs));
                recentlyOpenedSubMenu.add(fileItem);
            }
        }

        recentlyOpenedSubMenu.addSeparator();

        JMenuItem changeAmountItem = new JMenuItem("Zmień ilość przechowywanych plików");
        changeAmountItem.addActionListener(e -> {
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
                    makeRecentlyOpenedFilesMenuContent(recentlyOpenedSubMenu);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(window, "Podaj liczbę większą od 0!", "Błąd", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        JMenuItem clearItem = new JMenuItem("Wyczyść");
        clearItem.addActionListener(e -> {
            prefs.put("recentFiles", "none");
            makeRecentlyOpenedFilesMenuContent(recentlyOpenedSubMenu);
        });

        recentlyOpenedSubMenu.add(changeAmountItem);
        recentlyOpenedSubMenu.add(clearItem);
    }
}
