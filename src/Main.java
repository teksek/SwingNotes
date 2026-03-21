import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class Main {
    private static void makeWindow() {
        JFrame window = new JFrame("SwingNotes");
        window.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        // obszar tekstu
        JTextArea textArea = new JTextArea();
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);

        // ustawienie możliwości scrollowania obszaru tekstu
        JScrollPane scrollPane = new JScrollPane(textArea);

        // pasek statusu dokumentu na dole
        JLabel statusBar = new JLabel("Znaki: 0 | Linie: 1");
        statusBar.setBorder(BorderFactory.createEmptyBorder(3, 6, 3, 6));

        FileManager fileManager = new FileManager(window);
        window.setJMenuBar(new SwingNotesMenuBar(textArea, fileManager));

        // aktualizacja paska statusu przy każdej zmianie tekstu
        textArea.getDocument().addDocumentListener(new DocumentListener() {
            private void updateStatusBarOnChange() {
                int znaki = textArea.getText().length();
                int linie = textArea.getLineCount();
                statusBar.setText("Znaki: " + znaki + " | Linie: " + linie);
                fileManager.setFileChanged(true);
            }
            public void insertUpdate(DocumentEvent e) { updateStatusBarOnChange(); }
            public void removeUpdate(DocumentEvent e) { updateStatusBarOnChange(); }
            public void changedUpdate(DocumentEvent e) { updateStatusBarOnChange(); }
        });

        window.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
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
            }
        });

        window.add(scrollPane, BorderLayout.CENTER);
        window.add(statusBar, BorderLayout.SOUTH);

        window.setSize(new Dimension(800, 600));
        window.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::makeWindow);
    }
}