package pacman.display;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import pacman.game.PacmanGame;
import pacman.score.ScoreBoard;

import java.util.*;

public class ScoreViewModel {
    private ScoreBoard scoreModel;
    private ObservableList<String> scores =  FXCollections.observableArrayList();
    private StringProperty currentScore;
    private StringProperty currentOrderedBy;

    public ScoreViewModel(PacmanGame model) {
        currentOrderedBy = new SimpleStringProperty("Sorted By Name");
        scoreModel = model.getScores();
        currentScore = new SimpleStringProperty("Score:"+scoreModel.getScore());
        List<String> temp = scoreModel.getEntriesByName();
        scores.addAll(temp);

    }
    public int getCurrentScore() {
        return scoreModel.getScore();
    }

    public StringProperty getCurrentScoreProperty() {
        return currentScore;
    }

    public void setPlayerScore(String player, int score){
        scores.add(player +" : " + score);
        scoreModel.setScore(player,score);
    }

    public StringProperty getSortedBy() {
        return currentOrderedBy;
    }

    public ObservableList<String> getScores() {
        return scores;
   }

    public void switchScoreOrder() {
        if (currentOrderedBy.getValue().equals("Sorted By Name")) {
            scores.clear();
            scores.addAll(scoreModel.getEntriesByScore());
        }
        else {
            scores.clear();
            scores.addAll(scoreModel.getEntriesByName());
        }
    }

    public void update() {
        currentScore.setValue("Score:" + scoreModel.getScore());
        if (scores.equals(scoreModel.getEntriesByName())) {
            currentOrderedBy.setValue("Sorted By Name");
        } else {
            currentOrderedBy.setValue("Sorted By Score");
        }
        if (currentOrderedBy.getValue().equals("Sorted By Name")) {
//            scores.setAll(FXCollections.observableArrayList(scoreModel.getEntriesByName()));
            scores.sort(Comparator.comparing(o -> o.split(":")[0]));
        } else if (currentOrderedBy.getValue().equals("Sorted By Score")) {
            scores.sort((s, t1) -> (Integer.parseInt((
                    (t1.split(":")[1])).strip()) - (Integer.parseInt(((s.split(":")[1])).strip()))));
//            scores.setAll(scoreModel.getEntriesByScore());
        }
    }

}
