//Chris Wood
//Ana Theys
//Server program for word game

import javafx.application.Application;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;


public class ServerGUI extends Application {
	static Server theServer;

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		primaryStage.setTitle("Project 3 server");

		Parent root = FXMLLoader.load(getClass().getResource("ServerSelectPortNumberScene.fxml"));
	     
		Scene scene = new Scene(root, 700,700);

		scene.getStylesheets().add("ServerSelectPortNumberSceneStyle.css");

		primaryStage.setScene(scene);
		primaryStage.show();


		primaryStage.setOnCloseRequest(t -> {
            Platform.exit();
            System.exit(0);
        });
	}
}
