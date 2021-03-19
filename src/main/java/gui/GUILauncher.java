package gui;

import controller.GUIController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class GUILauncher extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(this.getClass().getClassLoader().getResource("GUIconfig.fxml"));
        Parent root = loader.load();

        GUIController controller = loader.getController();
        controller.setMainStage(stage);
        stage.setTitle("Quizizz Grader");
        stage.setResizable(false);
        stage.setScene(new Scene(root));
        stage.show();
    }

    public void run(String[] args) {
        launch(args);
    }
}
