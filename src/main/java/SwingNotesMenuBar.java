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
        JMenu fileMenu = new JMenu(I18n.get("menu.file"));
        fileMenu.setMnemonic(KeyEvent.VK_P);
        JMenu editMenu = new JMenu(I18n.get("menu.edit"));
        editMenu.setMnemonic(KeyEvent.VK_E);
        JMenu viewMenu = new JMenu(I18n.get("menu.view"));
        viewMenu.setMnemonic(KeyEvent.VK_O);
        JMenu helpMenu = new JMenu(I18n.get("menu.help"));

        // -=-=- File menu items -=-=-
        JMenuItem newItem = new JMenuItem(I18n.get("file.newFile"), KeyEvent.VK_N);
        newItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK));
        newItem.addActionListener(e -> fileManager.newFile(tab()));

        JMenuItem newTabItem = new JMenuItem(I18n.get("file.newTab"));
        newTabItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.CTRL_DOWN_MASK));
        newTabItem.addActionListener(e -> Main.addNewTab());

        JMenuItem closeTabItem = new JMenuItem(I18n.get("file.closeTab"));
        closeTabItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.CTRL_DOWN_MASK));
        closeTabItem.addActionListener(e -> {
            Tab currentTab = tab();
            if (currentTab.isFileChanged()) {
                int choice = JOptionPane.showOptionDialog(window,
                        I18n.get("dialog.saveChangesBeforeClosing.msg", currentTab.getTitle()),
                        I18n.get("dialog.saveChangesBeforeClosing.tab.title"), JOptionPane.YES_NO_CANCEL_OPTION,
                        JOptionPane.QUESTION_MESSAGE, null,
                        new String[]{I18n.get("msg.option.save"), I18n.get("msg.option.dontSave"), I18n.get("msg.option.cancel")}, 0);
                if (choice == 0) fileManager.saveFile(currentTab);
                else if (choice == 2 || choice == JOptionPane.CLOSED_OPTION) return;
            }
            if (Main.tabbedPane.getTabCount() <= 1) {
                Main.tabbedPane.removeTabAt(0);
                Main.addNewTab();
            } else {
                Main.tabbedPane.removeTabAt(Main.tabbedPane.getSelectedIndex());
            }
        });

        JMenu recentlyOpenedSubMenu = new JMenu(I18n.get("file.recentlyOpened"));
        makeRecentlyOpenedFilesMenuContent(recentlyOpenedSubMenu);

        JMenu fileExtensionSubMenu = new JMenu(I18n.get("file.fileExtension"));
        makeRecentlyOpenedFilesMenuContent(recentlyOpenedSubMenu);

        String[][] fileTypes = {
                {"Plain text", "text/plain"},
                {"ActionScript", "text/actionscript"},
                {"Assembler (x86)", "text/asm"},
                {"Assembler (6502)", "text/asm6502"},
                {"BBCode", "text/bbcode"},
                {"C", "text/c"},
                {"Clojure", "text/clojure"},
                {"C++", "text/cpp"},
                {"C#", "text/cs"},
                {"CSS", "text/css"},
                {"CSV", "text/csv"},
                {"D", "text/d"},
                {"Dockerfile", "text/dockerfile"},
                {"Dart", "text/dart"},
                {"Delphi/Pascal", "text/delphi"},
                {"DTD", "text/dtd"},
                {"Fortran", "text/fortran"},
                {"Go", "text/golang"},
                {"Groovy", "text/groovy"},
                {"Handlebars", "text/handlebars"},
                {"Hosts", "text/hosts"},
                {".htaccess", "text/htaccess"},
                {"HTML", "text/html"},
                {"INI", "text/ini"},
                {"Java", "text/java"},
                {"JavaScript", "text/javascript"},
                {"JSON", "text/json"},
                {"JSON (comments)", "text/jshintrc"},
                {"JSP", "text/jsp"},
                {"Kotlin", "text/kotlin"},
                {"LaTeX", "text/latex"},
                {"Less", "text/less"},
                {"Lisp", "text/lisp"},
                {"Lua", "text/lua"},
                {"Makefile", "text/makefile"},
                {"Markdown", "text/markdown"},
                {"MXML", "text/mxml"},
                {"NSIS", "text/nsis"},
                {"Perl", "text/perl"},
                {"PHP", "text/php"},
                {"PowerShell", "text/powershell"},
                {"Proto", "text/proto"},
                {"Properties", "text/properties"},
                {"Python", "text/python"},
                {"Ruby", "text/ruby"},
                {"Rust", "text/rust"},
                {"SAS", "text/sas"},
                {"Scala", "text/scala"},
                {"SQL", "text/sql"},
                {"Tcl", "text/tcl"},
                {"TypeScript", "text/typescript"},
                {"Unix Shell", "text/unix"},
                {"Visual Basic", "text/vb"},
                {"VHDL", "text/vhdl"},
                {"Batch", "text/bat"},
                {"XML", "text/xml"},
                {"YAML", "text/yaml"}
        };

        for (String[] fileType : fileTypes) {
            JMenuItem languageItem = new JMenuItem(fileType[0]);
            languageItem.addActionListener(e -> Main.getActiveTab().getTextArea().setSyntaxEditingStyle(fileType[1]));
            fileExtensionSubMenu.add(languageItem);
        }

        JMenuItem openItem = new JMenuItem(I18n.get("file.open"), KeyEvent.VK_O);
        openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
        openItem.addActionListener(e -> {
            fileManager.openFile(tab(), null, prefs);
            makeRecentlyOpenedFilesMenuContent(recentlyOpenedSubMenu);
        });

        JMenuItem saveItem = new JMenuItem(I18n.get("file.save"), KeyEvent.VK_Z);
        saveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
        saveItem.addActionListener(e -> fileManager.saveFile(tab()));

        JMenuItem saveAsItem = new JMenuItem(I18n.get("file.saveAs"));
        saveAsItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
                InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
        saveAsItem.addActionListener(e -> fileManager.saveAs(tab()));

        JMenuItem printItem = new JMenuItem(I18n.get("file.print"));
        printItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_DOWN_MASK));
        printItem.addActionListener(e -> {
            try {
                tab().getTextArea().print();
            } catch (PrinterException ex) {
                throw new RuntimeException(ex);
            }
        });

        JMenuItem quitItem = new JMenuItem(I18n.get("file.quit"), KeyEvent.VK_K);
        quitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK));
        quitItem.addActionListener(e -> Main.closeApp());

        fileMenu.add(newItem);
        fileMenu.add(newTabItem);
        fileMenu.add(closeTabItem);
        fileMenu.add(openItem);
        fileMenu.add(recentlyOpenedSubMenu);
        fileMenu.add(fileExtensionSubMenu);
        fileMenu.addSeparator();
        fileMenu.add(saveItem);
        fileMenu.add(saveAsItem);
        fileMenu.add(printItem);
        fileMenu.addSeparator();
        fileMenu.add(quitItem);


        // -=-=- Edit menu items -=-=-
        JMenuItem copyItem = new JMenuItem(I18n.get("edit.copy"));
        copyItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK));
        copyItem.addActionListener(e -> tab().getTextArea().copy());

        JMenuItem cutItem = new JMenuItem(I18n.get("edit.cut"));
        cutItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_DOWN_MASK));
        cutItem.addActionListener(e -> tab().getTextArea().cut());

        JMenuItem pasteItem = new JMenuItem(I18n.get("edit.paste"));
        pasteItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK));
        pasteItem.addActionListener(e -> tab().getTextArea().paste());

        JMenuItem undoItem = new JMenuItem(I18n.get("edit.undo"));
        undoItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK));
        undoItem.addActionListener(e -> tab().getTextArea().undoLastAction());

        JMenuItem redoItem = new JMenuItem(I18n.get("edit.redo"));
        redoItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_DOWN_MASK));
        redoItem.addActionListener(e -> tab().getTextArea().redoLastAction());

        JMenuItem selectEverythingItem = new JMenuItem(I18n.get("edit.selectEverything"));
        selectEverythingItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_DOWN_MASK));
        selectEverythingItem.addActionListener(e -> tab().getTextArea().selectAll());

        JMenuItem deleteItem = new JMenuItem(I18n.get("edit.delete"));
        deleteItem.addActionListener(e -> tab().getTextArea().replaceSelection(""));

        JMenuItem findAndReplaceItem = new JMenuItem(I18n.get("edit.findAndReplace"));
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
        JMenu languageSubMenu = new JMenu(I18n.get("view.language"));
        String[] langNames = {"Systemowy", "Polski", "English", "Deutsch", "Español", "Français"};
        String[] langCodes = {"system", "pl", "en", "de", "es", "fr"};

        for(int i = 0; i < langNames.length; i++) {
            String name = langNames[i];
            String code = langCodes[i];

            JMenuItem langItem = new JMenuItem(name);
            langItem.addActionListener(e -> {
                prefs.put("language", code);
                JOptionPane.showMessageDialog(window, I18n.get("dialog.languageChangeRestartRequired.msg"),
                        "SwingNotes", JOptionPane.INFORMATION_MESSAGE);
            });
            languageSubMenu.add(langItem);
        }

        JMenu themeSubMenu = new JMenu(I18n.get("view.theme"));

        JMenuItem blackThemeItem = new JMenuItem(I18n.get("theme.black"));
        blackThemeItem.addActionListener(e -> {
            prefs.put("theme", "dark");
            FlatDarkLaf.setup();
            applyEditorThemeToAllTabs();
            SwingUtilities.updateComponentTreeUI(window);
        });

        JMenuItem whiteThemeItem = new JMenuItem(I18n.get("theme.white"));
        whiteThemeItem.addActionListener(e -> {
            prefs.put("theme", "light");
            FlatLightLaf.setup();
            applyEditorThemeToAllTabs();
            SwingUtilities.updateComponentTreeUI(window);
        });

        JMenuItem systemThemeItem = new JMenuItem(I18n.get("theme.system"));
        systemThemeItem.addActionListener(e -> {
            prefs.put("theme", "system");
            OsThemeDetector detector = OsThemeDetector.getDetector();
            if (detector.isDark()) FlatDarkLaf.setup();
            else FlatLightLaf.setup();
            applyEditorThemeToAllTabs();
            SwingUtilities.updateComponentTreeUI(window);
        });

        JMenu syntaxThemeSubMenu = new JMenu(I18n.get("view.syntaxTheme"));
        String[] syntaxThemes = {"dark", "default", "druid", "eclipse", "idea", "monokai", "vs"};
        for (String syntaxThemeName : syntaxThemes) {
            JMenuItem syntaxThemeItem = new JMenuItem(syntaxThemeName);
            syntaxThemeItem.addActionListener(e -> {
                prefs.put("syntaxTheme", syntaxThemeName + ".xml");
                applyEditorThemeToAllTabs();
            });
            syntaxThemeSubMenu.add(syntaxThemeItem);
        }

        JCheckBoxMenuItem lineWrapItem = new JCheckBoxMenuItem(I18n.get("view.lineWrap"));
        lineWrapItem.setSelected(prefs.getBoolean("lineWrap", true));
        lineWrapItem.addActionListener(e -> {
            boolean wrap = lineWrapItem.isSelected();
            prefs.putBoolean("lineWrap", wrap);
            tab().getTextArea().setLineWrap(wrap);
        });

        JMenuItem fontItem = new JMenuItem(I18n.get("view.font"));
        fontItem.addActionListener(e -> {
            FontDialog dialog = new FontDialog(window, I18n.get("view.font"), true);
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

        viewMenu.add(languageSubMenu);
        viewMenu.add(themeSubMenu);
        themeSubMenu.add(blackThemeItem);
        themeSubMenu.add(whiteThemeItem);
        themeSubMenu.add(systemThemeItem);
        viewMenu.add(syntaxThemeSubMenu);
        viewMenu.add(lineWrapItem);
        viewMenu.add(fontItem);


        // -=-=- Help menu items -=-=-
        JMenuItem aboutItem = new JMenuItem(I18n.get("help.about"));
        aboutItem.addActionListener(e -> JOptionPane.showMessageDialog(window,
                I18n.get("dialog.about.msg"), I18n.get("dialog.about.title"),
                JOptionPane.INFORMATION_MESSAGE));
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
            recentlyOpenedSubMenu.add(new JMenuItem(I18n.get("msg.none")));
        } else {
            for (String path : recents.split(",")) {
                JMenuItem fileItem = new JMenuItem(path);
                fileItem.addActionListener(e -> fileManager.openFile(tab(), path, prefs));
                recentlyOpenedSubMenu.add(fileItem);
            }
        }

        recentlyOpenedSubMenu.addSeparator();

        JMenuItem changeAmountItem = new JMenuItem(I18n.get("recentlyOpened.changeAmount"));
        changeAmountItem.addActionListener(e -> {
            String amount = JOptionPane.showInputDialog(window,
                    I18n.get("dialog.changeAmount.msg"),
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
                    JOptionPane.showMessageDialog(window, I18n.get("dialog.zeroError.msg"), I18n.get("msg.error"), JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        JMenuItem clearItem = new JMenuItem(I18n.get("recentlyOpened.clear"));
        clearItem.addActionListener(e -> {
            prefs.put("recentFiles", "none");
            makeRecentlyOpenedFilesMenuContent(recentlyOpenedSubMenu);
        });

        recentlyOpenedSubMenu.add(changeAmountItem);
        recentlyOpenedSubMenu.add(clearItem);
    }
}
