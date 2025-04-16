# Craftmine

## How to Run

1. Build the project:
```bash
mvn clean package
```

2. Run the game:
```bash
java -XstartOnFirstThread -Djava.library.path=target/natives -jar target/craftmine-1.0-SNAPSHOT-jar-with-dependencies.jar
```

## Controls
- W: Move forward
- S: Move backward
- A: Strafe left
- D: Strafe right
- Space: Move up
- Left Shift: Move down
- Mouse: Look around
- ESC: Pause/Unpause
