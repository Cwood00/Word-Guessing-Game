import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.Scene;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

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


    // event handler for connectServerButton
    public void connectToServer() {
        String ipAddress = ipInput.getText();
        ipInput.clear();
        int portNum = Integer.parseInt(portInput.getText());
        portInput.clear();

        // definition for what happens when ClientThread calls guiUpdates.accept(data)
        ClientGUI.client = new ClientThread(data -> {
            Platform.runLater(()-> {
                // data will always be an arraylist of strings
                // first element is what function to call
                // rest of elements are parameters needed for that function
                ArrayList<String> input = (ArrayList<String>) data;

                // change to category scene
                if (Objects.equals(input.get(0), "setCategoryScene")) {
                    //System.out.println(input.toString());
                    setCategoryScene(input.get(1), input.get(2), input.get(3), input.get(4), input.get(5), input.get(6), input.get(7));
                }
                // change to guessing scene
                else if (Objects.equals(input.get(0), "setGuessingScene")) {
                    setGuessingScene(input.get(1));
                }
                // update guessing scene
                else if (Objects.equals(input.get(0), "updateGuessingScene")) {
                    updateGuessingScene(input.get(1), input.get(2));
                }
                // resolve guessing round
                else if (Objects.equals(input.get(0), "resolveGuessingRound")) {
                    resolveGuessingRound(input.get(1), input.get(2), input.get(3), input.get(4), input.get(5));
                }
                // go to end scene
                else if (Objects.equals(input.get(0), "goToEndScene")) {
                    goToEndScene(input.get(1));
                }

            }); // end runLater
        }, ipAddress, portNum);

        ClientGUI.client.start();

    } // end connectToServer()



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

    @FXML
    public HBox attemptsHolder;

    @FXML
    public Text category1Attempts;

    @FXML
    public Text category2Attempts;

    @FXML
    public Text category3Attempts;

    @FXML
    public Text instructionsText;


    void setCategoryScene(String cat1, String cat2, String cat3, String num1, String num2, String num3, String visit) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("selectCategoryScene.fxml"));
            selectCategoryRoot = loader.load();

            if (Objects.equals(visit, "new")) {
                connectionRoot.getScene().setRoot(selectCategoryRoot);
            } else {
                Scene tempScene = guessingSceneRoot.getScene();

                tempScene.setRoot(selectCategoryRoot);
            }

            ClientGUIController newController = loader.getController();

            newController.category1Button.setText(cat1);
            newController.category2Button.setText(cat2);
            newController.category3Button.setText(cat3);

            newController.category1Attempts.setText(num1);
            newController.category2Attempts.setText(num2);
            newController.category3Attempts.setText(num3);

            if (Objects.equals(num1, "solved")) {
                newController.category1Button.setDisable(true);
            }
            if (Objects.equals(num2, "solved")) {
                newController.category2Button.setDisable(true);
            }
            if (Objects.equals(num3, "solved")) {
                newController.category3Button.setDisable(true);
            }


        } catch (IOException e){
            System.out.println("Error unable to load fxml file for select category scene");
            e.printStackTrace();

        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    } // end setCategoryScene()


    // event handler for category buttons
    public void sendCategoryChoice(ActionEvent event) {
        Button source = (Button) event.getSource();
        ClientGUI.client.currentCategory = source.getText();
        String id = ((Button) event.getSource()).getId();
        ClientGUI.client.currentCategoryNumber = Integer.parseInt(""+id.charAt(8));
        System.out.println("In gui controller: text is " + source.getText() + " and number is " + id.charAt(8));
    } // end sendCategoryChoice()


// SCENE 3 - guessing letters  ------------------------------------------------

    @FXML
    BorderPane guessingSceneRoot;

    @FXML
    TextField displayGuessState;

    @FXML
    Text incorrectGuessesText;

    @FXML
    TextField incorrectGuessesDisplay;

    @FXML
    Text enterGuessText;

    @FXML
    TextField enterGuessInput;

    @FXML
    Button sendCharacterButton;

    @FXML
    Text errorDisplay;

    @FXML
    Text instructionsText2;


    // initializes the guessing scene
    public void setGuessingScene(String currGuessState) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("guessingScene.fxml"));
            guessingSceneRoot = loader.load();

            selectCategoryRoot.getScene().setRoot(guessingSceneRoot);

            ClientGUIController newController = loader.getController();

            newController.displayGuessState.setText(currGuessState);


        } catch (IOException e){
            System.out.println("Error unable to load fxml file for guessing scene");
            e.printStackTrace();

        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    } // end setGuessingScene()


    // event handler for sendCharacterButton
    public void sendCharacter() {
        String input = enterGuessInput.getText();
        if (input.length() != 1) {
            System.out.println("Enter only one character at a time");
            errorDisplay.setText("Enter only one character at a time");
            errorDisplay.setStyle("-fx-background-color: red");
            enterGuessInput.clear();

        } else {
            errorDisplay.setStyle("-fx-background-color: white");
            errorDisplay.setText("Sending '" + input + "' to server...");
            ClientGUI.client.currentGuess = input.charAt(0);
        }
    } // end sendCharacter()

    public void sendCharacterButtonHandler(ActionEvent e){
        sendCharacter();
    }

    public void keyboardInputHandler(KeyEvent event){
        if(event.getCode().equals(KeyCode.ENTER)) {
            sendCharacter();
        }
    }

    // updates the guessing scene
    public void updateGuessingScene(String currGuessState, String wrongGuesses) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("guessingScene.fxml"));

            Scene tempScene = guessingSceneRoot.getScene();

            guessingSceneRoot = loader.load();

            tempScene.setRoot(guessingSceneRoot);

            ClientGUIController newController = loader.getController();

            newController.displayGuessState.setText(currGuessState);
            newController.incorrectGuessesDisplay.setText(wrongGuesses);


        } catch (IOException e){
            System.out.println("Error unable to load fxml file for guessing scene");
            e.printStackTrace();

        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    } // end updateGuessingScene()


    // does stuff to the guessing scene when guessing is done
    public void resolveGuessingRound(String displayText, String currGuessState, String attempts1, String attempts2, String attempts3) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("guessingScene.fxml"));

            Scene tempScene = guessingSceneRoot.getScene();

            guessingSceneRoot = loader.load();

            tempScene.setRoot(guessingSceneRoot);

            ClientGUIController newController = loader.getController();

            newController.displayGuessState.setText(currGuessState);
            // newController.incorrectGuessesDisplay.setText(wrongGuesses);

            newController.errorDisplay.setText(displayText);
            if (displayText.charAt(0) == 'C') {
                newController.errorDisplay.setStyle("-fx-background-color: #58e065");
            } else if (displayText.charAt(0) == 'S') {
                newController.errorDisplay.setStyle("-fx-background-color: #e04d28");
            }

            newController.sendCharacterButton.setText("Select new category");
            newController.sendCharacterButton.setPrefWidth(200);
            newController.sendCharacterButton.setOnAction(e->ClientGUI.client.returnToCategories = true);


        } catch (IOException e){
            System.out.println("Error unable to load fxml file for guessing scene");
            e.printStackTrace();

        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    } // end resolveGuessingRound()


    // no longer using
    public void returnToCategoryScene(String attempts1, String attempts2, String attempts3) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("selectCategoryScene.fxml"));

            Scene tempScene = guessingSceneRoot.getScene();

            selectCategoryRoot = loader.load();

            tempScene.setRoot(selectCategoryRoot);

            ClientGUIController newController = loader.getController();

//            newController.category1Attempts.setText(attempts1);
//            newController.category2Attempts.setText(attempts2);
//            newController.category3Attempts.setText(attempts3);


        } catch (IOException e){
            System.out.println("Error unable to load fxml file for select category scene");
            e.printStackTrace();

        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    } // end returnToCategoryScene()



// SCENE 3 - end scene  -------------------------------------------------------

    @FXML
    VBox endSceneRoot;

    @FXML
    Text gameResultDisplay;

    @FXML
    Button exitButton;


    public void goToEndScene(String displayText) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("endScene.fxml"));

            Scene tempScene = guessingSceneRoot.getScene();

            endSceneRoot = loader.load();

            tempScene.setRoot(endSceneRoot);

            ClientGUIController newController = loader.getController();

            newController.gameResultDisplay.setText(displayText);

        } catch (IOException e){
            System.out.println("Error unable to load fxml file for end scene");
            e.printStackTrace();

        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    } // end goToEndScene()


    // event handler for exitButton
    public void exit() {
        ClientGUI.client.exit = true;
        Platform.exit();
    }

} // end class
