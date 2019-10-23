package pacman.display;

import javafx.beans.property.SimpleStringProperty;
import org.junit.Test;
import pacman.board.PacmanBoard;
import pacman.game.GameReader;
import pacman.game.PacmanGame;
import pacman.hunter.Hunter;
import pacman.hunter.Speedy;
import pacman.util.UnpackableException;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import static org.junit.Assert.*;

public class ScoreViewModelTest {
    private Hunter testHunter = new Speedy();
    private PacmanBoard board = new PacmanBoard(10,10);
    private PacmanGame gamePrototype = new PacmanGame("Map for testing","Treece",testHunter,board);
    private PacmanGame gameFromArchive = GameReader.read(new FileReader("maps/default.map"));
    private ScoreViewModel scoreModelArchived = new ScoreViewModel(gameFromArchive);
    private ScoreViewModel scoreModelPrototype = new ScoreViewModel(gamePrototype);

    public ScoreViewModelTest() throws UnpackableException, IOException {
    }

    @Test
    public void getCurrentScore() {
        assertEquals(123,scoreModelArchived.getCurrentScore());
        assertEquals(0,scoreModelPrototype.getCurrentScore());
    }

    @Test
    public void getCurrentScoreProperty() {
        assertEquals("Score:123",scoreModelArchived.getCurrentScoreProperty().getValue());
        assertEquals("Score:0",scoreModelPrototype.getCurrentScoreProperty().getValue());
    }

    @Test
    public void setPlayerScore() {
        scoreModelArchived.setPlayerScore("Treece",101);
        assertEquals("Treece : 101",scoreModelArchived.getScores().get(3));
    }

    @Test
    public void getSortedBy() {
        assertEquals("Sorted By Name",scoreModelArchived.getSortedBy().getValue());
        scoreModelArchived.switchScoreOrder();
        assertEquals("Sorted By Score",scoreModelArchived.getSortedBy().getValue());
    }

    @Test
    public void getScores() {
        var tempScores = new String[]{"A : 0", "B : 5", "C : 100"};
        var tempList = new ArrayList<>(Arrays.asList(tempScores));
        assertEquals(tempList,scoreModelArchived.getScores());
        scoreModelArchived.setPlayerScore("Treece",101);
        tempList.add("Treece : 101");
        assertEquals(tempList,scoreModelArchived.getScores());
    }

    @Test
    public void switchScoreOrder() {
        var tempScoresByName = new String[]{"A : 0", "B : 5", "C : 100"};
        var tempScoresByScore = Arrays.copyOf(tempScoresByName,3);
        Arrays.sort(tempScoresByScore, (s, t1) -> Integer.parseInt(t1.split(":")[1].strip()) - Integer.parseInt(s.split(":")[1].strip()));
        var tempListByName = new ArrayList<>(Arrays.asList(tempScoresByName));
        var tempListByScore = new ArrayList<>(Arrays.asList(tempScoresByScore));
        scoreModelArchived.switchScoreOrder();
        assertEquals("Sorted By Score",scoreModelArchived.getSortedBy().getValue());
        assertEquals(tempListByScore,scoreModelArchived.getScores());
        scoreModelArchived.switchScoreOrder();
        assertEquals(tempListByName,scoreModelArchived.getScores());
    }

    @Test
    public void update() {
        assertEquals("Score:123",scoreModelArchived.getCurrentScoreProperty().getValue());
        assertEquals("Sorted By Name",scoreModelArchived.getSortedBy().getValue());
        gameFromArchive.getScores().increaseScore(10);
        scoreModelArchived.switchScoreOrder();
        scoreModelArchived.update();
        assertEquals("Score:133",scoreModelArchived.getCurrentScoreProperty().getValue());
        assertEquals("Sorted By Score",scoreModelArchived.getSortedBy().getValue());
    }
}