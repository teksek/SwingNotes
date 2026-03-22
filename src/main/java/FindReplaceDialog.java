import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

public class FindReplaceDialog extends JDialog {
    public FindReplaceDialog(JFrame window, JTextArea textArea, FileManager fileManager) {
        super(window, "Znajdź i zamień", false);

        JPanel panel = new JPanel(new GridLayout(3, 2, 5, 5));

        JTextField txtSzukaj = new JTextField();
        JTextField txtZamien = new JTextField();

        txtSzukaj.getDocument().addDocumentListener(new DocumentListener() {
            private void reset() { fileManager.resetPozycjaSzukania(); }
            public void insertUpdate(DocumentEvent e) { reset(); }
            public void removeUpdate(DocumentEvent e) { reset(); }
            public void changedUpdate(DocumentEvent e) { reset(); }
        });

        txtSzukaj.setPreferredSize(new Dimension(150, 25));
        txtZamien.setPreferredSize(new Dimension(150, 25));

        JButton btnSzukaj = new JButton("Szukaj");
        JButton btnZamien = new JButton("Zamień");
        JButton btnZamienWszystko = new JButton("Zamień wszystko");

        btnSzukaj.addActionListener(event -> fileManager.znajdz(textArea, txtSzukaj.getText()));

        btnZamien.addActionListener(event -> fileManager.zamien(textArea, txtZamien.getText()));

        btnZamienWszystko.addActionListener(event -> fileManager.zamienWszystko(textArea, txtSzukaj.getText(), txtZamien.getText()));

        panel.add(new JLabel("Szukaj:"));
        panel.add(txtSzukaj);
        panel.add(new JLabel("Zamień na:"));
        panel.add(txtZamien);
        panel.add(btnSzukaj);
        panel.add(btnZamien);

        JPanel panelGlowny = new JPanel(new BorderLayout(5, 5));
        panelGlowny.add(panel, BorderLayout.CENTER);
        panelGlowny.add(btnZamienWszystko, BorderLayout.SOUTH);
        panelGlowny.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); //dodany padding, żeby nie było aż tak blisko krawędzi

        add(panelGlowny);
        setLocationRelativeTo(window);
        pack(); // dopasowuje rozmiar do zawartości
    }
}
