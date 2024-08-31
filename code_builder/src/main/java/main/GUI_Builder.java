/* 
Add fx:controller="main.<controller file name>" to first anchorpane in .fxml
FXML Set both id and fx:id in scenebuilder
Code can only run if .fxml and the controller.java implements the onAction, cannot only one have
Fuck
*/

package main;


import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.DecoderResult;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import javafx.animation.PauseTransition;
import javafx.embed.swing.SwingFXUtils;
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
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.util.Duration;

public class GUI_Builder implements Initializable {
    @FXML private AnchorPane main_pane; 
    @FXML private Pane qr_option;
    @FXML private TextArea generatetext;    //input and output text

    @FXML private Label download_status;
    @FXML private Label no_image;
    @FXML private Label bad_image;
    @FXML private Label download_finish;


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

    private BufferedImage current_image = null;

    @FXML
    private void get_type(ActionEvent event) {
        String output_type = output_choice.getValue();
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
        download_finish.setVisible(false);
        bad_image.setVisible(false);

        if (user_input.isBlank()) {return;}
        
        int inner = convert_color(inner_color);
        int outer = convert_color(outer_color);
        int error_lvl = correction_choice.getValue();

        BufferedImage image = Make.create_temp(user_input, error_lvl, output_type, inner, outer);
        for (int i = 0; i < 5; i++) { 
            try {
                Thread.sleep(1000);
                display_generated(image);
                break;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                }
        }
        current_image = image;
    }
    
    private void display_generated(BufferedImage image) {
        Image writable = SwingFXUtils.toFXImage(image, null);
        output_image.setImage(writable);      
    }


    @FXML
    private void download_press(ActionEvent event) throws IOException {
        PauseTransition pause = new PauseTransition(Duration.seconds(3));
        if (current_image == null) {
            no_image.setVisible(true);
            pause.setOnFinished(EventHandler -> no_image.setVisible(false));
            pause.play();
            return;
        }
        File filetosave = choose_file(1); 
        if (filetosave == null) {return;}       
        ImageIO.write(current_image,"PNG",filetosave);
        download_finish.setVisible(true);
        download_finish.setText("Image saved at "+filetosave.getAbsolutePath());

        download_status.setVisible(true);
        pause.setOnFinished(EventHandler -> download_status.setVisible(false));
        pause.play();
    }
    private File choose_file(int type) {
        FileChooser filechooser = new FileChooser();
        filechooser.setTitle("Save Image");
        filechooser.getExtensionFilters().add(new ExtensionFilter("Image", "*.png", "*.jpeg","*.jpg"));
        File filetosave = (type == 1) ? filechooser.showSaveDialog(null) : filechooser.showOpenDialog(null);

        return filetosave;

    }

    public static BufferedImage applyThreshold(BufferedImage image, int threshold) {
        BufferedImage thresholdedImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_BINARY);
        
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int rgb = image.getRGB(x, y);
                int red = (rgb >> 16) & 0xFF;
                int green = (rgb >> 8) & 0xFF;
                int blue = rgb & 0xFF;
                int gray = (red + green + blue) / 3; // Convert to grayscale
                
                // Apply the threshold
                int binaryColor = gray > threshold ? 0xFFFFFF : 0x000000;
                
                thresholdedImage.setRGB(x, y, binaryColor);
            }
        }
        
        return thresholdedImage;
    }
    
    
    @FXML
    private void upload_press(ActionEvent event) throws IOException {
        bad_image.setVisible(false);
        File file = choose_file(2);
        if (file == null) {return;}  
        System.out.println(file.getAbsolutePath());

        Map<DecodeHintType, Boolean> hintMap = new HashMap<>();
    hintMap.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
        BufferedImage coloredImage = ImageIO.read(new FileInputStream(file));
        display_generated(coloredImage);
        BufferedImage bufferedImage = new BufferedImage(coloredImage.getWidth(), coloredImage.getHeight(), coloredImage.TYPE_BYTE_GRAY);
        Graphics2D g2d = bufferedImage.createGraphics();
        g2d.drawImage(coloredImage, 0, 0, null);
        g2d.dispose();

        Result qrCodeResult = null;

        for (int i = 255; i > 0; i-=10) {
            BufferedImage adjusted = applyThreshold(bufferedImage, i);
                    
            BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(
                new BufferedImageLuminanceSource(
                    adjusted)));
        
            try {
                qrCodeResult = new MultiFormatReader().decode(binaryBitmap, hintMap);
            } catch (NotFoundException e) {
                System.out.println(i);
                continue;
            }
            String textresult = qrCodeResult.getText();
            generatetext.setText(textresult);
            System.out.println(textresult);
            display_generated(adjusted);
        }
            if (qrCodeResult == null){bad_image.setVisible(true);}
        }
               

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        generatetext.setText("rest");

        output_choice.getItems().addAll("Qr Code", "Barcode");
        output_choice.setValue("Qr Code");

        correction_choice.getItems().addAll(7,15,25,30);
        correction_choice.setValue(7);

        inner_color.setValue(Color.WHITE);
        
        output_image.fitHeightProperty().bind(output_pane.heightProperty());
        output_image.fitWidthProperty().bind(output_pane.widthProperty());
    }
}