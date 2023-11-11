import java.io.IOException;
import java.io.Serializable;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.function.Consumer;

//Back end of server application
public class Server{
    listeningTread listener;
    ArrayList<clientHandlerThread> clients ;
    private Consumer<Serializable> GUIprinter;

    public Server(int portNumber, Consumer<Serializable> consumer){

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
        }
    }
    //Plays game with one client
    private class clientHandlerThread extends Thread{

        @Override
        public void run(){

        }
    }
}
