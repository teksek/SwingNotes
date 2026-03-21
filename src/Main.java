import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

public class Main {
    private static void makeWindow() {
        JFrame window = new JFrame("SwingNotes");
        window.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        // obszar tekstu
        JTextArea textArea = new JTextArea();
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);

        // ustawienie mozliwosci scrollowania obszaru tekstu
        JScrollPane scrollPane = new JScrollPane(textArea);

        // pasek statusu dokumentu na dole
        JLabel statusBar = new JLabel("Znaki: 0 | Linie: 1");
        statusBar.setBorder(BorderFactory.createEmptyBorder(3, 6, 3, 6));

        SwingNotes aplikacja = new SwingNotes(textArea, window);
        window.setJMenuBar(aplikacja);

        // aktualizacja paska statusu przy kazdej zmianie tekstu
        textArea.getDocument().addDocumentListener(new DocumentListener() {
            private void updateStatusBarOnChange() {
                int znaki = textArea.getText().length();
                int linie = textArea.getLineCount();
                statusBar.setText("Znaki: " + znaki + " | Linie: " + linie);
                aplikacja.setIsFileChanged(true);
            }
            public void insertUpdate(DocumentEvent e) { updateStatusBarOnChange(); }
            public void removeUpdate(DocumentEvent e) { updateStatusBarOnChange(); }
            public void changedUpdate(DocumentEvent e) { updateStatusBarOnChange(); }
        });

        window.add(scrollPane, BorderLayout.CENTER);
        window.add(statusBar, BorderLayout.SOUTH);

        window.setSize(new Dimension(800, 600));
        window.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                makeWindow();
            }
        });
    }
}