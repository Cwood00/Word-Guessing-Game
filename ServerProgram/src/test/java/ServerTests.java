import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

class ServerTests{

	String callBackReturn;

	@Test
	void serverListensForClients(){
		Server server = new Server(55555, s->{
			//no-op
		});
		try{
			Socket clientSocket = new Socket("127.0.0.1", 55555);
		}
		catch (IOException e){
            fail("Could not connect to server");
		}
	}

	@Test
	void serverCallBack(){
		Server server = new Server(55556, s-> {
			callBackReturn = s.toString();
		});
		try{
			Socket clientSocket = new Socket("127.0.0.1", 55556);
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
		Server server = new Server(55557, s->{
			callBackReturn = s.toString();
		});
		try {
			Socket clientSocket = new Socket("127.0.0.1", 55557);
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
		Server server = new Server(55558, s->{
			//no-op
		});
		try {
			//client1 connects to the server, but does not set up I/O streams
			Socket client1 = new Socket("127.0.0.1", 55558);

			Socket client2 = new Socket("127.0.0.1", 55558);
			Socket client3 = new Socket("127.0.0.1", 55558);

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
		Server server = new Server(55559, s->{
			//no-op
		});
		try {
			Socket clientSocket = new Socket("127.0.0.1", 55559);
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
		Server server = new Server(55560, s->{
			callBackReturn = s.toString();
		});
		try {
			Socket clientSocket = new Socket("127.0.0.1", 55560);
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

			GuessResponse response = (GuessResponse)in.readObject();
			assertEquals("_________", response.displayString);

			out.writeObject('i');
			response = (GuessResponse)in.readObject();
			assertEquals("_i_______", response.displayString);

			out.writeObject('N');
			response = (GuessResponse)in.readObject();
			assertEquals("_in______", response.displayString);

			out.writeObject('z');
			response = (GuessResponse)in.readObject();
			assertEquals("_in______", response.displayString);

			out.writeObject('e');
			response = (GuessResponse)in.readObject();
			assertEquals("_ine____e", response.displayString);

			out.writeObject('p');
			response = (GuessResponse)in.readObject();
			assertEquals("Pine_pp_e", response.displayString);

			out.writeObject('y');
			response = (GuessResponse)in.readObject();
			assertEquals("Pine_pp_e", response.displayString);

			out.writeObject('l');
			response = (GuessResponse)in.readObject();
			assertEquals("Pine_pple", response.displayString);

			out.writeObject('a');
			response = (GuessResponse)in.readObject();
			assertEquals("Pineapple", response.displayString);

			assertTrue(response.roundWon);
			assertFalse(response.roundLost);

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
	@Test
	void playRoundLose(){
		Server server = new Server(55561, s->{
			callBackReturn = s.toString();
		});
		try {
			Socket clientSocket = new Socket("127.0.0.1", 55561);
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

			GuessResponse response = (GuessResponse)in.readObject();
			assertEquals("_________", response.displayString);

			out.writeObject('z');
			response = (GuessResponse)in.readObject();
			assertEquals("_________", response.displayString);

			out.writeObject('y');
			response = (GuessResponse)in.readObject();
			assertEquals("_________", response.displayString);

			out.writeObject('i');
			response = (GuessResponse)in.readObject();
			assertEquals("_i_______", response.displayString);

			out.writeObject('x');
			response = (GuessResponse)in.readObject();
			assertEquals("_i_______", response.displayString);

			out.writeObject('w');
			response = (GuessResponse)in.readObject();
			assertEquals("_i_______", response.displayString);

			out.writeObject('e');
			response = (GuessResponse)in.readObject();
			assertEquals("_i_e____e", response.displayString);

			out.writeObject('v');
			response = (GuessResponse)in.readObject();
			assertEquals("_i_e____e", response.displayString);

			out.writeObject('u');
			response = (GuessResponse)in.readObject();
			assertEquals("_i_e____e", response.displayString);

			assertTrue(response.roundLost);
			assertFalse(response.roundWon);

			//Put the tread to sleep, to wait for server call back response
			try {Thread.sleep(100);}
			catch (InterruptedException e){/* no-op */}
			assertEquals("Client #1 failed to guess Pineapple", callBackReturn);
		}
		catch (IOException e) {
			fail("Could not connect to the server");
		}
		catch (ClassNotFoundException e){
			fail("Server sent bad class");
		}
	}

	@Test
	void winGame(){
		Server server = new Server(55563, s->{
			callBackReturn = s.toString();
		});
		try {
			Socket clientSocket = new Socket("127.0.0.1", 55563);
			ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
			ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
			clientSocket.setTcpNoDelay(true);

			//Put the tread to sleep, to wait for server
			try {Thread.sleep(100);}
			catch (InterruptedException e){/* no-op */}

			Server.ClientHandlerThread clientHandler = server.clients.get(0);
			clientHandler.populateWordsMaps("src/test/resources/testWords3.txt");

			ArrayList<String> categories = (ArrayList<String>)in.readObject();

			out.writeObject("Pets");
			in.readObject();

			out.writeObject('c');
			in.readObject();

			out.writeObject('a');
			in.readObject();

			out.writeObject('t');
			in.readObject();

			out.writeObject("Colors");
			in.readObject();

			out.writeObject('r');
			in.readObject();

			out.writeObject('e');
			in.readObject();

			out.writeObject('d');
			in.readObject();

			out.writeObject("Palindromes");
			in.readObject();

			out.writeObject('d');
			in.readObject();

			out.writeObject('a');
			GuessResponse response = (GuessResponse)in.readObject();

			assertTrue(response.gameWon);
			assertFalse(response.gameLost);

			//Put the tread to sleep, to wait for server call back response
			try {Thread.sleep(100);}
			catch (InterruptedException e){/* no-op */}
			assertEquals("Client #1 has won", callBackReturn);
		}
		catch (IOException e) {
			fail("Could not connect to the server");
		}
		catch (ClassNotFoundException e){
			fail("Server sent bad class");
		}
	}

	@Test
	void loseGame(){
		Server server = new Server(55564, s->{
			callBackReturn = s.toString();
		});
		try {
			Socket clientSocket = new Socket("127.0.0.1", 55564);
			ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
			ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
			clientSocket.setTcpNoDelay(true);

			//Put the tread to sleep, to wait for server
			try {Thread.sleep(100);}
			catch (InterruptedException e){/* no-op */}

			Server.ClientHandlerThread clientHandler = server.clients.get(0);
			clientHandler.populateWordsMaps("src/test/resources/testWords4.txt");

			ArrayList<String> categories = (ArrayList<String>)in.readObject();

			GuessResponse response = null;

			//Play 3 games losing each
			for(int i = 0; i < 3; i++){
				out.writeObject("Fruits");
				in.readObject();

				out.writeObject('z');
				in.readObject();

				out.writeObject('y');
				in.readObject();

				out.writeObject('x');
				in.readObject();

				out.writeObject('w');
				in.readObject();

				out.writeObject('v');
				in.readObject();

				out.writeObject('u');
				response = (GuessResponse) in.readObject();
			}

			assertFalse(response.gameWon);
			assertTrue(response.gameLost);

			//Put the tread to sleep, to wait for server call back response
			try {Thread.sleep(100);}
			catch (InterruptedException e){/* no-op */}
			assertEquals("Client #1 has lost", callBackReturn);
		}
		catch (IOException e) {
			fail("Could not connect to the server");
		}
		catch (ClassNotFoundException e){
			fail("Server sent bad class");
		}
	}

	@Test
	void doesNotRepeatWords(){
		Server server = new Server(55565, s->{
			callBackReturn = s.toString();
		});
		try {
			Socket clientSocket = new Socket("127.0.0.1", 55565);
			ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
			ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
			clientSocket.setTcpNoDelay(true);

			//Put the tread to sleep, to wait for server
			try {Thread.sleep(100);}
			catch (InterruptedException e){/* no-op */}

			Server.ClientHandlerThread clientHandler = server.clients.get(0);
			clientHandler.populateWordsMaps("src/test/resources/testWords5.txt");

			ArrayList<String> categories = (ArrayList<String>)in.readObject();

			ArrayList<String> partiallyGuessedWords = new ArrayList<>();

			for(int i = 0; i < 3; i++){
				out.writeObject("Test");
				in.readObject();

				out.writeObject('x');
				partiallyGuessedWords.add(((GuessResponse)in.readObject()).displayString);

				out.writeObject('a');
				in.readObject();

				out.writeObject('b');
				in.readObject();

				out.writeObject('c');
				in.readObject();

				out.writeObject('d');
				in.readObject();

				out.writeObject('e');
				in.readObject();

				out.writeObject('f');
				in.readObject();
			}
			assertNotEquals(partiallyGuessedWords.get(0), partiallyGuessedWords.get(1));
			assertNotEquals(partiallyGuessedWords.get(0), partiallyGuessedWords.get(2));
			assertNotEquals(partiallyGuessedWords.get(1), partiallyGuessedWords.get(2));
		}
		catch (IOException e) {
			fail("Could not connect to the server");
		}
		catch (ClassNotFoundException e){
			fail("Server sent bad class");
		}
	}
}
