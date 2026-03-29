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
    private File currentFile = null;
    private boolean isFileChanged = false;
    private final JFrame window;
    @SuppressWarnings("FieldMayBeFinal")
    private int[] previousSearchPosition = {0}; //trick, bo Java wymaga, żeby zmienne w anonimowych klasach się nie zmieniały

    public FileManager(JFrame window) {
        this.window = window;
    }

    public void openFile(RSyntaxTextArea textArea, String path, Preferences prefs) {
        if (path == null) {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showOpenDialog(window) == JFileChooser.APPROVE_OPTION) {
                currentFile = chooser.getSelectedFile();
                try {
                    loadFile(textArea);
                    addFileToRecents(prefs, currentFile.getAbsolutePath());
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(window,
                            "Błąd odczytu pliku: " + ex.getMessage(),
                            "Błąd", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            currentFile = new File(path);
            try {
                loadFile(textArea);
                addFileToRecents(prefs, path);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(window,
                        "Błąd odczytu pliku: " + ex.getMessage(),
                        "Błąd", JOptionPane.ERROR_MESSAGE);
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
        if(recentFilesList.size() > maxLength) {
            recentFilesList = new ArrayList<>(recentFilesList.subList(0, maxLength));
        }

        prefs.put("recentFiles", String.join(",", recentFilesList));
    }

    private void loadFile(RSyntaxTextArea textArea) throws IOException {
        String fileContent = new String(Files.readAllBytes(currentFile.toPath()));
        textArea.setText(fileContent);
        textArea.setSyntaxEditingStyle(FileManager.getSyntaxStyle(currentFile.getName()));
        setFileChanged(false);
        window.setTitle("SwingNotes - " + currentFile.getName());
    }

    public void saveFile(RSyntaxTextArea textArea) {
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

    public void saveAs(RSyntaxTextArea textArea) {
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

    public void newFile(RSyntaxTextArea textArea) {
        if(!textArea.getText().isEmpty()) {
            int choice = JOptionPane.showOptionDialog(window,
                    "Czy zapisać zmiany przed utworzeniem nowego pliku?",
                    "Nowy plik", JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE, null,
                    new String[]{"Zapisz", "Nie zapisuj", "Anuluj"}, 0);
            if (choice == JOptionPane.YES_OPTION) saveFile(textArea);
            else if (choice == JOptionPane.CANCEL_OPTION || choice == JOptionPane.CLOSED_OPTION) return;
        }
        textArea.setText("");
        currentFile = null;
        isFileChanged = false;
        window.setTitle("SwingNotes - Nowy plik");
    }


    // -=- wyszukiwanie i zamienianie -=-
    public void find(RSyntaxTextArea textArea, String searchValue) {
        String text = textArea.getText();
        int cursorPosition = text.indexOf(searchValue, previousSearchPosition[0]); //zwraca -1, jeśli nie znaleziono
        if (cursorPosition != -1) {
            textArea.select(cursorPosition, cursorPosition + searchValue.length()); //zaznaczenie szukanego wyrażenia
            previousSearchPosition[0] = cursorPosition + 1;
        }
        else {
            resetSearchPosition();
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

    public boolean isFileChanged() {
        return isFileChanged;
    }

    public void setFileChanged(boolean fileChanged) {
        isFileChanged = fileChanged;
    }

    public void resetSearchPosition() {
        previousSearchPosition[0] = 0;
    }
}
