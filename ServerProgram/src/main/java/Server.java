import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.function.Consumer;

//Back end of server application
public class Server{
    ListeningThread listener;
    ArrayList<ClientHandlerThread> clients;
    private Consumer<Serializable> GUIprinter;
    int nextClientNumber = 1;

    public Server(int portNumber, Consumer<Serializable> consumer){

        clients = new ArrayList<>();
        GUIprinter = consumer;
        try {
            listener = new ListeningThread(portNumber);
            listener.setDaemon(true);
            listener.start();
        }
        catch(IOException e){
            GUIprinter.accept("Server socket could not be opened, please restart the server and try again");
        }
    }
    //Main server listening thread
    private class ListeningThread extends Thread{
        ServerSocket listeningSocket;
        public ListeningThread(int portNumber) throws IOException {
            listeningSocket = new ServerSocket(portNumber);
        }
        @Override
        public void run(){
            GUIprinter.accept("Server is listening for clients");
            try{
                //Accept loop each client is assigned a separate ClientHandlerThread to play the game with
                while (true){
                    ClientHandlerThread newClient = new ClientHandlerThread(listeningSocket.accept());
                    newClient.setName("Client #" + nextClientNumber);
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
    class ClientHandlerThread extends Thread{
        Socket connection;
        ObjectInputStream in;
        ObjectOutputStream out;
        ArrayList<String> categories;
        HashMap<String, ArrayList<String>> words;
        HashMap<String, Integer> failedWords;
        private int solvedCategories;
        private boolean playerHasWon;
        private boolean playerHasLost;
        private Random rand;

        public ClientHandlerThread(Socket incomingConnection) {
            connection = incomingConnection;
        }

        @Override
        public void run(){
            //Set up I/O streams
            try {
                out = new ObjectOutputStream(connection.getOutputStream());
                in = new ObjectInputStream(connection.getInputStream());
                connection.setTcpNoDelay(true);
            }
            catch (IOException e){
                GUIprinter.accept("Failed to establish I/O streams for " + this.getName());
            }
            //Initialize variables
            try {
                populateWordsMaps("src/main/resources/Words.txt");

                solvedCategories = 0;
                playerHasWon = false;
                playerHasLost = false;
                rand = new Random(System.currentTimeMillis());
                //Send categories to client
                GUIprinter.accept("Sending categories to " + this.getName());
                out.writeObject(categories);
                //Main game loop
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
            catch (FileNotFoundException e){
                GUIprinter.accept(this.getName() + " could not open words file");
            }
            catch (IOException e){
                GUIprinter.accept(this.getName() + " has disconnected");
            }
            clients.remove(this);
        }

         //Plays a single round with the user, consisting of guessing a single word
         //Takes in the users category choice as an argument
         void playRound(String category) throws IOException, ClassNotFoundException {
            int incorrectGuesses = 0;
            //Randomly choose a word from the category for the user to guess
            ArrayList<String> categoryWord = words.get(category);
            String wordToGuess = categoryWord.get(rand.nextInt(categoryWord.size()));
            String remainingWordToGuess = wordToGuess.toLowerCase();
            categoryWord.remove(wordToGuess);
            GUIprinter.accept(this.getName() + " is trying to guess " + wordToGuess);
            //Set up the string being sent to the user
            String clientDisplayString = "_".repeat(wordToGuess.length());
            out.writeObject(clientDisplayString);
            boolean roundHasEnded = false;
            //Play the round
            while(!roundHasEnded){
                //Receive the guess from the client
                char guess = Character.toLowerCase((Character)in.readObject());
                //Determine if the guess is in the word, and mark correct letters is string sent to user
                boolean guessInWord = false;
                int guessIndex = remainingWordToGuess.indexOf(guess);
                while(guessIndex != -1){
                    clientDisplayString = clientDisplayString.substring(0, guessIndex) + wordToGuess.charAt(guessIndex) + clientDisplayString.substring(guessIndex + 1);
                    remainingWordToGuess = remainingWordToGuess.substring(0, guessIndex) + "_" + remainingWordToGuess.substring(guessIndex + 1);
                    guessInWord = true;
                    guessIndex = remainingWordToGuess.indexOf(guess);
                }
                //Count incorrect guesses
                if(!guessInWord){
                    incorrectGuesses += 1;
                }
                //End the round if the client uses are their guesses, or has guessed every letter in the word
                if(incorrectGuesses >= 6){
                    this.failedWords.replace(category, (this.failedWords.get(category) + 1));
                    roundHasEnded = true;
                    GUIprinter.accept(this.getName() + " failed to guess " + wordToGuess);
                } else if(!clientDisplayString.contains("_")){
                    roundHasEnded = true;
                    solvedCategories++;
                    GUIprinter.accept(this.getName() + " successfully guessed " + wordToGuess);
                }
                //Send partially guessed word
                out.writeObject(clientDisplayString);
            }
        }

        //Checks to see if the game has endded
        void checkGameEnd(){
            //Game ends when all 3 categories have been solved, or the client has fail 3 guesses in a category
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
        //Populates the data structures containing the words and categories
        //Takes in a file to read data from as an argument
        void populateWordsMaps(String fileName) throws IOException {
            this.categories = new ArrayList<>();
            this.words = new HashMap<>();
            this.failedWords = new HashMap<>();

            BufferedReader fileReader = new BufferedReader(new FileReader(fileName));

            String category = fileReader.readLine();
            while(category != null && !category.isEmpty()) {
                ArrayList<String> categoryWords = new ArrayList<>();

                String words = fileReader.readLine();
                int commaIndex = words.indexOf(',');
                while(commaIndex != -1){
                    categoryWords.add(words.substring(0, commaIndex).trim());
                    words = words.substring(commaIndex + 1);
                    commaIndex = words.indexOf(',');
                }
                categoryWords.add(words.trim());

                this.categories.add(category);
                this.words.put(category, categoryWords);
                this.failedWords.put(category, 0);

                category = fileReader.readLine();
            }

            fileReader.close();
        }
    }
}