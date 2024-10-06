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
    private final String[] split_dir = System.getProperty("user.dir").split("\\\\"); 
    private final String dir = System.getProperty("user.dir") + (split_dir[split_dir.length-1].equals("code_builder") ? "\\src\\main\\resources\\temp_img" : "\\code_builder\\src\\main\\resources\\temp_img");

    @Override
    public void start(Stage stage) throws IOException {        
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