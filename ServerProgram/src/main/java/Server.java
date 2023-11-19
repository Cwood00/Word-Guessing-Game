import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.function.Consumer;

//Back end of server application
public class Server{
    listeningThread listener;
    ArrayList<clientHandlerThread> clients;
    private Consumer<Serializable> GUIprinter;
    int nextClientNumber = 1;

    public Server(int portNumber, Consumer<Serializable> consumer){

        clients = new ArrayList<>();
        GUIprinter = consumer;
        try {
            listener = new listeningThread(portNumber);
            listener.start();
        }
        catch(IOException e){
            GUIprinter.accept("Server socket could not be opened, please restart the server and try again");
        }
    }
    //Main server listening thread
    private class listeningThread extends Thread{
        ServerSocket listeningSocket;
        public listeningThread(int portNumber) throws IOException {
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
        private HashMap<String, Integer> failedWords;
        private int solvedCategories;
        private boolean playerHasWon;
        private boolean playerHasLost;
        private Random rand;

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
                rand = new Random(System.currentTimeMillis());

                GUIprinter.accept("Sending categories to " + this.getName());
                out.writeObject(categories);
                while(!playerHasWon && !playerHasLost){
                    try {
                        String selectedCategory = in.readObject().toString();
                        GUIprinter.accept(this.getName() + " has selected category " + selectedCategory);
                        playRound(selectedCategory);
                        checkGameEnd();
                    }
                    catch (ClassNotFoundException e){
                        GUIprinter.accept(this.getName() + " has sent an invalid class");
                    }
                }
            }
            catch (IOException e){
                GUIprinter.accept(this.getName() + " has disconnected");
            }
        }

        private void playRound(String category) throws IOException, ClassNotFoundException {
            int incorrectGuesses = 0;

            ArrayList<String> categoryWord = words.get(category);
            String wordToGuess = categoryWord.get(rand.nextInt(categoryWord.size()));
            categoryWord.remove(wordToGuess);
            GUIprinter.accept(this.getName() + " is trying to guess " + wordToGuess);

            String clientDisplayString = "_".repeat(wordToGuess.length());
            boolean roundHasEnded = false;

            while(!roundHasEnded){
                out.writeObject(clientDisplayString);
                char guess = (Character)in.readObject();
                boolean guessInWord = false;
                int guessIndex = wordToGuess.indexOf(guess);
                while(guessIndex != -1){
                    clientDisplayString = clientDisplayString.substring(0, guessIndex) + guess + clientDisplayString.substring(guessIndex + 1);
                    guessInWord = true;
                    guessIndex = wordToGuess.indexOf(guess);
                }
                if(!guessInWord){
                    incorrectGuesses += 1;
                }
                if(incorrectGuesses >= 6){
                    this.failedWords.replace(category, (this.failedWords.get(category) + 1));
                    roundHasEnded = true;
                    GUIprinter.accept(this.getName() + " failed to guess " + wordToGuess);
                } else if(!clientDisplayString.contains("_")){
                    roundHasEnded = true;
                    GUIprinter.accept(this.getName() + " successfully guessed " + wordToGuess);
                }
            }
        }

        private void checkGameEnd(){
            if(solvedCategories == 3){
                playerHasWon = true;
                GUIprinter.accept(this.getName() + " has won");
            }else{
                for(String category: categories){
                    if(failedWords.get(category) == 3){
                        playerHasLost = true;
                        GUIprinter.accept(this.getName() + " has lost");
                    }
                }
            }
        }

        private void populateWordsMaps(){
            //TODO read this information from a file
            categories = new ArrayList<>();
            words = new HashMap<>();
            failedWords = new HashMap<>();

            categories.add("Category1");
            categories.add("Category2");
            categories.add("Category3");

            ArrayList<String> Category1Words = new ArrayList<>();
            Category1Words.add("Cat1Word1");
            Category1Words.add("Cat1Word2");
            Category1Words.add("Cat1Word3");
            words.put(categories.get(0), Category1Words);
            failedWords.put(categories.get(0), 0);

            ArrayList<String> Category2Words = new ArrayList<>();
            Category2Words.add("Cat2Word1");
            Category2Words.add("Cat2Word2");
            Category2Words.add("Cat2Word3");
            words.put(categories.get(1), Category2Words);
            failedWords.put(categories.get(1), 0);

            ArrayList<String> Category3Words = new ArrayList<>();
            Category3Words.add("Cat3Word1");
            Category3Words.add("Cat3Word2");
            Category3Words.add("Cat3Word3");
            words.put(categories.get(2), Category3Words);
            failedWords.put(categories.get(2), 0);

        }
    }
}