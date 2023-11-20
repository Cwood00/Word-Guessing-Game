import javafx.application.Platform;

import java.io.IOException;
import java.net.Socket;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
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
    private ArrayList<Character> wrongGuesses;
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
        changeToGuessingScene();

        // sends selected category to the server
        selectCategory();


        // receives current guess state
        try {
            currGuessState = in.readObject().toString();
        } catch (Exception e) {
            System.out.println("Error receiving current guess state");
        }
        // System.out.println("Current game state: " + currGuessState);

//        updateGuessingScene();
//
//        // user sends guess, receives updated guess state
//        sendGuess();




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

    // takes in the user's guess and sends it to the server
    // also receives updated game state
    public void sendGuess() {
        while (currentGuess == null) {
            try {
                sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        } // infinite loop waiting until a button sets the selected guess
        try {
            out.writeObject(currentGuess);
            currentGuess = null;
            currGuessState = in.readObject().toString();
        } catch (Exception e) {
            System.out.println("Error sending current guess or reading current guess state");
        }
    } // end sendGuess


    // receives category titles and tells the GUI to change to the guessing scene
    public void changeToGuessingScene() {
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

    // tells the GUI to update the guessing scene
    public void updateGuessingScene() {
        // TODO - write
    }


} // end ClientThread class