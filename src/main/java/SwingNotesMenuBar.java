import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.jthemedetecor.OsThemeDetector;
import org.drjekyll.fontchooser.FontDialog;

import javax.swing.*;
import javax.swing.undo.UndoManager;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.print.PrinterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.prefs.Preferences;

public class SwingNotesMenuBar extends JMenuBar {

    public SwingNotesMenuBar(JTextArea textArea, FileManager fileManager, Preferences prefs, JFrame window, UndoManager undoManager) {
        // -=-=- Menu -=-=-
        JMenu mnPlik = new JMenu("Plik");
        mnPlik.setMnemonic(KeyEvent.VK_P);
        JMenu mnEdycja = new JMenu("Edycja");
        mnEdycja.setMnemonic(KeyEvent.VK_E);
        JMenu mnWidok = new JMenu("Widok");
        mnWidok.setMnemonic(KeyEvent.VK_O);
        JMenu mnPomoc = new JMenu("Pomoc");
        mnPomoc.setMnemonic(KeyEvent.VK_P);


        // -=-=- Pozycje menu Plik -=-=-
        JMenuItem pzNowy = new JMenuItem("Nowy plik", KeyEvent.VK_N);
        pzNowy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK));
        pzNowy.addActionListener(e -> fileManager.newFile(textArea));

        JMenu pmnOstatnioOtwierane = new JMenu("Ostatnio otwierane");
        pmnOstatnioOtwierane.setMnemonic(KeyEvent.VK_R);
        makeRecentlyOpenedFilesMenuContent(textArea, fileManager, prefs, pmnOstatnioOtwierane, window);

        JMenuItem pzOtworz = new JMenuItem("Otwórz plik", KeyEvent.VK_O);
        pzOtworz.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
        pzOtworz.addActionListener(e -> {
            fileManager.openFile(textArea, null, prefs);
            makeRecentlyOpenedFilesMenuContent(textArea, fileManager, prefs, pmnOstatnioOtwierane, window);
        });

        JMenuItem pzZapisz = new JMenuItem("Zapisz", KeyEvent.VK_Z);
        pzZapisz.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
        pzZapisz.addActionListener(e -> fileManager.saveFile(textArea));

        JMenuItem pzZapiszJako = new JMenuItem("Zapisz jako...");
        pzZapiszJako.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
                InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
        pzZapiszJako.addActionListener(e -> fileManager.saveAs(textArea));

        JMenuItem pzDrukuj = new JMenuItem("Drukuj");
        pzDrukuj.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_DOWN_MASK));
        pzDrukuj.addActionListener(e -> {
            try {
                textArea.print();
            } catch (PrinterException ex) {
                throw new RuntimeException(ex);
            }
        });

        JMenuItem pzZakoncz = new JMenuItem("Zakończ", KeyEvent.VK_K);
        pzZakoncz.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK));
        pzZakoncz.addActionListener(e -> {
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
        });

        mnPlik.add(pzNowy);
        mnPlik.add(pzOtworz);
        mnPlik.add(pmnOstatnioOtwierane);
        mnPlik.addSeparator();
        mnPlik.add(pzZapisz);
        mnPlik.add(pzZapiszJako);
        mnPlik.add(pzDrukuj);
        mnPlik.addSeparator();
        mnPlik.add(pzZakoncz);


        // -=-=- Pozycje menu Edycja -=-=-
        JMenuItem pzKopiuj = new JMenuItem("Kopiuj");
        pzKopiuj.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK));
        pzKopiuj.addActionListener(e -> textArea.copy());

        JMenuItem pzWytnij = new JMenuItem("Wytnij");
        pzWytnij.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_DOWN_MASK));
        pzWytnij.addActionListener(e -> textArea.cut());

        JMenuItem pzWklej = new JMenuItem("Wklej");
        pzWklej.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK));
        pzWklej.addActionListener(e -> textArea.paste());

        JMenuItem pzCofnij = new JMenuItem("Cofnij");
        pzCofnij.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK));
        pzCofnij.addActionListener(e -> {
            if (undoManager.canUndo()) {
                undoManager.undo();
            }
        });

        JMenuItem pzPonow = new JMenuItem("Ponów");
        pzPonow.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_DOWN_MASK));
        pzPonow.addActionListener(e -> {
            if (undoManager.canRedo()) {
                undoManager.redo();
            }
        });

        JMenuItem pzZaznaczWszystko = new JMenuItem("Zaznacz wszystko");
        pzZaznaczWszystko.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_DOWN_MASK));
        pzZaznaczWszystko.addActionListener(e -> textArea.selectAll());

        JMenuItem pzUsun = new JMenuItem("Usuń");
        pzUsun.setAccelerator(KeyStroke.getKeyStroke((char) KeyEvent.VK_DELETE));
        pzUsun.addActionListener(e -> textArea.replaceSelection(""));

        JMenuItem pzZnajdzIZamien = new JMenuItem("Znajdź i zamień");
        pzZnajdzIZamien.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK));
        pzZnajdzIZamien.addActionListener(e -> new FindReplaceDialog(window, textArea, fileManager).setVisible(true));

        mnEdycja.add(pzKopiuj);
        mnEdycja.add(pzWytnij);
        mnEdycja.add(pzWklej);
        mnEdycja.addSeparator();
        mnEdycja.add(pzCofnij);
        mnEdycja.add(pzPonow);
        mnEdycja.addSeparator();
        mnEdycja.add(pzZaznaczWszystko);
        mnEdycja.add(pzUsun);
        mnEdycja.addSeparator();
        mnEdycja.add(pzZnajdzIZamien);


        // -=-=- Pozycje menu Widok -=-=-
        JMenu pmnMotyw = new JMenu("Motyw"); //pmn - pod-menu

        JMenuItem pzCzarny = new JMenuItem("Czarny");
        pzCzarny.addActionListener(e -> {
            prefs.put("theme", "dark");
            FlatDarkLaf.setup();
            SwingUtilities.updateComponentTreeUI(window);
        });

        JMenuItem pzBialy = new JMenuItem("Biały");
        pzBialy.addActionListener(e -> {
            prefs.put("theme", "light");
            FlatLightLaf.setup();
            SwingUtilities.updateComponentTreeUI(window);
        });

        JMenuItem pzSystem = new JMenuItem("Systemowy");
        pzSystem.addActionListener(e -> {
            prefs.put("theme", "system");
            OsThemeDetector detector = OsThemeDetector.getDetector();
            if (detector.isDark()) FlatDarkLaf.setup();
            else FlatLightLaf.setup();
            SwingUtilities.updateComponentTreeUI(window);
        });

        JCheckBoxMenuItem pzZawijanieLinii = new JCheckBoxMenuItem("Zawijanie linii");
        pzZawijanieLinii.setSelected(prefs.getBoolean("lineWrap", true));
        pzZawijanieLinii.addActionListener(e -> {
            boolean wrap = pzZawijanieLinii.isSelected();
            prefs.putBoolean("lineWrap", wrap);
            textArea.setLineWrap(wrap);
        });

        JMenuItem pzCzcionka = new JMenuItem("Czcionka i rozmiar");
        pzCzcionka.addActionListener(e -> {
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


        mnWidok.add(pmnMotyw);
        pmnMotyw.add(pzCzarny);
        pmnMotyw.add(pzBialy);
        pmnMotyw.add(pzSystem);
        mnWidok.add(pzZawijanieLinii);
        mnWidok.add(pzCzcionka);


        // -=-=- Pozycje menu Pomoc -=-=-
        JMenuItem pzOProgramie = new JMenuItem("O programie", KeyEvent.VK_N);
        pzOProgramie.addActionListener(e -> {
            JOptionPane.showMessageDialog(window, "SwingNotes 1.0\n" + "Prosty notatnik napisany w Javie z użyciem biblioteki Swing.", "O programie", JOptionPane.INFORMATION_MESSAGE);
        });

        mnPomoc.add(pzOProgramie);

        add(mnPlik);
        add(mnEdycja);
        add(mnWidok);
        add(mnPomoc);
    }

    private static void makeRecentlyOpenedFilesMenuContent(JTextArea textArea, FileManager fileManager, Preferences prefs, JMenu pmnOstatnioOtwierane, JFrame window) {
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
