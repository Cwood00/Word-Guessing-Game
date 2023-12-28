import javafx.application.Application;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.control.*;

// Client program for word game
// Runs application thread, and creates listening thread
public class ClientGUI extends Application {
	static ClientThread client;

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		primaryStage.setTitle("Project 3 client");

		Parent connectionSceneRoot = FXMLLoader.load(getClass().getResource("connectionScene.fxml"));

		Scene scene = new Scene(connectionSceneRoot, 700,700);

		primaryStage.setScene(scene);
		primaryStage.show();

		//Ends all currently running threads when the window is closed
		primaryStage.setOnCloseRequest(t -> {
			Platform.exit();
			System.exit(0);
		});
	}

}
