import org.fife.rsta.ac.LanguageSupportFactory;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.ErrorStrip;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.HyperlinkEvent;
import java.awt.*;
import java.awt.dnd.*;
import java.awt.datatransfer.DataFlavor;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.prefs.Preferences;
import java.util.List;

public class Tab extends JPanel {
    private final RSyntaxTextArea textArea;
    private File file;
    private boolean fileChanged;
    private ErrorStrip errorStrip;
    @SuppressWarnings("FieldMayBeFinal")
    private int[] previousSearchPosition = {0}; //trick, bo Java wymaga, żeby zmienne w anonimowych klasach się nie zmieniały
    private String lastSavedContent = "";

    public Tab(FileManager fileManager, Preferences prefs, JLabel statusBar) {
        setLayout(new BorderLayout());

        this.textArea = new RSyntaxTextArea(); //obszar tekstu
        RTextScrollPane scrollPane = new RTextScrollPane(textArea); //ustawienie możliwości scrollowania obszaru tekstu
        this.file = null;
        this.fileChanged = false;

        add(scrollPane, BorderLayout.CENTER); //zakładka jest JPanelem który zawiera scrollPane

        toggleErrorStrip(prefs.getBoolean("errorStrip", false));

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
                int charsCount = Tab.this.getCharsCount();
                int linesCount = Tab.this.getLinesCount();
                int wordCount = Tab.this.getWordCount();
                statusBar.setText(I18n.get("statusBar.format", charsCount, wordCount, linesCount));
                checkChanges();

                int index = Main.tabbedPane.indexOfComponent(Tab.this);
                if (index != -1) {
                    Main.tabbedPane.setTitleAt(index, Tab.this.getTitle());
                }
            }

            private void checkChanges() {
                String currentText = textArea.getText();
                boolean changed;

                if (currentText.length() != lastSavedContent.length()) changed = true;
                else changed = !currentText.equals(lastSavedContent);

                if(changed != fileChanged) {
                    setFileChanged(changed);
                    int index = Main.tabbedPane.indexOfComponent(Tab.this);
                    if(index != -1) Main.tabbedPane.setTitleAt(index, getTitle());
                    Main.updateActiveTabUI();
                }
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

        new DropTarget(textArea, new DropTargetAdapter() { // implementacja mechanizmu drag-and-drop plików
            @Override
            public void drop(DropTargetDropEvent dtde) {
                try {
                    if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                        dtde.acceptDrop(DnDConstants.ACTION_COPY);

                        @SuppressWarnings("unchecked")
                        List<File> droppedFiles = (List<File>) dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);

                        if (!droppedFiles.isEmpty()) {
                            fileManager.openFile(Tab.this, droppedFiles.getFirst().getAbsolutePath(), prefs);
                        }
                        dtde.dropComplete(true);
                    } else {
                        dtde.rejectDrop();
                    }
                } catch (Exception e) {
                    dtde.rejectDrop();
                }
            }
        });
    }

    public void toggleErrorStrip(boolean show) {
        if(show) {
            if(errorStrip == null) errorStrip = new ErrorStrip(textArea);
            add(errorStrip, BorderLayout.LINE_END); //zaraz za scrollbarem
        } else {
            if (errorStrip != null) remove(errorStrip);
        }
        revalidate();
        repaint();
    }

    public String getTitle() {
        String name = file != null ? file.getName() : I18n.get("file.newFile"); //jeśli file != null jest prawdą, to zwraca file.getName() a jeśli fałszem, to zwraca "Nowy plik"
        return fileChanged ? "*" + name : name;
    }
    public RSyntaxTextArea getTextArea() { return textArea; }
    public File getFile() { return file; }
    public boolean isFileChanged() { return fileChanged; }
    public void setFile(File file) { this.file = file; }
    public void setFileChanged(boolean changed) { this.fileChanged = changed; }
    public int[] getPreviousSearchPosition() { return previousSearchPosition; }
    public void resetSearchPosition() { previousSearchPosition[0] = 0; }
    public int getCharsCount() { return textArea.getText().length(); }
    public int getLinesCount() { return textArea.getLineCount(); }
    public int getWordCount() { return textArea.getText().isBlank() ? 0 : textArea.getText().split("\\s+").length; }
    public void setLastSavedContent(String savedContent) { this.lastSavedContent = savedContent; }
}
