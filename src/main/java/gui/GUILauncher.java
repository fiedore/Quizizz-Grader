package gui;

import controller.GUIController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.Objects;

public class GUILauncher extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader();
        ClassLoader classLoader = this.getClass().getClassLoader();
        loader.setLocation(classLoader.getResource("GUIconfig.fxml"));
        Parent root = loader.load();

        GUIController controller = loader.getController();
        controller.setMainStage(stage);
        stage.setTitle("Quizizz Grader");
        stage.getIcons().add(new Image(Objects.requireNonNull(classLoader.getResourceAsStream("icon.png"))));
        stage.setResizable(false);
        stage.setScene(new Scene(root));
        stage.show();
    }

    public void run(String[] args) {
        launch(args);
    }
}
