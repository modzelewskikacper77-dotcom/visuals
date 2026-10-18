BUDOWANIE
1. Zainstaluj JDK 21 i Gradle 8.12 (https://gradle.org/install/).
2. W folderze projektu:  gradle build
3. Gotowy plik: build/libs/visuals-1.0.0.jar  (NIE *-sources.jar)
4. Wrzuc do .minecraft/mods razem z Fabric API 0.119.2+1.21.4 i Fabric Loader >= 0.16.10.

STEROWANIE
- Klawisz O = menu (zmiana w Opcje > Sterowanie > Visuals)
- LPM na karcie: wlacz/wylacz lub rozwin; PPM na liscie trybow = poprzedni tryb
- Ustawienia zapisuja sie w .minecraft/config/visuals.properties

BUDOWANIE BEZ INSTALOWANIA CZEGOKOLWIEK (GitHub Actions)
1. Zaloz darmowe konto na github.com, utworz nowe repozytorium i wrzuc do niego zawartosc tego folderu
   (razem z folderem .github).
2. Wejdz w zakladke Actions -> workflow "build" -> poczekaj ok. 3-5 min.
3. W zakonczonym przebiegu pobierz artefakt "visuals-jar" - w srodku jest visuals-1.0.0.jar.
