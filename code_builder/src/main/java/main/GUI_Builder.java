/* 
Add fx:controller="main.<controller file name>" to first anchorpane in .fxml
FXML Set both id and fx:id in scenebuilder
Code can only run if .fxml and the controller.java implements the onAction, cannot only one have
Fuck
*/

package main;

import java.io.IOException;
import java.io.InputStream;
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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
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
    @FXML private StackPane output_pane;
    @FXML private BorderPane logo_pane;

    @FXML
    private void get_type(ActionEvent event) {
        String output_type = output_choice.getValue();
        System.out.println(output_type);
        if (output_type == "Qr Code") 
            {qr_option.setVisible(true);}
        else 
            {qr_option.setVisible(false);}
    }

    private int convert_color(ColorPicker picker){
        Color color = picker.getValue();
        int r = (int) Math.round(color.getRed()*255);
        int g = (int) Math.round(color.getGreen()*255);
        int b = (int) Math.round(color.getBlue()*255);
        r = (r << 16) & 0x00FF0000;
        g = (g << 8) & 0x0000FF00;
        b = b & 0x000000FF;
        return 0xFF000000 | r | g | b;
    }

    @FXML
    private void onGeneratePush(ActionEvent event) 
                    throws NotFoundException, 
                    WriterException, IOException {
        String user_input = generatetext.getText();
        String output_type = output_choice.getValue();

        if (user_input.isBlank()) {return;}

        int inner = convert_color(inner_color);
        int outer = convert_color(outer_color);

        int error_lvl = correction_choice.getValue();

        String file_name = Make.create_temp(user_input, error_lvl, output_type, inner, outer);
        for (int i = 0; i < 5; i++) { 
            try {
                Thread.sleep(1000);
                display_generated(file_name);
                break;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                }
        }
        
    }
    

    private void display_generated(String file_name) {
        String path = "/temp_img/" + file_name;
            InputStream instream = getClass().getResourceAsStream(path);
            Image generated = new Image(instream);
            output_image.setImage(generated);      
        
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
        generatetext.setText("rest");

        output_choice.getItems().addAll("Qr Code", "Barcode");
        output_choice.setValue("Qr Code");

        correction_choice.getItems().addAll(7,15,25,30);
        correction_choice.setValue(7);

        download_status.setVisible(false);

        output_image.fitHeightProperty().bind(output_pane.heightProperty());
        output_image.fitWidthProperty().bind(output_pane.widthProperty());
    }
}