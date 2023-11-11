import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Text;

public class ServerGUIController {
    @FXML
    private TextField inputTextField;
    @FXML
    private Text errorMessageText;

    public void startServer(){
        try {
            int portNumber = Integer.parseInt(inputTextField.getText().trim());
            //TODO set up switching scenes
            ServerGUI.theServer = new Server(portNumber,  e ->{
               //TODO set up call back for updating server log
            });
        }
        catch(NumberFormatException ex){
            errorMessageText.setText("Error port number must be a number");
            inputTextField.clear();
        }
    }
    public void submitButtonHandler(ActionEvent e){
        startServer();
    }
    public void keyboardInputHandler(KeyEvent event){
        if(event.getCode().equals(KeyCode.ENTER)) {
            startServer();
        }
    }
}
