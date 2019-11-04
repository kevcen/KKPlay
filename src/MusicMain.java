package musicplayer;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class MusicMain extends Application {

	@Override
	public void start(Stage primaryStage) {
		AnchorPane root = null;
		try {
			root = FXMLLoader.load(getClass().getResource("MusicUI.fxml"));

			Scene scene = new Scene(root);
			scene.setFill(null);
			primaryStage.setScene(scene);
			primaryStage.initStyle(StageStyle.TRANSPARENT);
			primaryStage.setAlwaysOnTop(true);
			primaryStage.show();
		} catch (IOException e) {e.printStackTrace();}
	}

	public static void main(String[] args) {
		launch(args);
	}

}
