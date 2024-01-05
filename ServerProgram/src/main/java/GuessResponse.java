import java.io.Serializable;

public class GuessResponse implements Serializable {
    private static final long serialVersionUID = 1;
    public String displayString;
    public int remainingGuesses;
    public boolean correctGuess, roundWon, roundLost, gameWon, gameLost;

    public GuessResponse(){
        this.displayString = null;
        this.remainingGuesses = 0;
        this.roundWon = this.roundLost = this.gameWon = this.gameLost = false;
    }

    public  GuessResponse(String displayString, int remainingGuesses, boolean correctGuess, boolean roundWon,
                          boolean roundLost, boolean gameWon, boolean gameLost){
        this.displayString = displayString;
        this.remainingGuesses = remainingGuesses;
        this.correctGuess = correctGuess;
        this.roundWon = roundWon;
        this.roundLost = roundLost;
        this.gameWon = gameWon;
        this.gameLost = gameLost;
    }
}
