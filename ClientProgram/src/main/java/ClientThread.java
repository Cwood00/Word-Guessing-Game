import javafx.application.Platform;

import java.io.IOException;
import java.net.Socket;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
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
    private HashMap<String, Boolean> categories; // name of category, category solved
    protected String currentCategory;
    protected Character currentGuess;
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
        currentGuess = null;

        // receives category titles from the server and updates the GUI
        changeToCategoryScene();

        System.out.println("CT: Changed to category scene");

        // sends selected category to the server
        selectCategory();

        System.out.println("CT: Sent category to server");


        // receives current guess state and changes to guessing scene
        changeToGuessingScene();

        System.out.println("CT: Changed to guessing scene, current guessing state is: " + currGuessState);

        // while round is still going
        wrongGuesses = "";
        while (wrongGuesses.length() < 6 && currGuessState.contains("_")) {

            // user sends guess, receives updated guess state
            sendGuess();

            System.out.println("CT: Sent guess to server");

            // receives updated game state, updates scene
            updateGuessingScene();

            System.out.println("CT: Updated guessing state is: " + currGuessState);

        }


        System.out.println("Now what");
    } // end run

    // constructor - sets up the way to talk to the GUI
    public ClientThread(Consumer<Serializable> call, String ip, int port) {
        this.guiUpdates = call;
        this.ipAddress = ip;
        this.portNum = port;
    }

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

    }

    // receives category titles and tells the GUI to change to the category scene
    public void changeToCategoryScene() {
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
        // System.out.println("Array list contains:" + categoryTitles.get(0) + categoryTitles.get(1) + categoryTitles.get(2));

        // put in hash map
        categories = new HashMap<>();
        categories.put(categoryTitles.get(0), false);
        categories.put(categoryTitles.get(1), false);
        categories.put(categoryTitles.get(2), false);

        // tells GUI what the category titles are,
        // GUI changes to the select category scene
        ArrayList<String> guiFunctionCall = new ArrayList<>();
        guiFunctionCall.add("setCategoryScene");
        guiFunctionCall.add(categoryTitles.get(0));
        guiFunctionCall.add(categoryTitles.get(1));
        guiFunctionCall.add(categoryTitles.get(2));

        guiUpdates.accept(guiFunctionCall);
    }

    // tells the server what category the user selected
    public void selectCategory() {
        while (currentCategory.isEmpty()) {
            try {
                sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            // System.out.println("Current category: " + currentCategory);
        } // infinite loop waiting until a button sets the selected category
        try {
            // System.out.println("In client thread: text is " + currentCategory);
            out.writeObject(currentCategory);
            currentCategory = "";
        } catch (Exception e) {
            System.out.println("Unable to send category to server");
        }
    } // end selectCategory


    // receives current guessing state and tells the GUI to change to the guessing scene
    public void changeToGuessingScene() {
        // receive current guessing state from server
        String currentState;
        try{
            currentState = in.readObject().toString();
        } catch (Exception e) {
            System.out.println("Guessing state not received");
            currentState = "error in receiving word";
        }

        // set class variable
        currGuessState = currentState;

        // tells GUI what the guess state is,
        // GUI changes to the guessing scene
        ArrayList<String> guiFunctionCall = new ArrayList<>();
        guiFunctionCall.add("setGuessingScene");
        guiFunctionCall.add(currGuessState);

        guiUpdates.accept(guiFunctionCall);
    }


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
    } // end sendGuess


    // tells the GUI to update the guessing scene
    public void updateGuessingScene() {
        // receive updated guessing state from server
        String currentState;
        try{
            currentState = in.readObject().toString();
        } catch (Exception e) {
            System.out.println("Guessing state not received");
            currentState = "error in receiving word";
        }

        // checks if the guess was incorrect or not (if the string changed or not)
        if (!Objects.equals(currentState, currGuessState)) {
            if (wrongGuesses.isEmpty()) {
                wrongGuesses = "" + currentGuess;
            } else {
                wrongGuesses = wrongGuesses + ", " + currentGuess;
            }
            currentGuess = null;
        }

        // set class variable
        currGuessState = currentState;

        // tells GUI what the guess state is,
        // GUI changes to the guessing scene
        ArrayList<String> guiFunctionCall = new ArrayList<>();
        guiFunctionCall.add("updateGuessingScene");
        guiFunctionCall.add(currGuessState);

        guiUpdates.accept(guiFunctionCall);
    }


} // end ClientThread class