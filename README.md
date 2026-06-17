# Chess

A console chess **move validator / replayer**. It reads a list of moves from a text file,
replays them on a board one move at a time, checks each against the rules of chess, prints
the board after every move, and reports check / checkmate / stalemate.

It is not a chess *engine* in the AI sense — it never picks a move. Its job is to decide,
for each move in the file, "is this legal?", apply it if so, and recognise when the game
has ended.

> Want to understand how it works inside? See **[ARCHITECTURE.md](ARCHITECTURE.md)** for a
> full walkthrough of the design and the decisions behind it.

---

## Requirements

* **JDK 21** (the build pins this via a Gradle toolchain and can auto-provision it)
* **Gradle** (or use the bundled `./gradlew` wrapper)

## Build and run

```bash
# Run a game from a move file
gradle run --args="resources/sample-moves.txt"

# Run the test suite
gradle test
```

---

## How to play

The game is driven by a **move file**: one move per line, in coordinate notation —
the square you move *from* immediately followed by the square you move *to*:

```
e2e4      # white pawn e2 → e4
e7e5      # black pawn e7 → e5
f1c4      # white bishop f1 → c4
b8c6      # black knight b8 → c6
```

* Files are letters `a`–`h`, ranks are numbers `1`–`8` (standard chessboard coordinates).
* White moves first, then turns alternate automatically.
* To play your own game, create a text file in this format and pass it to
  `gradle run --args="<path-to-your-file>"`.

After each move the program prints the board (uppercase = White, lowercase = Black, `N` for
knight), and announces **CHECK**, **CHECKMATE**, or **STALEMATE** when they occur. If a line
in the file is an illegal move, the program reports it and stops.

```
    A  B  C  D  E  F  G  H
 |--------------------------|
8| [r][n][b][q][k][b][n][r] |8
7| [p][p][p][p][p][p][p][p] |7
6| [ ][ ][ ][ ][ ][ ][ ][ ] |6
5| [ ][ ][ ][ ][ ][ ][ ][ ] |5
4| [ ][ ][ ][ ][P][ ][ ][ ] |4
3| [ ][ ][ ][ ][ ][ ][ ][ ] |3
2| [P][P][P][P][ ][P][P][P] |2
1| [R][N][B][Q][K][B][N][R] |1
 |--------------------------|
    A  B  C  D  E  F  G  H
```

### Example move files

Sample games live in the `resources/` folder:

| File | What it shows |
|------|---------------|
| `sample-moves.txt`         | A short, fully legal sequence that just plays out. |
| `sample-moves-invalid.txt` | Contains an illegal move (`b1b3`, not a knight move) — reported, then it stops. |
| `checkmate.txt`            | Scholar's mate (`1.e4 e5 2.Bc4 Nc6 3.Qf3 d6 4.Qxf7#`) — ends in checkmate. |

---

## Rules supported

* Full movement and captures for every piece, with complete **legal-move validation** —
  you cannot make a move that leaves your own king in check (pinned pieces included).
* **Check**, **checkmate**, and **stalemate** detection; the game ends and reports the result.
* **Pawn promotion** (to a queen — the input format has no field to request another piece).
* Clear reporting of the first illegal move.

**Not implemented:** castling, en passant, underpromotion, and the draw rules (fifty-move,
threefold repetition, insufficient material). See
[ARCHITECTURE.md](ARCHITECTURE.md#rules-supported-and-not) for why.
