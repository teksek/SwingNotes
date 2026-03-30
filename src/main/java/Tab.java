import org.fife.rsta.ac.LanguageSupportFactory;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.HyperlinkEvent;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.prefs.Preferences;

public class Tab extends JPanel {
    private final RSyntaxTextArea textArea;
    private File file;
    private boolean fileChanged;
    @SuppressWarnings("FieldMayBeFinal")
    private int[] previousSearchPosition = {0}; //trick, bo Java wymaga, żeby zmienne w anonimowych klasach się nie zmieniały

    public Tab(FileManager fileManager, Preferences prefs, JLabel statusBar) {
        setLayout(new BorderLayout());

        this.textArea = new RSyntaxTextArea(); //obszar tekstu
        RTextScrollPane scrollPane = new RTextScrollPane(textArea); //ustawienie możliwości scrollowania obszaru tekstu
        this.file = null;
        this.fileChanged = false;

        add(scrollPane, BorderLayout.CENTER); //zakładka jest JPanelem który zawiera scrollPane

        LanguageSupportFactory.get().register(textArea);

        String fontName = prefs.get("fontName", "Monospaced");
        int fontSize = prefs.getInt("fontSize", 14);
        textArea.setFont(new Font(fontName, Font.PLAIN, fontSize));

        boolean lineWrap = prefs.getBoolean("lineWrap", true);
        textArea.setLineWrap(lineWrap);
        textArea.setWrapStyleWord(true);

        textArea.setComponentPopupMenu(new SwingNotesContextMenu(textArea));

        // aktualizacja paska statusu przy każdej zmianie tekstu
        textArea.getDocument().addDocumentListener(new DocumentListener() {
            private void updateStatusBarOnChange() {
                int charsCount = textArea.getText().length();
                int linesCount = textArea.getLineCount();
                int wordCount = textArea.getText().isBlank() ? 0 : textArea.getText().split("\\s+").length;
                statusBar.setText("Znaki: " + charsCount + " | Słowa: " + wordCount + " | Linie: " + linesCount);
                Tab.this.fileChanged = charsCount != 0;
            }

            public void insertUpdate(DocumentEvent e) { updateStatusBarOnChange(); }
            public void removeUpdate(DocumentEvent e) { updateStatusBarOnChange(); }
            public void changedUpdate(DocumentEvent e) { updateStatusBarOnChange(); }
        });

        // obsługa linków
        textArea.addHyperlinkListener(e -> {
            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                    try {
                        Desktop.getDesktop().browse(e.getURL().toURI());
                    } catch (IOException | URISyntaxException ex) {
                        throw new RuntimeException(ex);
                    }
                } else {
                    String os = System.getProperty("os.name").toLowerCase();
                    try {
                        if (os.contains("mac")) { //mac
                            Runtime.getRuntime().exec(new String[]{"open", e.getURL().toString()});
                        } else { //linux
                            Runtime.getRuntime().exec(new String[]{"xdg-open", e.getURL().toString()});
                        }
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
        });

        TransferHandler originalHandler = textArea.getTransferHandler();

        TransferHandler transferHandler = new TransferHandler() { // implementacja mechanizmu drag-and-drop plików ORAZ tekstu na poszczególną zakładkę (textArea)
            @Override
            public boolean canImport(TransferSupport support) {
                return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor)
                        || support.isDataFlavorSupported(DataFlavor.stringFlavor);
            }

            @SuppressWarnings("unchecked")
            @Override
            public boolean importData(TransferSupport support) {
                if (support.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                    try {
                        java.util.List<File> files = (java.util.List<File>) support.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                        if (!files.isEmpty()) {
                            fileManager.openFile(Tab.this, files.getFirst().getAbsolutePath(), prefs);
                        }
                        return true;
                    } catch (Exception e) {
                        return false;
                    }
                }
                return originalHandler.importData(support);
            }
        };

        textArea.setTransferHandler(transferHandler);
    }

    public String getTitle() {
        return file != null ? file.getName() : "Nowy plik"; //jeśli file != null jest prawdą, to zwraca file.getName() a jeśli fałszem, to zwraca "Nowy plik"
    }

    public RSyntaxTextArea getTextArea() { return textArea; }
    public File getFile() { return file; }
    public boolean isFileChanged() { return fileChanged; }
    public void setFile(File file) { this.file = file; }
    public void setFileChanged(boolean changed) { this.fileChanged = changed; }
    public int[] getPreviousSearchPosition() { return previousSearchPosition; }
    public void resetSearchPosition() { previousSearchPosition[0] = 0; }
}
