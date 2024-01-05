import java.net.Socket;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;
import java.util.function.Consumer;

// Back end of client application
// Communicates with server on separate thread
public class ClientThread extends Thread {
    // networking data members
    private Socket connection;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private String ipAddress;
    private int portNum;

    // gameplay data members
    //private HashMap<String, String> categories; // name of category, num of attempts left
    private ArrayList<String> categoryTitles;
    private ArrayList<String> categoryAttemptsRemaining;
    private boolean gameWon;
    private boolean gameLost;
    private boolean roundWon;
    private boolean roundLost;
    protected String currentCategory;
    protected Integer currentCategoryNumber;
    protected Character currentGuess;
    protected boolean returnToCategories;
    protected boolean exit;
    private String wrongGuesses;
    private String currGuessState;
    private Consumer<Serializable> guiUpdates;

    // ----------------------------------------------------

    // everything that happens when the thread is running
    public void run() {

        System.out.println("Running");

        // connect to server
        boolean connectedSuccessfully = establishConnection();
        // if the thread did not connect, exit the run method and end the thread
        if (!connectedSuccessfully) {
            return;
        }

        currentCategory = "";
        currentCategoryNumber = 0;
        currentGuess = null;
        gameWon = false;
        gameLost = false;

        // receives category titles from the server
        receiveCategoryTitles();

        // updates the gui
        changeToCategoryScene("new");

        while (!gameWon && !gameLost) {
            returnToCategories = false;

            System.out.println("CT: Changed to category scene");

            // sends selected category to the server
            selectCategory();

            System.out.println("CT: Sent category to server");


            // receives current guess state and changes to guessing scene
            changeToGuessingScene();

            System.out.println("CT: Changed to guessing scene, current guessing state is: " + currGuessState);

            roundWon = false;
            roundLost = false;
            // while round is still going
            wrongGuesses = "";
            while (!roundWon && !roundLost) {

                // user sends guess, receives updated guess state
                sendGuess();

                System.out.println("CT: Sent guess to server");

                // receives updated game state, updates scene
                updateGuessingScene();

                System.out.println("CT: Updated guessing state is: " + currGuessState);

            }

            // guessing round has ended
            resolveGuessingRound();

            // infinite loop waiting for user to click button
            while (!returnToCategories) {
                try {
                    sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            if (!gameWon && !gameLost) {
                // if game has not ended, go back to category scene
                changeToCategoryScene("again");
            } else {
                // if game has ended, go to end scene
                goToEndScene();

                // infinite loop waiting for user to click button
                while (!exit) {
                    try {
                        sleep(100);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

        } // end main playing loop


        System.out.println("Game over");
    } // end run()


    // constructor - sets up the way to talk to the GUI
    public ClientThread(Consumer<Serializable> call, String ip, int port) {
        this.guiUpdates = call;
        this.ipAddress = ip;
        this.portNum = port;
    } // end constructor


    // takes in the address and port # and connects the client with the server
    public boolean establishConnection() {
        // for same machine, use ip address "127.0.0.1"
        try {
            connection = new Socket(ipAddress,portNum);
            in = new ObjectInputStream(connection.getInputStream());
            out = new ObjectOutputStream(connection.getOutputStream());
            connection.setTcpNoDelay(true);
        }
        catch(Exception e) {
            System.out.println("Issue connecting to the server. Try a different address.");
            return false;
        }

        System.out.println("Successfully connected to the server");
        return true;

    } // end establishConnection()


    // receives category titles
    public void receiveCategoryTitles() {
        // receive categories from server
        ArrayList<String> categoryTitles;
        try{
            categoryTitles = (ArrayList<String>) in.readObject();
        } catch (Exception e) {
            System.out.println("Categories not received");

            categoryTitles = new ArrayList<>(3);
            categoryTitles.add("error");
            categoryTitles.add("error");
            categoryTitles.add("error");
        }
        // Platform.runLater(()->System.out.println("Array list contains:" + categoryTitles.get(0) + categoryTitles.get(1) + categoryTitles.get(2)));

        // store in arraylists
        this.categoryTitles = categoryTitles;
        categoryAttemptsRemaining = new ArrayList<>();
        categoryAttemptsRemaining.add("3");
        categoryAttemptsRemaining.add("3");
        categoryAttemptsRemaining.add("3");

    } // end receiveCategoryTitles()


    // tells the GUI to change to the category scene
    public void changeToCategoryScene(String visit) {
        ArrayList<String> guiFunctionCall = new ArrayList<>();
        guiFunctionCall.add("setCategoryScene");
        guiFunctionCall.add(categoryTitles.get(0));
        guiFunctionCall.add(categoryTitles.get(1));
        guiFunctionCall.add(categoryTitles.get(2));
        guiFunctionCall.add(categoryAttemptsRemaining.get(0));
        guiFunctionCall.add(categoryAttemptsRemaining.get(1));
        guiFunctionCall.add(categoryAttemptsRemaining.get(2));
        guiFunctionCall.add(visit);

        guiUpdates.accept(guiFunctionCall);
    } // end changeToCategoryScene()


    // tells the server what category the user selected
    public void selectCategory() {
        while (currentCategory.isEmpty()) {
            try {
                sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            // Platform.runLater(()->System.out.println("Current category: " + currentCategory));
        } // infinite loop waiting until a button sets the selected category
        try {
            // Platform.runLater(()->System.out.println("In client thread: text is " + currentCategory));
            out.writeObject(currentCategory);
        } catch (Exception e) {
            System.out.println("Unable to send category to server");
        }
    } // end selectCategory()


    // receives current guessing state and tells the GUI to change to the guessing scene
    public void changeToGuessingScene() {
        // receive current guessing state from server
        GuessResponse serverResponse;
        try{
            serverResponse = (GuessResponse)in.readObject();
        } catch (Exception e) {
            System.out.println("Guessing state not received");
            throw new RuntimeException(e);
        }

        // set class variable
        currGuessState = serverResponse.displayString;

        // tells GUI what the guess state is,
        // GUI changes to the guessing scene
        ArrayList<String> guiFunctionCall = new ArrayList<>();
        guiFunctionCall.add("setGuessingScene");
        guiFunctionCall.add(currGuessState);

        guiUpdates.accept(guiFunctionCall);
    } // end changeToGuessingScene()


    // takes in the user's guess and sends it to the server
    public void sendGuess() {
        while (currentGuess == null) {
            try {
                sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        } // infinite loop waiting until a button sets the selected guess
        System.out.println("In clientThread: guess is " + currentGuess);
        try {
            out.writeObject(currentGuess);
        } catch (Exception e) {
            System.out.println("Error sending current guess");
        }
    } // end sendGuess()


    // tells the GUI to update the guessing scene
    public void updateGuessingScene() {
        // receive updated guessing state from server
        GuessResponse serverResponse;
        try{
            serverResponse = (GuessResponse)in.readObject();
        } catch (Exception e) {
            System.out.println("Guessing state not received");
            throw new RuntimeException(e);
        }

        gameWon = serverResponse.gameWon;
        gameLost = serverResponse.gameLost;
        roundWon = serverResponse.roundWon;
        roundLost = serverResponse.roundLost;

        if (!serverResponse.correctGuess) {
            System.out.println("Guess of '" + currentGuess + "' was wrong");
            if (wrongGuesses.isEmpty()) {
                wrongGuesses = "" + currentGuess;
            } else {
                wrongGuesses = wrongGuesses + ", " + currentGuess;
            }
        }
        currentGuess = null;

        // set class variable
        currGuessState = serverResponse.displayString;

        // tells GUI what the guess state is,
        // GUI changes to the guessing scene
        ArrayList<String> guiFunctionCall = new ArrayList<>();
        guiFunctionCall.add("updateGuessingScene");
        guiFunctionCall.add(currGuessState);
        guiFunctionCall.add(wrongGuesses);

        guiUpdates.accept(guiFunctionCall);
    } // end updateGuessingScene()


    // checks if word was fully guessed and prints to the screen accordingly
    // also tells the GUI to change what the button does
    public void resolveGuessingRound() {
        ArrayList<String> guiFunctionCall = new ArrayList<>();
        guiFunctionCall.add("resolveGuessingRound");

        // did not fully guess word
        if (roundLost) {
            int attempts = Integer.parseInt(categoryAttemptsRemaining.get(currentCategoryNumber-1));
            attempts--;
            categoryAttemptsRemaining.set(currentCategoryNumber-1, ""+attempts);
            guiFunctionCall.add("Sorry, you did not correctly guess this word.");
        }
        // successfully guessed word
        else {
            categoryAttemptsRemaining.set(currentCategoryNumber-1, "solved");
            guiFunctionCall.add("Congrats, you successfully guessed this word!");
        }
        currentCategory = "";

        guiFunctionCall.add(currGuessState);

        guiFunctionCall.add(categoryAttemptsRemaining.get(0));
        guiFunctionCall.add(categoryAttemptsRemaining.get(1));
        guiFunctionCall.add(categoryAttemptsRemaining.get(2));

        guiUpdates.accept(guiFunctionCall);
    } // end resolveGuessingRound()

    // tells gui to change to end scene and what to display
    public void goToEndScene() {
        ArrayList<String> guiFunctionCall = new ArrayList<>();
        guiFunctionCall.add("goToEndScene");
        if (gameWon) {
            guiFunctionCall.add("Congratulations you won!!!");
        } else if (gameLost) {
            guiFunctionCall.add("Sorry, you lost :(");
        }

        guiUpdates.accept(guiFunctionCall);
    } // goToEndScene()


} // end ClientThread class