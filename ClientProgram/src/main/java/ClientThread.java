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

    // gameplay data members
    private HashMap<String, Boolean> categories; // name of category, category solved
    private ArrayList<Character> wrongGuesses;
    private String currGuessState;
    private Consumer<Serializable> guiUpdates;

    // ----------------------------------------------------

    // everything that happens when the thread is running
    public void run() {

    }

    // constructor - sets up the way to talk to the GUI
    public ClientThread(Consumer<Serializable> call) {
        guiUpdates = call;
    }

    // takes in the address and port # and connects the client with the server
    public void establishConnection(String ipAddress, int portNum) {
        // for same machine, use ip address "127.0.0.1"
        try {
            connection = new Socket(ipAddress,portNum);
            in = new ObjectInputStream(connection.getInputStream());
            out = new ObjectOutputStream(connection.getOutputStream());
            connection.setTcpNoDelay(true);
        }
        catch(Exception e) {
            System.out.println("Issue connecting to the server. Try a different address.");
        }

        System.out.println("Successfully connected to the server?");

        while(true) {

            // TODO - ?
        }

    }

    // tells the server what category the user selected
    public void selectCategory(String category) {
        // TODO - write
    }

    // takes in the user's guess and sends it to the server
    public void sendGuess(char guess) {
        // TODO - write
    }


} // end ClientThread class