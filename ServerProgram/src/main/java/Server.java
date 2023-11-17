import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Consumer;

//Back end of server application
public class Server{
    listeningTread listener;
    ArrayList<clientHandlerThread> clients;
    private Consumer<Serializable> GUIprinter;
    int nextClientNumber = 1;

    public Server(int portNumber, Consumer<Serializable> consumer){

        clients = new ArrayList<>();
        GUIprinter = consumer;
        try {
            listener = new listeningTread(portNumber);
            listener.start();
        }
        catch(IOException e){
            GUIprinter.accept("Server socket could not be opened, please restart the server and try again");
        }
    }
    //Main server listening thread
    private class listeningTread extends Thread{
        ServerSocket listeningSocket;
        public listeningTread(int portNumber) throws IOException {
            listeningSocket = new ServerSocket(portNumber);
        }
        @Override
        public void run(){
            GUIprinter.accept("Server is listening for clients");
            try{
                while (true){
                    clientHandlerThread newClient = new clientHandlerThread(listeningSocket.accept());
                    newClient.setName("client #" + nextClientNumber);
                    nextClientNumber++;
                    newClient.start();
                    clients.add(newClient);
                }
            }
            catch(IOException e){
                GUIprinter.accept("IO exception in accept loop");
            }
        }
    }//End class ListeningThread
    //Plays game with one client
    private class clientHandlerThread extends Thread{
        Socket connection;
        ObjectInputStream in;
        ObjectOutputStream out;
        private ArrayList<String> categories;
        private HashMap<String, ArrayList<String>> words;
        private HashMap<String, Integer> failedGuesses;
        private int solvedCategories;
        private boolean playerHasWon;
        private boolean playerHasLost;

        public clientHandlerThread(Socket incomingConnection) {
            connection = incomingConnection;
        }

        @Override
        public void run(){
            try {
                out = new ObjectOutputStream(connection.getOutputStream());
                in = new ObjectInputStream(connection.getInputStream());
                connection.setTcpNoDelay(true);
            }
            catch (IOException e){
                GUIprinter.accept("Failed to establish I/O streams for " + this.getName());
            }
            try {
                populateWordsMaps();

                solvedCategories = 0;
                playerHasWon = false;
                playerHasLost = false;

                GUIprinter.accept("Sending categories to " + this.getName());
                out.writeObject(categories);
                //while(!playerHasWon && !playerHasLost){
                    //TODO play game
                //}
            }
            catch (IOException e){
                GUIprinter.accept(this.getName() + " has disconnected");
            }
        }
        private void populateWordsMaps(){
            //TODO read this information from a file
            categories = new ArrayList<>();
            words = new HashMap<>();
            failedGuesses = new HashMap<>();

            categories.add("Category1");
            categories.add("Category2");
            categories.add("Category3");

            ArrayList<String> Category1Words = new ArrayList<>();
            Category1Words.add("Cat1Word1");
            Category1Words.add("Cat1Word2");
            Category1Words.add("Cat1Word3");
            words.put(categories.get(0), Category1Words);
            failedGuesses.put(categories.get(0), 0);

            ArrayList<String> Category2Words = new ArrayList<>();
            Category2Words.add("Cat2Word1");
            Category2Words.add("Cat2Word2");
            Category2Words.add("Cat2Word3");
            words.put(categories.get(1), Category2Words);
            failedGuesses.put(categories.get(1), 0);

            ArrayList<String> Category3Words = new ArrayList<>();
            Category3Words.add("Cat3Word1");
            Category3Words.add("Cat3Word2");
            Category3Words.add("Cat3Word3");
            words.put(categories.get(2), Category3Words);
            failedGuesses.put(categories.get(2), 0);

        }
    }
}