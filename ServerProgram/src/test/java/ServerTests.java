import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

class ServerTests{

	String callBackReturn;

	@Test
	void serverListensForClients(){
		Server server = new Server(5555, s->{
			//no-op
		});
		try{
			Socket clientSocket = new Socket("127.0.0.1", 5555);
		}
		catch (IOException e){
            fail("Could not connect to server");
		}
	}

	@Test
	void serverCallBack(){
		Server server = new Server(5556, s-> {
			callBackReturn = s.toString();
		});
		try{
			Socket clientSocket = new Socket("127.0.0.1", 5556);
			//Put the tread to sleep, to wait for server call back response
			try {Thread.sleep(100);}
			catch (InterruptedException e){/* no-op */}
			assertEquals("Server is listening for clients", callBackReturn);
		}
		catch (IOException e){
			fail("Could not connect to server");
		}
	}
	@Test
	void serverSendsCategories(){
		Server server = new Server(5557, s->{
			callBackReturn = s.toString();
		});
		try {
			Socket clientSocket = new Socket("127.0.0.1", 5557);
			ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
			ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
			clientSocket.setTcpNoDelay(true);
			ArrayList<String> categories = (ArrayList<String>)in.readObject();
			assertEquals("Sending categories to client #1", callBackReturn);
			assertEquals(3, categories.size());
			for(String category :categories){
				assertNotNull(category);
				assertNotEquals("", category);
			}
		}
		catch (IOException e) {
			fail("Could not connect to the server");
		}
		catch (ClassNotFoundException e){
			fail("Server sent bad class");
		}
	}
	@Test
	void serverCanAcceptMultipleClients(){
		Server server = new Server(5558, s->{
			//on-op
		});
		try {
			//client1 connects to the server, but does not set up I/O streams
			Socket client1 = new Socket("127.0.0.1", 5558);

			Socket client2 = new Socket("127.0.0.1", 5558);
			Socket client3 = new Socket("127.0.0.1", 5558);

			ObjectInputStream c2in = new ObjectInputStream(client2.getInputStream());
			ObjectOutputStream c2out = new ObjectOutputStream(client2.getOutputStream());
			ObjectInputStream c3in = new ObjectInputStream(client3.getInputStream());
			ObjectOutputStream c3out = new ObjectOutputStream(client3.getOutputStream());

			ArrayList<String> c2categories = (ArrayList<String>)c2in.readObject();
			ArrayList<String> c3categories = (ArrayList<String>)c3in.readObject();

			assertEquals(3, c2categories.size());
			assertEquals(3, c3categories.size());

			for (int i = 0; i < 3; i++){
				assertEquals(c2categories.get(i), c3categories.get(i));
			}
		}
		catch (IOException e) {
			fail("Could not connect to the server");
		}
		catch (ClassNotFoundException e){
			fail("Server sent bad class");
		}
	}
}
