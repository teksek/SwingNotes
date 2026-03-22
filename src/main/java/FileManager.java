import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class FileManager {
    private File currentFile = null;
    private boolean isFileChanged = false;
    private final JFrame window;
    @SuppressWarnings("FieldMayBeFinal")
    private int[] pozycjaSzukaniaWczesniejsza = {0};

    public FileManager(JFrame window) {
        this.window = window;
    }

    public void openFile(JTextArea textArea) {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(window) == JFileChooser.APPROVE_OPTION) {
            try {
                currentFile = chooser.getSelectedFile();
                String fileContent = new String(Files.readAllBytes(currentFile.toPath()));
                textArea.setText(fileContent);
                setFileChanged(false);
                window.setTitle("SwingNotes - " + currentFile.getName());
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(window,
                        "Błąd odczytu pliku: " + ex.getMessage(),
                        "Błąd", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void saveFile(JTextArea textArea) {
        if(currentFile == null) {
            saveAs(textArea);
        } else {
            try {
                Files.write(currentFile.toPath(), textArea.getText().getBytes());
                setFileChanged(false);
                window.setTitle("SwingNotes - " + currentFile.getName());
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(window,
                        "Błąd zapisu pliku" + ex.getMessage(),
                        "Błąd", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void saveAs(JTextArea textArea) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Text files (*.txt)", "txt"));
        if(chooser.showSaveDialog(window) == JFileChooser.APPROVE_OPTION) {
            currentFile = chooser.getSelectedFile();
            if(!currentFile.getName().contains(".")) {
                currentFile = new File(currentFile.getAbsolutePath() + ".txt");
            }
            saveFile(textArea);
        }
    }

    public void newFile(JTextArea textArea) {
        if(!textArea.getText().isEmpty()) {
            int choice = JOptionPane.showOptionDialog(window,
                    "Czy zapisać zmiany przed utworzeniem nowego pliku?",
                    "Nowy plik", JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE, null,
                    new String[]{"Zapisz", "Nie zapisuj", "Anuluj"}, 0);
            if (choice == JOptionPane.YES_OPTION) {
                saveFile(textArea);
            } else if (choice == JOptionPane.CANCEL_OPTION || choice == JOptionPane.CLOSED_OPTION) {
                return;
            }
        }
        textArea.setText("");
        currentFile = null;
        isFileChanged = false;
        window.setTitle("SwingNotes - Nowy plik");
    }

    public void znajdz(JTextArea textArea, String szukany) {
        String tekst = textArea.getText();
        int pozycja = tekst.indexOf(szukany, pozycjaSzukaniaWczesniejsza[0]); //zwraca -1, jeśli nie znaleziono
        if (pozycja != -1) {
            textArea.select(pozycja, pozycja + szukany.length()); //zaznaczenie szukanego wyrażenia
            pozycjaSzukaniaWczesniejsza[0] = pozycja + 1;
        }
    }

    public void zamien(JTextArea textArea, String zamienNa) {
        if (textArea.getSelectedText() != null) {
            textArea.replaceSelection(zamienNa);
        }
    }

    public void zamienWszystko(JTextArea textArea, String szukany, String zamienNa) {
        if (!szukany.isEmpty()) {
            textArea.setText(textArea.getText().replace(szukany, zamienNa));
        }
    }

    public boolean isFileChanged() {
        return isFileChanged;
    }

    public void setFileChanged(boolean fileChanged) {
        isFileChanged = fileChanged;
    }

    public void resetPozycjaSzukania() {
        pozycjaSzukaniaWczesniejsza[0] = 0;
    }
}
