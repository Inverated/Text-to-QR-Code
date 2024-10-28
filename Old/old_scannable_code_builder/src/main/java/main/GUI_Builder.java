package main;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;

public class GUI_Builder implements Initializable {
    
    @FXML
    private ChoiceBox<String> output_choice;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("test");
        output_choice.getItems().addAll("Dog","Shir","sdf");
    }

    
}
