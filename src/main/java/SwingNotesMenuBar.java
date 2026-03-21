import javax.swing.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

public class SwingNotesMenuBar extends JMenuBar {

    public SwingNotesMenuBar(JTextArea textArea, FileManager fileManager) {
        JMenu mnPlik = new JMenu("Plik");
        mnPlik.setMnemonic(KeyEvent.VK_P);
        JMenu mnEdycja = new JMenu("Edycja");
        mnEdycja.setMnemonic('E');

        // -=-=- Pozycje menu Plik -=-=-
        JMenuItem pzNowy = new JMenuItem("Nowy plik", KeyEvent.VK_N);
        pzNowy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK));
        pzNowy.addActionListener(e -> fileManager.newFile(textArea));

        JMenuItem pzOtworz = new JMenuItem("Otwórz plik", KeyEvent.VK_O);
        pzOtworz.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
        pzOtworz.addActionListener(e -> fileManager.openFile(textArea));

        JMenuItem pzZapisz = new JMenuItem("Zapisz", KeyEvent.VK_Z);
        pzZapisz.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
        pzZapisz.addActionListener(e -> fileManager.saveFile(textArea));

        JMenuItem pzZapiszJako = new JMenuItem("Zapisz jako...");
        pzZapiszJako.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
                InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
        pzZapiszJako.addActionListener(e -> fileManager.saveAs(textArea));

        JMenuItem pzKoniec = new JMenuItem("Koniec", KeyEvent.VK_K);
        pzKoniec.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK));
        pzKoniec.addActionListener(e -> System.exit(0));

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
        pzKopiuj.addActionListener(e -> textArea.copy());

        JMenuItem pzWytnij = new JMenuItem("Wytnij");
        pzWytnij.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_DOWN_MASK));
        pzWytnij.addActionListener(e -> textArea.cut());

        JMenuItem pzWklej = new JMenuItem("Wklej");
        pzWklej.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK));
        pzWklej.addActionListener(e -> textArea.paste());

        JMenuItem pzZaznaczWszystko = new JMenuItem("Zaznacz wszystko");
        pzZaznaczWszystko.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_DOWN_MASK));
        pzZaznaczWszystko.addActionListener(e -> textArea.selectAll());

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
    }
}
