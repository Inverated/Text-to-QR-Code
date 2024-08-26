package main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {
    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("GUI_Builder.fxml"));
        scene = new Scene(fxmlLoader.load(), 640, 480);
        
        stage.setMinHeight(500);
        stage.setMinWidth(600);
        stage.setTitle("Scannable Code Generator");
        stage.setScene(scene);
        stage.show();
    }


    public static void main(String[] args) {
        Application.launch(App.class,args);
    }
    
}