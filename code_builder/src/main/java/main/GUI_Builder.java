/* 
Add fx:controller="main.<controller file name>" to first anchorpane in .fxml
FXML Set both id and fx:id in scenebuilder
Code can only run if .fxml and the controller.java implements the onAction, cannot only one have
Fuck
*/

package main;


import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import com.google.zxing.NotFoundException;
import com.google.zxing.WriterException;

import javafx.animation.PauseTransition;
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
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.util.Duration;

public class GUI_Builder implements Initializable {
    @FXML private Pane qr_option;
    @FXML private TextArea generatetext;    //input and output text
    @FXML private Label download_status;

    @FXML private Button downloadButton;
    @FXML private Button uploadButton;
    @FXML private Button upload_logo;
    @FXML private Button generate_button;

    @FXML private Slider logo_size;
    
    @FXML private ComboBox<String> output_choice;
    @FXML private ComboBox<Integer> correction_choice;

    @FXML private ColorPicker inner_color;
    @FXML private ColorPicker outer_color;

    @FXML private ImageView output_image;
    @FXML private ImageView logo_image;


    @FXML
    private void get_type(ActionEvent event) {
        String output_type = output_choice.getValue();
        System.out.println(output_type);
        if (output_type == "Qr Code") 
            {qr_option.setVisible(true);}
        else 
            {qr_option.setVisible(false);}
    }

    @FXML
    private void onGeneratePush(ActionEvent event) 
                    throws NotFoundException, 
                    WriterException, IOException {
        String user_input = generatetext.getText();
        if (user_input.isBlank()) {return;}
        Color inner = inner_color.getValue();
        Color outer = outer_color.getValue();
        int error_lvl = correction_choice.getValue();
        Make.create_temp_qr(user_input, error_lvl);
        System.out.println(user_input);
        System.out.println(inner + "" + outer);
    }

    @FXML
    private void download_press(ActionEvent event) {

        PauseTransition pause = new PauseTransition(Duration.seconds(3));
        download_status.setVisible(true);
        pause.setOnFinished(EventHandler -> download_status.setVisible(false));
        pause.play();

        System.out.println("Button pressed");
        System.out.println(generatetext.getText());
    }
    
    @FXML
    private void upload_press(ActionEvent event) {

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("Test");
        generatetext.setText("rest");
        correction_choice.getItems().addAll(7,15,25,30);
        output_choice.getItems().addAll("Qr Code", "Barcode");
        output_choice.setValue("Qr Code");
        qr_option.setVisible(true);
        download_status.setVisible(false);


    }
    

}