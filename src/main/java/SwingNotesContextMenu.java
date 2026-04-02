import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import javax.swing.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

public class SwingNotesContextMenu extends JPopupMenu {
    public SwingNotesContextMenu(RSyntaxTextArea textArea) {
        // Nazwy zmiennych pozycji menu zawierają w sobie suffix: -Item (np. copyItem)

        JMenuItem copyItem = new JMenuItem(I18n.get("edit.copy"));
        copyItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK));
        copyItem.addActionListener(e -> textArea.copy());

        JMenuItem cutItem = new JMenuItem(I18n.get("edit.cut"));
        cutItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_DOWN_MASK));
        cutItem.addActionListener(e -> textArea.cut());

        JMenuItem pasteItem = new JMenuItem(I18n.get("edit.paste"));
        pasteItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK));
        pasteItem.addActionListener(e -> textArea.paste());

        JMenuItem undoItem = new JMenuItem(I18n.get("edit.undo"));
        undoItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK));
        undoItem.addActionListener(e -> textArea.undoLastAction());

        JMenuItem redoItem = new JMenuItem(I18n.get("edit.redo"));
        redoItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_DOWN_MASK));
        redoItem.addActionListener(e -> textArea.redoLastAction());

        JMenuItem selectEverythingItem = new JMenuItem(I18n.get("edit.selectEverything"));
        selectEverythingItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_DOWN_MASK));
        selectEverythingItem.addActionListener(e -> textArea.selectAll());

        JMenuItem deleteItem = new JMenuItem(I18n.get("edit.delete"));
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
