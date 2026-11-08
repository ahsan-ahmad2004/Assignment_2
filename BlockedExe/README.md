# BLOCKED.exe - The Block Burst Challenge
Java Swing / Java 2D puzzle game inspired by Block Blast (SE3005, Assignment 02).

## Install & run
Requires a JDK 11 or newer (tested on 21).

    # Linux / macOS
    ./run.sh
    # Windows
    run.bat
    # or manually
    mkdir out && javac -d out src/*.java && java -cp out Main

## Controls
| Action | Mouse | Keyboard |
|---|---|---|
| Pick piece | press on a tray piece | `1` `2` `3` |
| Move preview | drag | arrow keys |
| Place | release over the board | `Enter` / `Space` |
| Pause / Resume | PAUSE button | `P` |
| Restart | menu buttons | `R` |
| Mute | SND button | `M` |

Invalid drops/placements never consume the piece.

## Features
- 8x8 board, 20 shapes (single, lines, 2x2, rectangles, L, T, S + rotations), tray of 3
- Live placement preview (green/tinted = valid, red = invalid) and highlight of lines about to clear
- Rows and columns clear together; an intersection cell is cleared once
- Scoring with multi-line bonus and combo streaks; best score saved to `data/best.txt`
- Classic mode (elapsed time) and Time Attack (`Game.TIME_ATTACK_SECONDS = 180`, `Game.TARGET_SCORE = 500`)
- Screens: main menu, instructions, gameplay, pause, game over (all mouse-clickable)
- Animations: placement pop, clear flash + particles, score / combo popups, bobbing menu pieces
- Six WAV sound effects through `javax.sound.sampled.Clip` (auto-generated into `assets/sounds/` if missing; game runs silently if audio is unavailable)
- Errors logged to `logs/game.log`; window scales to any size

## Architecture
| Class | Role |
|---|---|
| `ShapeType` (enum), `BlockPiece`, `PieceGenerator` | pieces |
| `Board` | grid, collision, line detection/clearing |
| `Game`, `GameMode`, `GameState`, `Outcome` | rules, timers, state (no drawing) |
| `ScoreManager`, `AppLog`, `SoundManager` | persistence, logging, audio |
| `Drawable` -> `Effect` -> `PlacementPopEffect`, `ClearFlashEffect`, `ParticleEffect`, `ScorePopup` | polymorphic animations |
| `Renderer`, `Layout`, `Button` | all Graphics2D drawing / geometry |
| `InputHandler`, `InputState` | mouse + keyboard |
| `GamePanel` (Swing Timer loop), `GameFrame`, `Main` | window and wiring |




