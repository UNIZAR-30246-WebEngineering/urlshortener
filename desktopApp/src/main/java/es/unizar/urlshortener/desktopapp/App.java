package es.unizar.urlshortener.desktopapp;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {
    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("app-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 800, 700);
        App.primaryStage = stage;
        stage.setTitle("urlShortener");
        stage.setScene(scene);
        stage.show();
    }

    public static Stage getStage() {
        return App.primaryStage;
    }

    public static void main(String[] args) {
        launch();
    }
}
