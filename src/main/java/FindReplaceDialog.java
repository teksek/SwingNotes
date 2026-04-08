import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class FindReplaceDialog extends JDialog {
    public FindReplaceDialog(JFrame window, FileManager fileManager, Tab activeTab) {
        super(window, I18n.get("dialog.findAndReplace.title"), false);

        JPanel panel = new JPanel(new GridLayout(3, 2, 5, 5));

        JTextField txtFind = new JTextField();
        JTextField txtReplace = new JTextField();

        txtFind.getDocument().addDocumentListener(new DocumentListener() {
            private void reset() { activeTab.resetSearchPosition(); }
            public void insertUpdate(DocumentEvent e) { reset(); }
            public void removeUpdate(DocumentEvent e) { reset(); }
            public void changedUpdate(DocumentEvent e) { reset(); }
        });

        txtFind.setPreferredSize(new Dimension(150, 25));
        txtReplace.setPreferredSize(new Dimension(150, 25));

        JButton btnFind = new JButton(I18n.get("dialog.findAndReplace.button.find"));
        JButton btnReplace = new JButton(I18n.get("dialog.findAndReplace.button.replace"));
        JButton btnReplaceAll = new JButton(I18n.get("dialog.findAndReplace.button.replaceAll"));

        btnFind.addActionListener(event -> fileManager.find(activeTab.getTextArea(), txtFind.getText(), activeTab.getPreviousSearchPosition(), activeTab));

        btnReplace.addActionListener(event -> fileManager.replace(activeTab.getTextArea(), txtReplace.getText()));

        btnReplaceAll.addActionListener(event -> fileManager.replaceAll(activeTab.getTextArea(), txtFind.getText(), txtReplace.getText()));

        panel.add(new JLabel(I18n.get("dialog.findAndReplace.label.find")));
        panel.add(txtFind);
        panel.add(new JLabel(I18n.get("dialog.findAndReplace.label.replace")));
        panel.add(txtReplace);
        panel.add(btnFind);
        panel.add(btnReplace);

        JPanel mainPanel = new JPanel(new BorderLayout(5, 5));
        mainPanel.add(panel, BorderLayout.CENTER);
        mainPanel.add(btnReplaceAll, BorderLayout.SOUTH);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); //dodany padding, żeby nie było aż tak blisko krawędzi

        setupEscapeKey();
        add(mainPanel);
        setLocationRelativeTo(window);
        pack(); // dopasowuje rozmiar do zawartości
    }

    private void setupEscapeKey() {
        String cancelName = "cancel";
        InputMap inputMap = getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT); //reaguje na klawisz esc, nawet jeśli focus jest na przycisku/polu tekstowym
        ActionMap actionMap = getRootPane().getActionMap();

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), cancelName); //połączenie klawisza esc z akcją o nazwie "cancel"
        actionMap.put(cancelName, new AbstractAction() { //przypisanie do "cancel" zamknięcie okna
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose(); //zamyka okno
            }
        });
    }
}
