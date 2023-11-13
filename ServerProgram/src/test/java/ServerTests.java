import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.ArrayList;

class ServerTests{

	String callBackReturn;
	@BeforeEach
	void setUp(){
		callBackReturn = "";
	}
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
			this.callBackReturn = s.toString();
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
			this.callBackReturn = s.toString();
		});
		try {
			Socket clientSocket = new Socket("127.0.0.1", 55557);
			ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
			clientSocket.setTcpNoDelay(true);
			ArrayList<String> categories = (ArrayList<String>)in.readObject();
			assertEquals(3, categories.size());
		}
		catch (IOException e) {
			fail("Could not connect to the server");
		}
		catch (ClassNotFoundException e){
			fail("Server sent bad class");
		}
	}
}
