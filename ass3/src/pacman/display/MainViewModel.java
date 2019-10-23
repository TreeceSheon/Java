package pacman.display;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import pacman.game.GameWriter;
import pacman.game.PacmanGame;
import pacman.hunter.Hunter;
import pacman.util.Direction;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

public class MainViewModel {

    private ScoreViewModel scoreModel;
    private BoardViewModel boardModel;
    private PacmanGame gameModel;
    private BooleanProperty pause = new SimpleBooleanProperty();
    private String address;
    private BooleanProperty gameOver = new SimpleBooleanProperty(false);
    private int tick = 0;
    private StringProperty title;

    public MainViewModel(PacmanGame model, String saveFileName) {
        pause.set(true);
        scoreModel = new ScoreViewModel(model);
        boardModel = new BoardViewModel(model);
        gameModel = model;
        address = saveFileName;
        title = new SimpleStringProperty("");
    }

    public void accept(String input) {
        switch (input.toLowerCase()) {
            case "p":
                pause.setValue(!pause.getValue());
                break;
            case "r":
                gameModel.reset();
                break;
            case "a":
                if (!pause.getValue()) {
                    gameModel.getHunter().setDirection(Direction.LEFT);
                }
                break;
            case "d":
                if (!pause.getValue()) {
                    gameModel.getHunter().setDirection(Direction.RIGHT);
                }
                break;
            case "w":
                if (!pause.getValue()) {
                    gameModel.getHunter().setDirection(Direction.UP);
                }
                break;
            case "s":
                if (!pause.getValue()) {
                    gameModel.getHunter().setDirection(Direction.DOWN);
                }
                break;
            case "o":
                if (!pause.getValue()) {
                    Hunter currentHunter = gameModel.getHunter();
                    currentHunter.activateSpecial(Hunter.SPECIAL_DURATION);
                }
                break;
        }
    }

    public BoardViewModel getBoardVM() {
        return boardModel;
    }

    public ScoreViewModel getScoreVM() {
        return scoreModel;
    }

    public StringProperty getTitle() {
        return title;
    }

    public BooleanProperty isGameOver() {
        return gameOver;
    }

    public BooleanProperty isPaused() {
        return pause;
    }

    public void save() {
        try {
            Writer writer = new FileWriter(new File(address));
            GameWriter.write(writer, gameModel);
            writer.close();
        } catch (IOException ignored) {
        }
    }

    public void tick() {
        if (pause.getValue()) {
            checkGameOver();
            return;
        }
        int[] delays = {50, 50, 40, 40, 30, 30, 20, 20, 20};
        var level = boardModel.getLevel();
        int delay;
        if (level < 9) {
            delay = delays[level];
        } else {
            delay = 10;
        }
        if ((tick % delay) == 0) {
            gameModel.tick();
        }
        tick++;
        checkGameOver();
    }

   public void update() {
        var tempTitle = gameModel.getTitle() + " by " + gameModel.getAuthor();
        title.setValue(tempTitle);
        scoreModel.update();
   }
    private void checkGameOver() {
        gameOver.setValue(boardModel.getLives() == 0);
    }
}