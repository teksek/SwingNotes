import javax.swing.*;
import javax.swing.undo.UndoManager;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

public class SwingNotesContextMenu extends JPopupMenu {
    public SwingNotesContextMenu(JTextArea textArea, UndoManager undoManager) {
        // Nazwy zmiennych pozycji menu zawierają w sobie suffix: -Item (np. copyItem)

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
        undoItem.addActionListener(e -> {
            if (undoManager.canUndo()) {
                undoManager.undo();
            }
        });

        JMenuItem redoItem = new JMenuItem("Ponów");
        redoItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_DOWN_MASK));
        redoItem.addActionListener(e -> {
            if (undoManager.canRedo()) {
                undoManager.redo();
            }
        });

        JMenuItem selectEverythingItem = new JMenuItem("Zaznacz wszystko");
        selectEverythingItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_DOWN_MASK));
        selectEverythingItem.addActionListener(e -> textArea.selectAll());

        JMenuItem deleteItem = new JMenuItem("Usuń");
        deleteItem.setAccelerator(KeyStroke.getKeyStroke((char) KeyEvent.VK_DELETE));
        deleteItem.addActionListener(e -> textArea.replaceSelection(""));

        add(copyItem);
        add(cutItem);
        add(pasteItem);
        addSeparator();
        add(undoItem);
        add(redoItem);
        addSeparator();
        add(selectEverythingItem);
        add(deleteItem);
    }
}
