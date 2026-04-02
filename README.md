# SwingNotes

Prosty notatnik desktopowy napisany w Javie z użyciem biblioteki Swing.

## Funkcjonalności

- Tworzenie, otwieranie i zapisywanie plików tekstowych (.txt)
- Automatyczne dodawanie rozszerzenia `.txt` przy zapisie
- Obsługa wielu dokumentów jednocześnie dzięki systemowi zakładek (JTabbedPane)
- Cofnij / Ponów
- Kopiuj, wytnij, wklej, zaznacz wszystko
- Znajdź i zamień (z obsługą "Zamień wszystko")
- Menu kontekstowe (prawy przycisk myszy)
- Zmiana czcionki i rozmiaru
- Włączanie/wyłączanie zawijania linii
- Motywy UI: jasny, ciemny, systemowy (FlatLaf)
- Podświetlanie składni na podstawie rozszerzenia pliku (RSyntaxTextArea) i możliwość ręcznego ustawienia
- Motywy składni: dark, monokai, eclipse, idea, vs i inne
- Autouzupełnianie dla obsługiwanych języków (RSTALanguageSupport)
- Numerowanie linii (RTextScrollPane)
- Przeciąganie i upuszczanie plików (drag-and-drop)
- Klikalne linki (Ctrl+LPM)
- Pasek statusu z licznikiem znaków, słów i linii
- Tytuł okna aktualizowany po otwarciu/zapisaniu pliku
- Pytanie o zapis przy zamykaniu lub tworzeniu nowego pliku
- Skróty klawiszowe (Ctrl+N, Ctrl+O, Ctrl+S, Ctrl+Shift+S, Ctrl+F, Ctrl+Z, Ctrl+Y...)
- Zapamiętywanie preferencji użytkownika (czcionka, zawijanie linii, motywy)
- Drukowanie zawartości
- Ostatnio otwierane pliki w menu Plik z możliwością ustawienia liczby pozycji i ich wyczyszczenia
- Wielojęzyczność (i18n): Interfejs w językach PL, EN, DE, ES, FR (automatyczne wykrywanie i ręczna zmiana)
- Gwiazdka (*) na zakładce informująca o niezapisanych zmianach w pliku

## Technologie

- Java SE — język i środowisko uruchomieniowe
- Swing (`javax.swing`) — biblioteka GUI
- NIO (`java.nio.file`) — operacje na plikach
- Preferences (`java.util.prefs.Preferences`) - trwałe zapisywanie ustawień użytkownika
- ResourceBundle (`java.util.ResourceBundle`) — obsługa internacjonalizacji (i18n)
- [FlatLaf 3.7.1](https://github.com/JFormDesigner/FlatLaf) — nowoczesne motywy UI
- [jSystemThemeDetector 3.8](https://github.com/Dansoftowner/jSystemThemeDetector) — wykrywanie motywu systemowego
- [FontChooser 3.1.0](https://github.com/dheid/fontchooser) — okno wyboru czcionki
- [RSyntaxTextArea 3.4.1](https://github.com/bobbylight/RSyntaxTextArea) — podświetlanie składni i edytor kodu
- [RSTALanguageSupport 3.3.1](https://github.com/bobbylight/RSTALanguageSupport) — autouzupełnianie dla języków programowania

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
├── Tab.java                   # Zakładka
├── SwingNotesMenuBar.java     # Pasek menu
├── SwingNotesContextMenu.java # Menu kontekstowe
├── I18n.java                  # Zarządzanie tłumaczeniami i lokalizacją
└── FindReplaceDialog.java     # Okno znajdź i zamień
```

## Kontekst

Projekt edukacyjny stworzony w ramach nauki programowania w technikum. Rozbudowana wersja ćwiczenia z zajęć — dodane m.in. rzeczywisty I/O plików, podświetlanie składni, obsługa preferencji, motywy UI, drag-and-drop, ostatnio otwierane pliki, drukowanie i wiele innych funkcji.

## Planowane funkcje (v1.x)
- Przywracanie otwartych plików oraz pozycji kursora po ponownym uruchomieniu aplikacji.
- Funkcja Auto-save (konfigurowalny interwał czasowy lub zapis przy utracie fokusu).
- Szukanie frazy we wszystkich otwartych dokumentach jednocześnie.
- Mini-mapa kodu oraz statystyki czasu pracy nad danym plikiem.
- Możliwość zmiany kolejności kart metodą drag-and-drop.
