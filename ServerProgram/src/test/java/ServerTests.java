import static org.junit.jupiter.api.Assertions.*;

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
			assertEquals("Sending categories to Client #1", callBackReturn);
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
			//no-op
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
	@Test
	void serverReadsWordsFromFile(){
		Server server = new Server(5559, s->{
			//no-op
		});
		try {
			Socket clientSocket = new Socket("127.0.0.1", 5559);
			ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
			ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
			clientSocket.setTcpNoDelay(true);

			//Put the tread to sleep, to wait for server
			try {Thread.sleep(100);}
			catch (InterruptedException e){/* no-op */}
			Server.ClientHandlerThread clientHandler = server.clients.get(0);
			clientHandler.populateWordsMaps("src/test/resources/testWords1.txt");

			String[] expectedCat1Words = {"Cat1Word1", "Cat1Word2", "Cat1Word3"};
			String[] expectedCat2Words = {"Cat2Word1", "Cat2Word2", "Cat2Word3"};
			String[] expectedCat3Words = {"Cat3Word1", "Cat3Word2", "Cat3Word3"};

			assertArrayEquals(expectedCat1Words, clientHandler.words.get("Category1").toArray());
			assertArrayEquals(expectedCat2Words, clientHandler.words.get("Category2").toArray());
			assertArrayEquals(expectedCat3Words, clientHandler.words.get("Category3").toArray());

			assertEquals(0, clientHandler.failedWords.get("Category1"));
			assertEquals(0, clientHandler.failedWords.get("Category2"));
			assertEquals(0, clientHandler.failedWords.get("Category3"));
		}
		catch (IOException e) {
			fail("Could not connect to the server");
		}
	}

	@Test
	void playRoundWin(){
		Server server = new Server(5560, s->{
			callBackReturn = s.toString();
		});
		try {
			Socket clientSocket = new Socket("127.0.0.1", 5560);
			ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
			ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
			clientSocket.setTcpNoDelay(true);

			//Put the tread to sleep, to wait for server
			try {Thread.sleep(100);}
			catch (InterruptedException e){/* no-op */}

			Server.ClientHandlerThread clientHandler = server.clients.get(0);
			clientHandler.populateWordsMaps("src/test/resources/testWords2.txt");

			ArrayList<String> categories = (ArrayList<String>)in.readObject();
			out.writeObject("Fruits");

			String receivedString = in.readObject().toString();
			assertEquals("_________", receivedString);

			out.writeObject('i');
			receivedString = in.readObject().toString();
			assertEquals("_i_______", receivedString);

			out.writeObject('N');
			receivedString = in.readObject().toString();
			assertEquals("_in______", receivedString);

			out.writeObject('z');
			receivedString = in.readObject().toString();
			assertEquals("_in______", receivedString);

			out.writeObject('e');
			receivedString = in.readObject().toString();
			assertEquals("_ine____e", receivedString);

			out.writeObject('p');
			receivedString = in.readObject().toString();
			assertEquals("Pine_pp_e", receivedString);

			out.writeObject('y');
			receivedString = in.readObject().toString();
			assertEquals("Pine_pp_e", receivedString);

			out.writeObject('l');
			receivedString = in.readObject().toString();
			assertEquals("Pine_pple", receivedString);

			out.writeObject('a');
			receivedString = in.readObject().toString();
			assertEquals("Pineapple", receivedString);

			//Put the tread to sleep, to wait for server call back response
			try {Thread.sleep(100);}
			catch (InterruptedException e){/* no-op */}
			assertEquals("Client #1 successfully guessed Pineapple", callBackReturn);
		}
		catch (IOException e) {
			fail("Could not connect to the server");
		}
		catch (ClassNotFoundException e){
			fail("Server sent bad class");
		}
	}
}
