# TicTacToe Multiplayer Game

Willkommen zum **TicTacToe Multiplayer-Projekt**!  
Dieses Projekt ermöglicht es zwei Spielern, lokal oder über das Netzwerk TicTacToe zu spielen.

---
## Entwicklerinfos

- Entwickelt von: **Ayse Toy**
- Studiengang: **Objektorientiere Dienstentwicklung, 3. Semester**
- Hochschule: **Technikum Wien**
---

## Projektübersicht

- **Sprache:** Java 17+
- **Frameworks:** JavaFX (für Benutzeroberfläche)
- **Kommunikation:** Socket-Netzwerkverbindung (Client-Server)
- **Architektur:**
    - **Model:** Spiellogik (GameModel, GameState, Player)
    - **View:** JavaFX-UI (tictactoe.fxml)
    - **Controller:** Spielsteuerung und Netzwerk (GameController)
    - **Network:** Verbindungslogik (NetworkConnection, NetworkListener, NetworkCommand)
    - **Util:** Konfiguration laden (ConfigLoader)

---

## Projektstruktur

```
src/
 ├─ main/
 │   ├─ java/
 │   │   └─ com.example.tictactoe/
 │   │       ├─ controller/
 │   │       │    └─ GameController.java
 │   │       ├─ model/
 │   │       │    ├─ GameModel.java
 │   │       │    ├─ GameState.java
 │   │       │    └─ Player.java
 │   │       ├─ network/
 │   │       │    ├─ NetworkCommand.java
 │   │       │    ├─ NetworkConnection.java
 │   │       │    └─ NetworkListener.java
 │   │       ├─ util/
 │   │       │    └─ ConfigLoader.java
 │   │       ├─ Main.java
 │   │       └─ TicTacToeApp.java
 │   ├─ resources/
 │   │   ├─ com.example.tictactoe/
 │   │   │    └─ tictactoe.fxml
 │   │   └─ config.properties
```

---

## Ausführen des Spiels

### Voraussetzungen

- **Java 17** oder höher installiert
- **JavaFX SDK** eingebunden
- **Maven** oder manuelles Builden möglich

---

### Lokales Spiel starten

1. Starte die Anwendung `Main`
2. Das Fenster öffnet sich.
3. Spiele lokal abwechselnd als zwei Spieler auf demselben Gerät.

---

### Netzwerkspiel starten

1. **Konfiguration anpassen**:  
   Öffne `config.properties`:

   ```properties
   opponent.ip=127.0.0.1
   network.port=54321
   ```

    - Für Tests auf demselben PC bleibt `127.0.0.1`.
    - Für echtes Netzwerkspiel IP des anderen Rechners angeben.

2. **Spiel starten:**
    - Ein Spieler klickt auf **Host Game** (Server starten).
    - Der andere klickt auf **Join Game** (Client verbindet sich).

3. **Spielen!**

---

## Inhalt der config.properties

```properties
# IP-Adresse des Gegners
opponent.ip=127.0.0.1

# Portnummer für die Verbindung
network.port=54321
```

---

## Wichtige Hinweise

- Das Spiel prüft, ob ein Feld frei ist, bevor ein Zug gemacht wird.
- Gewinn oder Unentschieden werden automatisch erkannt.
- Das Netzwerkmodul meldet Verbindungsabbrüche und behandelt sie sauber.
- Popup-Dialoge informieren über Fehler oder Statusänderungen.

---

## Quellen

- [OpenJFX (JavaFX) Official Website](https://openjfx.io/)
- [Java SE 17 Documentation](https://docs.oracle.com/en/java/javase/17/)
- [Oracle Java Networking Tutorial (Sockets)](https://docs.oracle.com/javase/tutorial/networking/sockets/)
- [Code.Makery JavaFX Tutorial](https://code.makery.ch/library/javafx-tutorial/)
- Youtube
- Eigene Ausarbeitung basierend auf Vorlesungen (Technikum Wien, 2024)

---