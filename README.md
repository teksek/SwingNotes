# SwingNotes

Prosty notatnik desktopowy napisany w Javie z użyciem biblioteki Swing.

## Funkcjonalności

- Tworzenie, otwieranie i zapisywanie plików tekstowych (.txt)
- Zapisz / Zapisz jako z obsługą `JFileChooser`
- Kopiuj, wytnij, wklej, zaznacz wszystko
- Pasek statusu z licznikiem znaków i linii
- Tytuł okna aktualizowany po otwarciu/zapisaniu pliku
- Pytanie o zapis przy zamykaniu lub tworzeniu nowego pliku
- Skróty klawiszowe (Ctrl+N, Ctrl+O, Ctrl+S, Ctrl+Shift+S)

## Technologie

- Java SE
- javax.swing
- java.nio.file

## Uruchomienie

Skompiluj i uruchom `Main.java` w dowolnym IDE obsługującym Javę (np. IntelliJ IDEA).

## Struktura

```
└── src/
    ├── FileManager.java          # Operacje na plikach
    ├── Main.java                 # Główne okno aplikacji
    └── SwingNotesMenuBar.java    # Logika menu
```

## Kontekst

Projekt edukacyjny stworzony w ramach nauki programowania w technikum. Rozbudowana wersja ćwiczenia z zajęć — dodane m.in. rzeczywisty I/O plików, status bar i wiele innych rzeczy.