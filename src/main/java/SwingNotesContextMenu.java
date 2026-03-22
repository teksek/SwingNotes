import javax.swing.*;
import javax.swing.undo.UndoManager;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

public class SwingNotesContextMenu extends JPopupMenu {
    public SwingNotesContextMenu(JTextArea textArea, UndoManager undoManager) {
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

        add(pzKopiuj);
        add(pzWytnij);
        add(pzWklej);
        addSeparator();
        add(pzCofnij);
        add(pzPonow);
        addSeparator();
        add(pzZaznaczWszystko);
        add(pzUsun);
    }
}
