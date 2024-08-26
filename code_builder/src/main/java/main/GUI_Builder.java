/* 
Add fx:controller="main.<controller file name>" to first anchorpane in .fxml
FXML Set both id and fx:id in scenebuilder
Code can only run if .fxml and the controller.java implements the onAction, cannot only one have
Fuck
*/

package main;

import java.net.URL;
import java.util.ResourceBundle;


import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;

public class GUI_Builder implements Initializable {

    @FXML private TextArea generatetext;    //input and output text
    @FXML private Label download_status;
    @FXML private Button downloadButton;
    @FXML private Button uploadButton;
    @FXML private Button upload_logo;

    @FXML private Slider logo_size;
    
    @FXML private ComboBox<String> output_choice;
    @FXML private ComboBox<Integer> correction_choice;

    @FXML private ColorPicker inner_color;
    @FXML private ColorPicker outer_color;

    @FXML private ImageView output_image;
    @FXML private ImageView logo_image;


    @FXML
    private void get_type(ActionEvent event) {
        System.out.println(output_choice.getValue());
    }

    @FXML
    private void download_press(ActionEvent event) {
        System.out.println("Button pressed");
        System.out.println(generatetext.getText());
        generatetext.clear();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("Test");
        generatetext.setText("rest");
        correction_choice.getItems().addAll(7,15,25,30);
        output_choice.getItems().addAll("Qr Code", "Barcode");

    }
    

}