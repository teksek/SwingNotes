import javax.swing.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.*;

public class SwingNotes extends JMenuBar {
    private JTextArea obszarTekstu;
    private JFrame okno;
    private File aktualnyPlik = null;
    public boolean isFileChanged = false;

    public SwingNotes(JTextArea obszarTekstu, JFrame okno) {
        this.obszarTekstu = obszarTekstu;
        this.okno = okno;

        JMenu mnPlik = new JMenu("Plik");
        mnPlik.setMnemonic(KeyEvent.VK_P);
        JMenu mnEdycja = new JMenu("Edycja");
        mnEdycja.setMnemonic('E');

        // -=-=- Pozycje menu Plik -=-=-
        JMenuItem pzNowy = new JMenuItem("Nowy plik", KeyEvent.VK_N);
        pzNowy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK));
        pzNowy.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                nowyPlik();
            }
        });

        JMenuItem pzOtworz = new JMenuItem("Otwórz plik", KeyEvent.VK_O);
        pzOtworz.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
        pzOtworz.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                otworzPlik();
            }
        });

        JMenuItem pzZapisz = new JMenuItem("Zapisz", KeyEvent.VK_Z);
        pzZapisz.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
        pzZapisz.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                zapiszPlik();
            }
        });

        JMenuItem pzZapiszJako = new JMenuItem("Zapisz jako...");
        pzZapiszJako.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
                InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
        pzZapiszJako.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                zapiszJako();
            }
        });

        JMenuItem pzKoniec = new JMenuItem("Koniec", KeyEvent.VK_K);
        pzKoniec.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK));
        pzKoniec.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        mnPlik.add(pzNowy);
        mnPlik.add(pzOtworz);
        mnPlik.addSeparator();
        mnPlik.add(pzZapisz);
        mnPlik.add(pzZapiszJako);
        mnPlik.addSeparator();
        mnPlik.add(pzKoniec);

        // -=-=- Pozycje menu Edycja -=-=-
        JMenuItem pzKopiuj = new JMenuItem("Kopiuj");
        pzKopiuj.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK));
        pzKopiuj.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                obszarTekstu.copy();
            }
        });

        JMenuItem pzWytnij = new JMenuItem("Wytnij");
        pzWytnij.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_DOWN_MASK));
        pzWytnij.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                obszarTekstu.cut();
            }
        });

        JMenuItem pzWklej = new JMenuItem("Wklej");
        pzWklej.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK));
        pzWklej.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                obszarTekstu.paste();
            }
        });

        JMenuItem pzZaznaczWszystko = new JMenuItem("Zaznacz wszystko");
        pzZaznaczWszystko.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_DOWN_MASK));
        pzZaznaczWszystko.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                obszarTekstu.selectAll();
            }
        });

        JRadioButtonMenuItem pzDopisywanie = new JRadioButtonMenuItem("Tryb dopisywania");
        pzDopisywanie.setSelected(true);

        JRadioButtonMenuItem pzNadpisywanie = new JRadioButtonMenuItem("Tryb zastępowania");

        ButtonGroup grpMenu = new ButtonGroup();
        grpMenu.add(pzDopisywanie);
        grpMenu.add(pzNadpisywanie);

        mnEdycja.add(pzKopiuj);
        mnEdycja.add(pzWytnij);
        mnEdycja.add(pzWklej);
        mnEdycja.addSeparator();
        mnEdycja.add(pzZaznaczWszystko);
        mnEdycja.addSeparator();
        mnEdycja.add(pzDopisywanie);
        mnEdycja.add(pzNadpisywanie);

        add(mnPlik);
        add(mnEdycja);

        okno.addWindowListener(new WindowListener() {
            @Override public void windowOpened(WindowEvent e) { }

            @Override
            public void windowClosing(WindowEvent e) {
                if(!obszarTekstu.getText().isEmpty() && isFileChanged) {
                    int wybor = JOptionPane.showOptionDialog(okno,
                            "Czy zapisać zmiany przed zamknięciem programu?",
                            "Zamykanie", JOptionPane.YES_NO_CANCEL_OPTION,
                            JOptionPane.QUESTION_MESSAGE, null,
                            new String[]{"Zapisz", "Nie zapisuj", "Anuluj"}, 0);
                    if (wybor == 0) {
                        zapiszJako();
                    } else if (wybor == 1) {
                        System.exit(0);
                    } else if (wybor == 2) {
                        return;
                    }
                } else {
                    System.exit(0);
                }
            }

            @Override public void windowClosed(WindowEvent e) { }
            @Override public void windowIconified(WindowEvent e) { }
            @Override public void windowDeiconified(WindowEvent e) { }
            @Override public void windowActivated(WindowEvent e) { }
            @Override public void windowDeactivated(WindowEvent e) { }
        });
    }

    private void nowyPlik() {
        if (!obszarTekstu.getText().isEmpty()) {
            int wybor = JOptionPane.showConfirmDialog(okno,
                    "Czy zapisać zmiany przed utworzeniem nowego pliku?",
                    "Nowy plik", JOptionPane.YES_NO_CANCEL_OPTION);
            if (wybor == JOptionPane.YES_OPTION) {
                zapiszPlik();
            } else if (wybor == JOptionPane.CANCEL_OPTION) {
                return;
            }
        }
        obszarTekstu.setText("");
        aktualnyPlik = null;
        isFileChanged = false;
        okno.setTitle("SwingNotes - Nowy plik");
    }

    private void otworzPlik() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(okno) == JFileChooser.APPROVE_OPTION) {
            try {
                aktualnyPlik = chooser.getSelectedFile();
                String zawartosc = new String(Files.readAllBytes(aktualnyPlik.toPath()));
                obszarTekstu.setText(zawartosc);
                isFileChanged = false;
                okno.setTitle("SwingNotes - " + aktualnyPlik.getName());
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(okno,
                        "Błąd odczytu pliku: " + ex.getMessage(),
                        "Błąd", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    protected void zapiszPlik() {
        if (aktualnyPlik == null) {
            zapiszJako();
        } else {
            try {
                Files.write(aktualnyPlik.toPath(), obszarTekstu.getText().getBytes());
                okno.setTitle("SwingNotes - " + aktualnyPlik.getName());
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(okno,
                        "Błąd zapisu pliku: " + ex.getMessage(),
                        "Błąd", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void zapiszJako() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showSaveDialog(okno) == JFileChooser.APPROVE_OPTION) {
            aktualnyPlik = chooser.getSelectedFile();
            zapiszPlik();
        }
    }

    public void setIsFileChanged(boolean changed) {
        isFileChanged = changed;
    }

    //TODO:
    // PP - obsluga linkow
    // JEditorpane sprawdzic mozliwosc kolorowania
    // zrobic mozliwosc zmiany czcionek
    // dodać menu kontekstowe
    // zrobić undo poprzed UndoManager
    // możliwość zmiany czcionki poprzez JFontChooser ALBO zmiana rozmiaru poprzez po prostu JComboBox
    // ciemny motyw?
    // funkcja szukania w pliku
    // funkcja szukania i zamiany w pliku (JDialog?)
    // numerowanie linii? jak w IDE
}