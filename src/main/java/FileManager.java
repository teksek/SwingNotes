import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.prefs.Preferences;

public class FileManager {
    private final JFrame window;

    public FileManager(JFrame window) {
        this.window = window;
    }

    public void openFile(Tab activeTab, String path, Preferences prefs) {
        if (!canDiscardChanges(activeTab, "dialog.discardChanges.msg")) return;
        if (path == null) {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showOpenDialog(window) == JFileChooser.APPROVE_OPTION) {
                activeTab.setFile(chooser.getSelectedFile());
                try {
                    loadFile(activeTab);
                    addFileToRecents(prefs, activeTab.getFile().getAbsolutePath());
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(window,
                            I18n.get("dialog.readError.msg", ex.getMessage()),
                            I18n.get("msg.error"), JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            activeTab.setFile(new File(path));
            try {
                loadFile(activeTab);
                addFileToRecents(prefs, path);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(window,
                        I18n.get("dialog.readError.msg", ex.getMessage()),
                        I18n.get("msg.error"), JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void addFileToRecents(Preferences prefs, String path) {
        String recents = prefs.get("recentFiles", "none");

        List<String> recentFilesList;
        if (recents.equals("none")) {
            recentFilesList = new ArrayList<>();
        } else {
            recentFilesList = new ArrayList<>(Arrays.asList(recents.split(",")));
        }

        recentFilesList.remove(path); //usuwa pierwsze napotkanie
        recentFilesList.addFirst(path);

        int maxLength = Integer.parseInt(prefs.get("recentFilesMenuLength", "5"));
        if (recentFilesList.size() > maxLength) {
            recentFilesList = new ArrayList<>(recentFilesList.subList(0, maxLength));
        }

        prefs.put("recentFiles", String.join(",", recentFilesList));
    }

    private void loadFile(Tab activeTab) throws IOException {
        File currentFile = activeTab.getFile();
        RSyntaxTextArea textArea = activeTab.getTextArea();
        String fileContent = new String(Files.readAllBytes(currentFile.toPath()));
        textArea.setText(fileContent);
        textArea.setSyntaxEditingStyle(FileManager.getSyntaxStyle(currentFile.getName()));
        activeTab.setFileChanged(false);
        activeTab.setLastSavedContent(activeTab.getTextArea().getText());

        window.setTitle("SwingNotes - " + currentFile.getName());
        Main.updateActiveTabUI();
    }

    public void saveFile(Tab activeTab) {
        File currentFile = activeTab.getFile();

        if (currentFile == null) {
            saveAs(activeTab);
        } else {
            try {
                Files.write(currentFile.toPath(), activeTab.getTextArea().getText().getBytes());
                activeTab.setFileChanged(false);
                activeTab.setLastSavedContent(activeTab.getTextArea().getText());
                window.setTitle("SwingNotes - " + currentFile.getName());
                Main.updateActiveTabUI();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(window,
                        I18n.get("dialog.saveError.msg", ex.getMessage()),
                        I18n.get("msg.error"), JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void saveAs(Tab activeTab) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Text files (*.txt)", "txt"));
        if (chooser.showSaveDialog(window) == JFileChooser.APPROVE_OPTION) {
            File newFile = chooser.getSelectedFile();
            if (!newFile.getName().contains(".")) {
                newFile = new File(newFile.getAbsolutePath() + ".txt");
            }
            activeTab.setFile(newFile);
            saveFile(activeTab);
        }
    }

    public void newFile(Tab activeTab) {
        if (!canDiscardChanges(activeTab, "dialog.saveChangesBeforeCreatingNewFile.msg")) return;
        activeTab.setLastSavedContent("");
        activeTab.getTextArea().setText("");
        activeTab.setFile(null);
        activeTab.setFileChanged(false);
        window.setTitle("SwingNotes - " + I18n.get("file.newFile"));
        Main.updateActiveTabUI();
    }


    // -=- wyszukiwanie i zamienianie -=-
    public void find(RSyntaxTextArea textArea, String searchValue, int[] previousSearchPosition, Tab activeTab) {
        String text = textArea.getText();
        int cursorPosition = text.indexOf(searchValue, previousSearchPosition[0]); //zwraca -1, jeśli nie znaleziono
        if (cursorPosition != -1) {
            textArea.select(cursorPosition, cursorPosition + searchValue.length()); //zaznaczenie szukanego wyrażenia
            previousSearchPosition[0] = cursorPosition + 1;
        } else {
            activeTab.resetSearchPosition();
            cursorPosition = text.indexOf(searchValue);
            if (cursorPosition != -1) {
                textArea.select(cursorPosition, cursorPosition + searchValue.length());
                previousSearchPosition[0] = cursorPosition + 1;
            }
        }
    }

    public void replace(RSyntaxTextArea textArea, String replaceWith) {
        if (textArea.getSelectedText() != null) textArea.replaceSelection(replaceWith);
    }

    public void replaceAll(RSyntaxTextArea textArea, String searchValue, String replaceWith) {
        if (!searchValue.isEmpty()) textArea.setText(textArea.getText().replace(searchValue, replaceWith));
    }
    // -=--=--=--=--=--=--=--=--=--=--=-

    public static String getSyntaxStyle(String fileName) {
        if (fileName == null) return SyntaxConstants.SYNTAX_STYLE_NONE;
        int dot = fileName.lastIndexOf('.');
        if (dot == -1) return SyntaxConstants.SYNTAX_STYLE_NONE;
        return switch (fileName.substring(dot + 1).toLowerCase()) {
            case "java"         -> SyntaxConstants.SYNTAX_STYLE_JAVA;
            case "py"           -> SyntaxConstants.SYNTAX_STYLE_PYTHON;
            case "js"           -> SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT;
            case "ts"           -> SyntaxConstants.SYNTAX_STYLE_TYPESCRIPT;
            case "html"         -> SyntaxConstants.SYNTAX_STYLE_HTML;
            case "css"          -> SyntaxConstants.SYNTAX_STYLE_CSS;
            case "xml"          -> SyntaxConstants.SYNTAX_STYLE_XML;
            case "json"         -> SyntaxConstants.SYNTAX_STYLE_JSON;
            case "php"          -> SyntaxConstants.SYNTAX_STYLE_PHP;
            case "c"            -> SyntaxConstants.SYNTAX_STYLE_C;
            case "cpp"          -> SyntaxConstants.SYNTAX_STYLE_CPLUSPLUS;
            case "cs"           -> SyntaxConstants.SYNTAX_STYLE_CSHARP;
            case "sh"           -> SyntaxConstants.SYNTAX_STYLE_UNIX_SHELL;
            case "bat"          -> SyntaxConstants.SYNTAX_STYLE_WINDOWS_BATCH;
            case "sql"          -> SyntaxConstants.SYNTAX_STYLE_SQL;
            case "md"           -> SyntaxConstants.SYNTAX_STYLE_MARKDOWN;
            case "yaml", "yml"  -> SyntaxConstants.SYNTAX_STYLE_YAML;
            case "properties"   -> SyntaxConstants.SYNTAX_STYLE_PROPERTIES_FILE;
            case "kt"           -> SyntaxConstants.SYNTAX_STYLE_KOTLIN;
            case "groovy"       -> SyntaxConstants.SYNTAX_STYLE_GROOVY;
            case "rb"           -> SyntaxConstants.SYNTAX_STYLE_RUBY;
            case "go"           -> SyntaxConstants.SYNTAX_STYLE_GO;
            case "rs"           -> SyntaxConstants.SYNTAX_STYLE_RUST;
            case "lua"          -> SyntaxConstants.SYNTAX_STYLE_LUA;
            case "perl"         -> SyntaxConstants.SYNTAX_STYLE_PERL;
            default             -> SyntaxConstants.SYNTAX_STYLE_NONE;
        };
    }

    public boolean canDiscardChanges(Tab activeTab, String messageKey) {
        if (!activeTab.isFileChanged()) {
            return true; // brak zmian, można porzucić zmiany
        }

        String fileName = activeTab.getTitle().replace("*", "");

        int option = JOptionPane.showOptionDialog(window,
                I18n.get(messageKey, fileName),
                I18n.get("dialog.discardChanges.title"), JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE, null,
                new String[]{I18n.get("msg.option.save"), I18n.get("msg.option.dontSave"), I18n.get("msg.option.cancel")}, 0);
        if (option == JOptionPane.YES_OPTION) {
            saveFile(activeTab);
            return !activeTab.isFileChanged(); //jeśli zapisano pomyślnie można kontynuować
        } else if (option == JOptionPane.NO_OPTION) {
            return true; //porzucenie zmian
        }

        return false; //cancel lub zamknięcie okna - przerwanie wykonywanej operacji
    }
}
