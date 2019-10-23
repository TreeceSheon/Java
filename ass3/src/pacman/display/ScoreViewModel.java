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
        currentOrderedBy = new SimpleStringProperty("");
        this.scoreModel = model.getScores();
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
        scoreModel.setScore(player, score);
    }

    public StringProperty getSortedBy() {
        return currentOrderedBy;
    }

    public ObservableList<String> getScores() {
        return scores;
   }

    public void switchScoreOrder() {
        if (currentOrderedBy.getValue().equals("Sorted By Name")) {
            currentOrderedBy.setValue("Sorted By Score");
            scores.clear();
            scores.addAll(scoreModel.getEntriesByScore());
        }
        else {
            currentOrderedBy.setValue("Sorted By Name");
            scores.clear();
            scores.addAll(scoreModel.getEntriesByName());
        }
    }

    public void update() {
        currentScore.setValue("Score: " + scoreModel.getScore());
        if (scores.equals(scoreModel.getEntriesByName())) {
            currentOrderedBy.setValue("Sorted By Name");
        } else {
            currentOrderedBy.setValue("Sorted By Score");
        }
        if (currentOrderedBy.getValue().equals("Sorted By Name")) {
            scores.sort(new ComName());
        } else if (currentOrderedBy.getValue().equals("Sorted By Score")) {
            scores.sort(new comScore());
        }
    }

    private static class ComName implements Comparator<String> {
        @Override
        public int compare(String s, String t1) {
            return s.split(":")[0].compareTo(t1.split(":")[0]);
        }
    }
    private static class comScore implements Comparator<String> {
        public int compare(String s, String t1) {
            return (Integer.parseInt(((t1.split(":")[1])).strip()) - (Integer.parseInt(((s.split(":")[1])).strip())));
        }
    }
}
