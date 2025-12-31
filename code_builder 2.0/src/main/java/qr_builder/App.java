package qr_builder;

import java.io.File;
import java.io.IOException;

import org.opencv.core.Core;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class App extends Application {
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }
    private static Scene scene;
    
    @Override
    public void start(Stage stage) throws IOException {     
        // Clean temp directory
        final String[] split_dir = System.getProperty("user.dir").split("\\\\"); 
        final String dir = System.getProperty("user.dir") + 
            (split_dir[split_dir.length-1].equals("code_builder 2.0") ? 
             "\\src\\main\\resources\\temp_img" : 
             "\\code_builder 2.0\\src\\main\\resources\\temp_img");
        
        File[] folder = new File(dir).listFiles();
        if (folder != null) {
            for (File file : folder) {
                file.delete();
            }
        }

        // FIXED: Match case of filename
        scene = new Scene(loadFXML("/gui_primary"), 650, 500); // if renamed to lowercase
        // OR: scene = new Scene(loadFXML("/GUI_Primary"), 650, 500); // if keeping uppercase
        
        stage.setMinHeight(500);
        stage.setMinWidth(650);
        stage.setTitle("Scannable Code Generator");
        stage.setScene(scene);

        stage.setOnCloseRequest(event -> {
            GUI_Builder.stop_cam(); 
            stage.close();
            Platform.exit();
            event.consume();
        });

        stage.show();  
    }

    private static AnchorPane loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch();
    }
}