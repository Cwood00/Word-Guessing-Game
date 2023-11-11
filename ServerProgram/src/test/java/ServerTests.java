import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.Socket;

class ServerTests{

	String callBackReturn;
	@BeforeEach
	void setUp(){
		callBackReturn = "";
	}
	@Test
	void serverAcceptsClients(){
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
			assertEquals("Server is listening for clients", callBackReturn);
		}
		catch (IOException e){
			fail("Could not connect to server");
		}
	}
}
