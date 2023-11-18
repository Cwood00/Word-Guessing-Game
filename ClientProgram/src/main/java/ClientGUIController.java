import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

// Contains all the controllers for the fxml elements
public class ClientGUIController {

// SCENE 1 - connect to server ------------------------------------------------
    @FXML
    public VBox connectionRoot;

    @FXML
    public HBox ipHolder;

    @FXML
    public TextField ipText;

    @FXML
    public TextField ipInput;

    @FXML
    public HBox portHolder;

    @FXML
    public TextField portText;

    @FXML
    public TextField portInput;

    @FXML
    public HBox connectButtonHolder;

    @FXML
    public Button connectServerButton;


    public void connectToServer() {
        String ipAddress = ipInput.getText();
        ipInput.clear();
        int portNum = Integer.parseInt(portInput.getText());
        portInput.clear();

        ClientGUI.client = new ClientThread(data -> {
            // TODO - change to take arraylist w data which says what function here to call
            Platform.runLater((Runnable) data);
        }, ipAddress, portNum);

        ClientGUI.client.start();

    }



// SCENE 2 - select category --------------------------------------------------

    @FXML
    public BorderPane selectCategoryRoot;

    @FXML
    public Text selectCategoryText;

    @FXML
    public Text remainingAttemptsText;

    @FXML
    public HBox categoryButtonHolder;

    @FXML
    public Button category1Button;

    @FXML
    public Button category2Button;

    @FXML
    public Button category3Button;



} // end class
