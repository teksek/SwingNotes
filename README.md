# SwingNotes

Prosty notatnik desktopowy napisany w Javie z użyciem biblioteki Swing.

## Funkcjonalności

- Tworzenie, otwieranie i zapisywanie plików tekstowych (.txt)
- Automatyczne dodawanie rozszerzenia `.txt` przy zapisie
- Cofnij / Ponów (UndoManager)
- Kopiuj, wytnij, wklej, zaznacz wszystko
- Znajdź i zamień (z obsługą "Zamień wszystko")
- Menu kontekstowe (prawy przycisk myszy)
- Zmiana czcionki i rozmiaru
- Włączanie/wyłączanie zawijania linii
- Motywy: jasny, ciemny, systemowy (FlatLaf)
- Zapisz / Zapisz jako z obsługą `JFileChooser`
- Pasek statusu z licznikiem znaków, słów i linii
- Tytuł okna aktualizowany po otwarciu/zapisaniu pliku
- Pytanie o zapis przy zamykaniu lub tworzeniu nowego pliku
- Skróty klawiszowe (Ctrl+N, Ctrl+O, Ctrl+S, Ctrl+Shift+S, Ctrl+F, Ctrl+Z, Ctrl+Y...)
- Zapamiętywanie preferencji użytkownika (czcionka, zawijanie linii, motyw)
- Drukowanie zawartości
- Ostatnio otwierane pliki w menu Plik z możliwością ustawienia liczby przechowywanych pozycji i ich wyczyszczenia

## Technologie

- Java SE — język i środowisko uruchomieniowe
- Swing (`javax.swing`) — biblioteka GUI
- NIO (`java.nio.file`) — operacje na plikach
- [FlatLaf 3.7.1](https://github.com/JFormDesigner/FlatLaf) — nowoczesne motywy UI
- [jSystemThemeDetector 3.8](https://github.com/Dansoftowner/jSystemThemeDetector) — wykrywanie motywu systemowego
- [FontChooser 3.1.0](https://github.com/dheid/fontchooser) — okno wyboru czcionki

## Uruchomienie

Sklonuj repozytorium i uruchom przez Gradle:

```
./gradlew run
```

Lub otwórz projekt w IntelliJ IDEA i uruchom `Main.java`.

## Struktura

```
src/main/java/
├── Main.java                  # Główne okno aplikacji
├── FileManager.java           # Operacje na plikach i logika szukania/zamiany
├── SwingNotesMenuBar.java     # Pasek menu
├── SwingNotesContextMenu.java # Menu kontekstowe
└── FindReplaceDialog.java     # Okno znajdź i zamień
```

## Kontekst

Projekt edukacyjny stworzony w ramach nauki programowania w technikum. Rozbudowana wersja ćwiczenia z zajęć — dodane m.in. rzeczywisty I/O plików, obsługa preferencji, motywy UI, ostatnio otwierane pliki, drukowanie i wiele innych funkcji.

## Planowane funkcje (v1.x)
- Obsługa wielu dokumentów w zakładkach
- Przeciąganie i upuszczanie plików (drag-and-drop)
- Implementacja RTextScrollPane
