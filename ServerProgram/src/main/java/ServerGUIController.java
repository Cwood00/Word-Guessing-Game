import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.io.IOException;

public class ServerGUIController {
    @FXML
    public ListView<String> serverLog;
    @FXML
    public VBox selectPortRoot;
    @FXML
    private TextField inputTextField;
    @FXML
    private Text errorMessageText;

    public void startServer(){
        try {
            int portNumber = Integer.parseInt(inputTextField.getText().trim());

            FXMLLoader loader = new FXMLLoader(getClass().getResource("ServerLogScene.fxml"));
            Parent logSceneRoot = loader.load();
            logSceneRoot.getStylesheets().add("ServerLogSceneStyle.css");
            ServerGUIController newController = loader.getController();
            selectPortRoot.getScene().setRoot(logSceneRoot);

            ServerGUI.theServer = new Server(portNumber, message ->{
                Platform.runLater(() ->{
                    newController.serverLog.getItems().add(message.toString());
                });
            });
        }
        catch(NumberFormatException ex){
            errorMessageText.setText("Error port number must be a number");
            inputTextField.clear();
        }
        catch (IOException e){
            errorMessageText.setText("Error unable to load fxml file for server log scene");
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
