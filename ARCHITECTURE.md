# Architecture

A full walkthrough of how the chess move validator is built and *why*, so the design can be
picked up again from cold. For what the program does and how to run it, see
**[README.md](README.md)**.

---

## Table of contents

1. [The problem and the input format](#the-problem-and-the-input-format)
2. [Architecture at a glance](#architecture-at-a-glance)
3. [The coordinate system (read this first)](#the-coordinate-system-read-this-first)
4. [The domain model: enums as the type system](#the-domain-model-enums-as-the-type-system)
5. [The board](#the-board)
6. [Move generation (the Strategy pattern)](#move-generation-the-strategy-pattern)
7. [Moves as commands: execute / undo](#moves-as-commands-execute--undo)
8. [From pseudo-legal to legal: make/unmake and pins](#from-pseudo-legal-to-legal-makeunmake-and-pins)
9. [Check, checkmate and stalemate](#check-checkmate-and-stalemate)
10. [The game loop](#the-game-loop)
11. [Output is decoupled from the engine](#output-is-decoupled-from-the-engine)
12. [Error handling](#error-handling)
13. [Rules supported (and not)](#rules-supported-and-not)
14. [Testing strategy](#testing-strategy)
15. [Build, toolchain and CI](#build-toolchain-and-ci)
16. [Design decisions and trade-offs](#design-decisions-and-trade-offs)
17. [Where to look (file map)](#where-to-look-file-map)
18. [Possible extensions](#possible-extensions)

---

## The problem and the input format

A move file has one move per line in coordinate notation — source square followed by
destination square (`e2e4`, `e7e5`, …).

Reading the file is handled by a **provided library**, `lib/userinput.jar`
(`com.whitehatgaming.UserInputFile`). Its contract is tiny:

```java
public interface UserInput {
    int[] nextMove() throws IOException;   // returns null at end of file
}
```

Each call returns a 4-element array `[srcCol, srcRow, dstCol, dstRow]`, already converted
from algebraic notation to 0–7 indices, or `null` when the file is exhausted. We do not
control this jar; the program is built around its shape.

> **Note on promotion:** because the format carries only source + destination (no piece
> letter), a pawn reaching the last rank always promotes to a **queen**. There is nowhere
> in the input to request a knight/rook/bishop underpromotion.

---

## Architecture at a glance

The code is organised into packages by responsibility:

```
com.whitehatgaming
├── GameManager              ← entry point: parse file → build moves → run game
│
├── game/                    ← the board, the rules engine, the game loop, output
│   ├── Board, Square            board representation
│   ├── BoardInitializer         sets up the starting position
│   ├── MoveCommand              one requested move (source → destination)
│   ├── Game                     the game loop / orchestration
│   ├── GameState, BoardState    whose turn it is + STANDARD/CHECK/CHECKMATE/STALEMATE
│   ├── BoardEvaluator           legal moves, check detection, position classification
│   ├── GameResult               the outcome of a finished game
│   ├── GameListener             output port (engine emits events to it)
│   ├── ConsoleGameListener      console implementation of that port
│   └── ConsoleBoardDisplayer    renders a board to ASCII
│
├── pieces/                  ← the type system
│   ├── Piece                    WHITE_KING, BLACK_PAWN, … (enum)
│   ├── PieceType                KING, QUEEN, … each owns its move generator (enum)
│   └── Color                    WHITE / BLACK, encodes direction + key rows (enum)
│
├── generators/              ← how each piece type moves (Strategy pattern)
│   ├── MoveGenerator            interface
│   ├── MoveGeneratorHelper       shared step/slide helpers
│   └── {Pawn,Knight,Bishop,Rook,Queen,King}MoveGenerator
│
├── moves/                   ← a move as an executable, reversible command
│   ├── Move                     interface: execute() / undo()
│   ├── StandardMove             a normal move/capture
│   └── PromotionMove            a pawn reaching the last rank
│
└── exceptions/
    └── InvalidMovementException
```

The high-level data flow for one run:

```
 file
   │  UserInputFile.nextMove() → int[]
   ▼
GameManager ── builds ──▶ List<MoveCommand>
   │
   ▼
 Game.start(moves)
   │   for each command:
   │     1. legalMoves(player)         ← BoardEvaluator
   │     2. find the move matching src→dst   (none ⇒ InvalidMovementException)
   │     3. move.execute()             ← mutates the Board
   │     4. state.notifyMove(...)      ← flips side, re-classifies position
   │     5. listener.movePlayed(...)   ← console prints the board
   ▼
 GameResult  +  listener.gameEnded(...)
```

The three big ideas, each explained below, are:

* **Strategy** — each piece type knows how to generate its own candidate moves.
* **Command + make/unmake** — a move is an object you can `execute()` and `undo()`, which
  is what makes legality checking simple and correct.
* **Observer** — the engine never prints; it emits events to a listener.

---

## The coordinate system (read this first)

Almost every confusing bug in board code comes from coordinates, so pin this down before
reading anything else.

A square is a `(row, col)` pair, each `0–7`. The board is stored "screen-style", with
**row 0 at the top**:

```
            col: 0   1   2   3   4   5   6   7
            file: a   b   c   d   e   f   g   h
          ┌────────────────────────────────────┐
 row 0 r8 │  r   n   b   q   k   b   n   r       │  ← Black's back rank
 row 1 r7 │  p   p   p   p   p   p   p   p       │
 row 2 r6 │                                     │
 row 3 r5 │                                     │
 row 4 r4 │                                     │
 row 5 r3 │                                     │
 row 6 r2 │  P   P   P   P   P   P   P   P       │
 row 7 r1 │  R   N   B   Q   K   B   N   R       │  ← White's back rank
          └────────────────────────────────────┘
```

Consequences that the whole codebase relies on:

* **Rank ↔ row:** `rank = 8 - row`. **File ↔ col:** `file = 'a' + col`.
* **White starts at the bottom** (rows 6–7) and moves *up* the screen toward row 0, so
  White's "forward" direction is **−1**. Black starts at the top and moves *down*, so
  Black's direction is **+1**.

`Color` bakes these facts in so no other class has to special-case white vs black:

```java
//        direction, pawn start row, promotion row
WHITE(-1, 6, 0)   // moves toward row 0; pawns start on row 6; promote on row 0
BLACK( 1, 1, 7)   // moves toward row 7; pawns start on row 1; promote on row 7
```

The input jar hands moves back as `[col, row, col, row]`, which is why `GameManager`
builds squares as `new Square(moveIn[1] /*row*/, moveIn[0] /*col*/)`.

---

## The domain model: enums as the type system

Three enums carry the chess vocabulary. Using enums (rather than classes + subclassing)
keeps the model closed, allocation-free, and trivially comparable with `==`.

* **`Color`** — `WHITE` / `BLACK`, plus the direction/row data above and `opponent()`.
* **`PieceType`** — `KING, QUEEN, BISHOP, KNIGHT, ROOK, PAWN`. Crucially, **each type holds
  its own move generator**:

  ```java
  KING(new KingMoveGenerator()),
  QUEEN(new QueenMoveGenerator()),
  ...
  ```

  This is the wiring for the Strategy pattern: "how does a king move?" is answered by the
  `KING` constant, not by an `if/switch` somewhere.

* **`Piece`** — the twelve concrete pieces (`WHITE_KING` … `BLACK_PAWN`), each a
  `(PieceType, Color)` pair. A piece delegates move generation to its type:

  ```java
  public List<Move> availableMoves(Square position, Board board) {
      return type.generator.generateMoves(position, color, board);
  }
  ```

  `Piece.of(type, color)` looks a piece up from a `(type, color)` pair — used by promotion
  to turn a pawn into the right-coloured queen.

---

## The board

`Board` stores the position as a **sparse map** of occupied squares:

```java
private final Map<Square, Piece> positions;   // only occupied squares are present
```

`Square` is an immutable `record(row, col)`. Two design points:

* **Factory vs constructor.** The raw constructor `new Square(r, c)` allows *off-board*
  coordinates, because move generation routinely computes "two squares up-left" before
  knowing if that is on the board. The factory methods (`Square.at`, `Square.atOffset`,
  `right/left/forward`) return **`null`** when a coordinate would leave the board. So the
  rule across the code is: *a `null` square means "off the board".* `Board.isFree(null)`
  and `Board.isColor(null, …)` both return `false`, so callers don't need null checks
  everywhere.
* **Why a `Map`, not an 8×8 array?** A map makes "iterate over every piece of a colour"
  (the core of move generation and check detection) clean — you only visit occupied
  squares. The trade-off is per-access hashing instead of array indexing, which is
  irrelevant at this scale (replaying a file). An 8×8 array would be the choice if this
  were performance-critical.

`BoardInitializer` places the standard starting position. `Board.SIZE = 8` is the single
source of truth for the board dimension, used by `Square.isValid`, the initializer, and
the displayer.

---

## Move generation (the Strategy pattern)

Every piece type implements one interface:

```java
public interface MoveGenerator {
    List<Move> generateMoves(Square square, Color color, Board board);
}
```

`MoveGeneratorHelper` provides the two primitives every generator is built from:

* **`stepIfEmptyOrOpponent(src, dst, …)`** — a *single* step. Returns one move if `dst` is
  empty or holds an opponent (a capture), otherwise nothing. Used by the **king** and
  **knight**, which jump to fixed offsets.
* **`slideWhileEmptyOrOpponent(src, rowΔ, colΔ, …)`** — *slides* along a direction,
  emitting a move for each empty square and one capture when it meets an opponent, then
  **stops**. Used by the **rook**, **bishop** and **queen**.

  > The "then stops" (`break` after a capture) matters: a sliding piece must not jump
  > *over* the first piece it hits. An earlier version of this helper kept sliding past a
  > capture, which generated illegal moves and could miscount attacks; the regression test
  > `slidingPieceCannotJumpOverACapture` locks the fix in.

The generators read almost like their dictionary definitions, e.g. the rook:

```java
moves.addAll(slideWhileEmptyOrOpponent(square,  1,  0, color, board)); // down
moves.addAll(slideWhileEmptyOrOpponent(square, -1,  0, color, board)); // up
moves.addAll(slideWhileEmptyOrOpponent(square,  0,  1, color, board)); // right
moves.addAll(slideWhileEmptyOrOpponent(square,  0, -1, color, board)); // left
```

The **pawn** is the special one (`PawnMoveGenerator`): one step forward if empty; two steps
from its starting row; diagonal captures only when an opponent is actually there; and if a
move lands on the `promotionRow`, it emits a `PromotionMove` instead of a `StandardMove`
(for both the forward push and a capture).

These generators produce **pseudo-legal** moves: geometrically valid, but they ignore
whether the move leaves your own king in check. Turning pseudo-legal into legal is the
next section.

---

## Moves as commands: execute / undo

A `Move` is a **command object** — something you can apply to the board and take back:

```java
public interface Move {
    void execute();
    void undo();          // restores the board to exactly before execute()
    Piece getCapturedPiece();
    Square getSource();
    Square getDst();
    default boolean matches(Square src, Square dst) { … }   // "is this the src→dst move?"
}
```

`StandardMove` records what it needs to reverse itself:

```java
public void execute() {
    movedPiece    = board.at(srcSquare);   // remember the mover
    capturedPiece = board.at(dstSquare);   // remember anything captured (may be null)
    board.removePieceAt(srcSquare);
    board.setPieceAt(dstSquare, placedPiece());
}

public void undo() {
    board.removePieceAt(dstSquare);
    board.setPieceAt(srcSquare, movedPiece);
    if (capturedPiece != null) board.setPieceAt(dstSquare, capturedPiece);
}
```

The `placedPiece()` hook is what makes promotion a one-class change:
`PromotionMove extends StandardMove` and overrides `placedPiece()` to return a queen of the
mover's colour. Because `undo()` restores `movedPiece` (the original *pawn*), promotion
reverses correctly too — important, because legality testing tries moves out and rolls them
back.

`matches(src, dst)` is deliberately **not** called `equals` — it's a domain question ("does
this move go from here to there?"), not object identity, and overloading `Object.equals`
would have been a trap.

---

## From pseudo-legal to legal: make/unmake and pins

This is the heart of the rules engine, in `BoardEvaluator.legalMoves`:

```java
for (Move move : semiLegalMoves(color, board)) {
    move.execute();
    if (!isCheck(color, board)) {   // did I leave / put my own king in check?
        legalMoves.add(move);
    }
    move.undo();
}
```

The technique is **make / test / unmake**. For every candidate move we actually play it on
the real board, ask "is my king in check now?", and undo it. A move is legal iff it does
*not* leave your own king attacked.

This one loop correctly handles things that are otherwise fiddly:

* **Pins** — a piece shielding its king can't move off the line, because moving it would
  expose the king, so `isCheck` returns true and the move is filtered out. (Test:
  `pinnedPieceHasNoLegalMove`.)
* **Escaping check** — when you're in check, only moves that result in a not-in-check
  position survive, which is exactly "get out of check".
* **Kings can't move adjacent / into attack** — after the king moves, the enemy king's (or
  any piece's) generator covers that square, so `isCheck` rejects it.

Supporting pieces:

* **`semiLegalMoves(color)`** — gathers the pseudo-legal moves of every piece of `color`.
* **`isThreatenedBy(color, square)`** — true if any of `color`'s pseudo-legal moves target
  `square`.
* **`isCheck(color)`** — `isThreatenedBy(color.opponent(), kingSquare)`.
* **`findKing(color)`** — locates the king (throws if it is missing, which only happens for
  a malformed position; see [Error handling](#error-handling)).

> **Why make/unmake instead of attack maps?** A "real" engine would precompute attack
> bitboards for speed. Here, clarity and obvious-correctness win: replaying a file does at
> most a few hundred move generations, so the simple approach is the right approach. Each
> ply generates moves roughly twice (once to validate the chosen move, once to classify the
> resulting position) — negligible at this scale.

---

## Check, checkmate and stalemate

All four position states are the two-by-two combination of two booleans — *am I in check?*
and *do I have any legal move?* — computed in one place, `BoardEvaluator.evaluate`:

| In check? | Has a legal move? | State |
|:---------:|:-----------------:|-------|
| no  | yes | `STANDARD`  |
| yes | yes | `CHECK`     |
| yes | no  | `CHECKMATE` |
| no  | no  | `STALEMATE` |

```java
public static BoardState evaluate(Color color, Board board) {
    boolean inCheck = isCheck(color, board);
    boolean canMove = !legalMoves(color, board).isEmpty();
    if (inCheck && !canMove) return BoardState.CHECKMATE;
    if (!inCheck && !canMove) return BoardState.STALEMATE;
    if (inCheck) return BoardState.CHECK;
    return BoardState.STANDARD;
}
```

`BoardState.isGameOver()` is true for `CHECKMATE`/`STALEMATE`, which is how the game loop
knows to stop.

`GameState` ties turn-taking to this classification. After a move, `notifyMove` flips the
side to move and re-classifies **for the new side**:

```java
public void notifyMove(Move move, Board board) {
    playerColor = playerColor.opponent();
    boardState  = BoardEvaluator.evaluate(playerColor, board);
}
```

So `CHECKMATE` means *the side now to move* is mated — and therefore the **winner is its
opponent** (the player who just moved). That is exactly how `Game.winner()` reports it.

---

## The game loop

`Game.start(List<MoveCommand>)` is the orchestrator. A `MoveCommand` is just a requested
`(source, destination)` pair parsed from the file — distinct from a `Move`, which is the
concrete, executable thing the engine resolves it to.

```java
for (MoveCommand command : moves) {
    if (state.getBoardState().isGameOver()) break;     // stop once the game is decided

    Color mover = state.getPlayerColor();
    Move move = findLegalMove(command);                // search legalMoves for src→dst
    if (move == null) {
        throw new InvalidMovementException(describeIllegal(command));
    }

    executeMove(move);                                 // execute + notify state
    movesPlayed++;
    listener.movePlayed(movesPlayed, mover, state.getBoardState(), board);
}

GameResult result = new GameResult(state.getBoardState(), winner(), movesPlayed);
listener.gameEnded(result);
return result;
```

Key behaviours:

* **Validation is "is it in the legal-move list?"** `findLegalMove` asks `BoardEvaluator`
  for the current player's legal moves and looks for one matching the requested
  `src → dst`. This means turn order, "that's not your piece", moving into check, illegal
  geometry, and pins are *all* enforced by the same mechanism — there is no separate web of
  validation rules to keep in sync.
* **An illegal move stops the game** by throwing `InvalidMovementException` (caught and
  printed by `GameManager`). For a move-file validator, refusing to continue past a bad
  move is the honest behaviour.
* **`start` returns a `GameResult`** (final state, winner, moves played), so the engine is
  usable as a library, not just a console program.

---

## Output is decoupled from the engine

The engine **never calls `System.out`**. Instead it emits events to a `GameListener`:

```java
public interface GameListener {
    GameListener NONE = new GameListener() {};        // no-op default
    default void gameStarted(Board board) {}
    default void movePlayed(int moveNumber, Color mover, BoardState state, Board board) {}
    default void gameEnded(GameResult result) {}
}
```

`ConsoleGameListener` is the only place that prints — it renders the headers, the
check/checkmate/stalemate banners, and delegates board drawing to `ConsoleBoardDisplayer`.
`GameManager` wires it in with `new Game(new ConsoleGameListener())`; tests use the default
no-op `Game()` and assert on the returned `GameResult` / `pieceAt(...)` instead.

This is the Observer pattern, and it is what makes the engine testable and reusable: a GUI,
a web front end, or a test harness just supplies a different listener. (It also resolved an
old `// TODO: less displaying in the engine` — display now lives entirely outside it.)

`ConsoleBoardDisplayer` prints **uppercase letters for White, lowercase for Black**, with
`N` for knight (since `K` is the king):

```
    A  B  C  D  E  F  G  H
 |--------------------------|
8| [r][n][b][q][k][b][n][r] |8
7| [p][p][p][p][p][p][p][p] |7
...
1| [R][N][B][Q][K][B][N][R] |1
 |--------------------------|
```

---

## Error handling

* **`InvalidMovementException`** (checked) is thrown for an illegal move or malformed input
  (a coordinate outside 0–7, or a zero-length move). It carries a human-readable message,
  e.g. `Invalid move: WHITE_KNIGHT b1 -> b3`. It does *not* print from its constructor (an
  earlier version did, which made `getMessage()` useless).
* **`GameManager`** is the boundary that turns failures into clean console lines: missing
  file, unreadable file, invalid movement, and a catch-all for unexpected
  `RuntimeException`s (e.g. `findKing` on a corrupt position, or a malformed move record) so
  the program reports a message instead of dumping a stack trace.

---

## Rules supported (and not)

**Supported**

* All piece movement, captures, and full **legal-move validation** (you cannot make a move
  that leaves your own king in check — including pinned pieces).
* **Check**, **checkmate**, and **stalemate** detection; the game stops and reports the
  result.
* **Pawn promotion** to a queen (the input format has no field to request another piece).
* Clear reporting of the first illegal move.

**Not implemented** (and why it would be non-trivial)

* **Castling** and **en passant** — both need state beyond the current board (has the
  king/rook moved? what was the previous move?), which the current `Board` + `GameState`
  don't track.
* **Underpromotion** (to knight/rook/bishop) — no way to express it in the input format.
* **Draw rules**: fifty-move, threefold repetition, insufficient material — would need move
  history / position hashing.

---

## Testing strategy

Tests run on JUnit 5 (`gradle test`). They are deliberately layered:

* **Generator-level** (`PawnGeneratorTest`, `KnightGeneratorTest`) — geometry of individual
  pieces from hand-built positions.
* **Evaluator-level** (`BoardEvaluatorTest`, `GameRulesTest`) — `legalMoves` count from the
  opening, pins, checkmate, stalemate, promotion (push and capture), and the
  slide-through-a-capture regression. These exercise the rules logic directly.
* **Engine-level** (`GameTest`) — drives whole games through `Game.start` using readable
  algebraic strings (`"e2e4"`):
  * Scholar's mate ends in `CHECKMATE`.
  * An illegal move throws `InvalidMovementException`.
  * **The move-drop regression**: two knights shuffle out-and-back so that a later move
    reuses an earlier source square. This pins the fix for an old bug where moves were
    stored in a `Map` keyed by source square and silently overwrote each other.

Each test builds its own position (`@BeforeEach`, fresh `Board`); there is no shared mutable
state between tests.

---

## Build, toolchain and CI

* **Gradle** with the `application` plugin; `mainClass` is `com.whitehatgaming.GameManager`.
* **Pinned toolchain** — the build targets **JDK 21** via a Gradle toolchain, so it compiles
  the same way regardless of the developer's default Java (Gradle can auto-provision the
  JDK). Bumping the version later is a one-line change.
* **JUnit 5** (`junit-jupiter` + the platform launcher on the test runtime classpath).
* **SpotBugs** static analysis runs via `gradle spotbugsMain` (report at
  `build/reports/spotbugs/`). It is configured with `ignoreFailures = true` because it was
  introduced to an existing codebase; today it reports a single, intentional finding
  (`StandardMove` holds a reference to the shared `Board`, which is by design — a move must
  act on the board).
* **CI** — `.github/workflows/ci.yml` runs `gradle test` and `gradle spotbugsMain` on every
  push and pull request.

---

## Design decisions and trade-offs

| Decision | Why | Trade-off / alternative |
|----------|-----|-------------------------|
| One `MoveGenerator` per piece type, wired through the `PieceType` enum | Adding or changing a piece's movement is local; no central `switch` | Slightly more classes |
| Moves are command objects with `execute()`/`undo()` | Enables make/unmake legality testing and clean promotion | A move must hold a `Board` reference |
| Legality via make/test/unmake | Obvious correctness; pins and check-escape "just work" | Slower than attack maps (irrelevant here) |
| Position state from two booleans in `BoardEvaluator.evaluate` | Checkmate vs stalemate vs check derived in one place, provably exhaustive | Re-runs `legalMoves` (fine at this scale) |
| `Board` as a sparse `Map<Square, Piece>` | Iterating "all pieces of a colour" is clean | Hashing per access vs array indexing |
| `Square` as an immutable `record`, off-board factories return `null` | Safe map key; "null == off board" convention removes scattered bounds checks | Callers must treat `null` as off-board |
| `Color` encodes direction + pawn/promotion rows | No white-vs-black branching in generators | A little "magic" in the enum constants |
| Output via `GameListener` (Observer) | Engine is silent, testable, reusable | One more interface |
| Illegal move throws and stops | Honest behaviour for a validator | Can't "skip and continue" past a bad move |

---

## Where to look (file map)

| If you want to understand… | Read |
|----------------------------|------|
| How the program starts and reads the file | `GameManager` |
| The board representation and coordinates | `game/Board`, `game/Square`, `pieces/Color` |
| How each piece moves | `generators/*MoveGenerator`, `generators/MoveGeneratorHelper` |
| What a move *is* and how it's reversed | `moves/Move`, `moves/StandardMove`, `moves/PromotionMove` |
| Legality, check, checkmate, stalemate | `game/BoardEvaluator`, `game/GameState`, `game/BoardState` |
| The game loop and result | `game/Game`, `game/MoveCommand`, `game/GameResult` |
| Output | `game/GameListener`, `game/ConsoleGameListener`, `game/ConsoleBoardDisplayer` |

---

## Possible extensions

In rough order of effort:

1. **Underpromotion / a richer input format** — let a move specify the promotion piece;
   `PromotionMove` already accepts a target `PieceType`.
2. **Draw rules** — track move history for fifty-move and threefold repetition.
3. **En passant / castling** — add the small amount of extra state they require
   (last move; king/rook "has moved" flags) and the corresponding `Move` subclasses.
4. **A different front end** — implement `GameListener` for a GUI or web view; the engine
   doesn't change.
