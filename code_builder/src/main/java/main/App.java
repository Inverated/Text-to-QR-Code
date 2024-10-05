package main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.File;

/**
 * JavaFX App
 */
public class App extends Application {

    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        String dir = System.getProperty("user.dir");
        String[] temp = dir.split("\\\\");
        if (temp[temp.length-1].equals("code_builder")) dir += "\\src\\main\\resources\\temp_img";
        else dir += "\\code_builder\\src\\main\\resources\\temp_img";
        
        File[] folder = new File(dir).listFiles();
        for (File file : folder) {
            //System.out.println(file.getName());
            file.delete();
        }

        scene = new Scene(loadFXML("Gui_Primary"), 650, 500);
        stage.setMinHeight(500);
        stage.setMinWidth(650);
        stage.setTitle("Scannable Code Generator");
        stage.setScene(scene);
        stage.show();
    }

    static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch();
    }

}