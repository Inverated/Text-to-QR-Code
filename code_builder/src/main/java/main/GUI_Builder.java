/* 
Add fx:controller="main.<controller file name>" to first anchorpane in .fxml
FXML Set both id and fx:id in scenebuilder
Code can only run if .fxml and the controller.java implements the onAction, cannot only one have
Fuck
*/

package main;


import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;

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
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.util.Duration;

public class GUI_Builder implements Initializable {
    @FXML private Pane qr_option;
    @FXML private TextArea generatetext;    //input and output text

    @FXML private Label download_status;
    @FXML private Label result_handler;
    @FXML private Label bad_result;


    @FXML private Button downloadButton;
    @FXML private Button uploadButton;
    @FXML private Button upload_logo;
    @FXML private Button generate_button;
    @FXML private Button remove_button;

    @FXML private Slider logo_size;
    
    @FXML private ComboBox<String> output_choice;
    @FXML private ComboBox<Integer> correction_choice;

    @FXML private ColorPicker inner_color;
    @FXML private ColorPicker outer_color;

    @FXML private ImageView output_image;
    @FXML private ImageView logo_image;
    @FXML private StackPane output_pane;
    @FXML private StackPane logo_pane;

    private BufferedImage current_image = null;
    private BufferedImage current_logo = null;
    private BufferedImage current_combined = null;


    @FXML
    private void get_type(ActionEvent event) {
        String output_type = output_choice.getValue();
        if (output_type == "Qr Code") {
            qr_option.setVisible(true);
        } else {
            qr_option.setVisible(false);
        }
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
    private void onGeneratePush(ActionEvent event) {
        String user_input = generatetext.getText();
        String output_type = output_choice.getValue();
        result_handler.setVisible(false);
        bad_result.setVisible(false);

        if (user_input.isBlank()) return;
        
        int inner = convert_color(inner_color);
        int outer = convert_color(outer_color);
        int error_lvl = correction_choice.getValue();

        BufferedImage image;
        try {
            image = Make.create_temp(user_input, error_lvl, output_type, inner, outer);
        } catch (WriterException e) {
            result_handler.setVisible(false);
            bad_result.setVisible(true);
            bad_result.setText("Invalid/Excess input for type of code selected.");
            return;
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        for (int i = 0; i < 5; i++) { 
            try {
                Thread.sleep(1000);
                display_generated(image, output_image);
                break;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                }
        }
        current_image = image;
        if (current_logo != null) {
            set_logo();
            return;
        }
        upload_logo.setDisable(false);
        logo_size.setDisable(false);
    }
    
    private void display_generated(BufferedImage image, ImageView location) {
        Image writable = SwingFXUtils.toFXImage(image, null);
        location.setImage(writable);      
    }


    @FXML
    private void download_press(ActionEvent event) throws IOException {
        PauseTransition pause = new PauseTransition(Duration.seconds(3));
        if (current_image == null) {
            bad_result.setVisible(true);
            bad_result.setText("Please generate an image before downloading");
            pause.setOnFinished(EventHandler -> result_handler.setVisible(false));
            pause.play();
            return;
        }
        File filetosave = choose_file(1); 
        if (filetosave == null) return;    
        
        if (current_combined != null) {current_image = current_combined;}
        ImageIO.write(current_image,"PNG",filetosave);
        bad_result.setVisible(false);
        result_handler.setVisible(true);
        result_handler.setText("Image saved at "+filetosave.getAbsolutePath());
        download_status.setVisible(true);

        pause.setOnFinished(EventHandler -> {
            download_status.setVisible(false);
            result_handler.setVisible(false);
        });
        pause.play();
    }
    private File choose_file(int type) {
        FileChooser filechooser = new FileChooser();
        filechooser.setTitle("Save Image");
        filechooser.getExtensionFilters().add(new ExtensionFilter("Image", "*.png", "*.jpeg","*.jpg"));
        File filetosave = (type == 1) ? filechooser.showSaveDialog(null) : filechooser.showOpenDialog(null);

        return filetosave;
    }

    @FXML
    private void remove_logo(ActionEvent event) {
        current_logo = null;
        current_combined = null;
        logo_image.setImage(null);
        display_generated(current_image, output_image);
        remove_button.setDisable(true);

    }

    private void set_logo() {
        BufferedImage current = current_image;
        BufferedImage logo = current_logo;
        double size = logo_size.getValue();
        if (size < 3) {
            display_generated(current, output_image);
            return;
        }

        int current_width = current.getWidth(); int current_height = current.getWidth();
        int new_width = (int) ((current_width/3)*(size/100)); int new_height = (int) ((current_height/3)*(size/100));

        java.awt.Image tmp = logo.getScaledInstance(new_width, new_height, java.awt.Image.SCALE_SMOOTH);
        logo = new BufferedImage(new_width, new_height, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = logo.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose(); 

        BufferedImage combined = new BufferedImage(current.getWidth(), current.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = (Graphics2D) combined.getGraphics();

        graphics.drawImage(current, 0,0,null);
        graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));

        graphics.drawImage(logo,(int) Math.round((current_width/2)-(new_width/2)), (int) Math.round((current_height/2)-(new_width/2)), null);
        display_generated(combined, output_image);
        current_combined = combined;
    }

    @FXML
    private void upload_logo(ActionEvent event) throws FileNotFoundException, IOException {
        File file = choose_file(2);
        if (file == null) {return;} 
        BufferedImage logo = ImageIO.read(new FileInputStream(file));
        if (logo == null) {return;}
        display_generated(logo,logo_image);
        current_logo = logo;
        set_logo();
        remove_button.setDisable(false);
    }

    private static BufferedImage applyThreshold(BufferedImage image, int threshold) {
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
    
    private Result decode_code(BufferedImage coloredImage){
        Map<DecodeHintType, Boolean> hintMap = new HashMap<>();
        hintMap.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
        BufferedImage bufferedImage = new BufferedImage(coloredImage.getWidth(), coloredImage.getHeight(), 10);
        Graphics2D g2d = bufferedImage.createGraphics();
        g2d.drawImage(coloredImage, 0, 0, null);
        g2d.dispose(); 
        display_generated(coloredImage, output_image);

        Result qrCodeResult = null;

        for (int i = 255; i > 0; i-=10) {
            BufferedImage adjusted = applyThreshold(bufferedImage, i);
                    
            BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(
                new BufferedImageLuminanceSource(
                    adjusted)));
        
            try {
                qrCodeResult = new MultiFormatReader().decode(binaryBitmap, hintMap);
            } catch (NotFoundException e) {
                continue;
            }
            return qrCodeResult;
            //display_generated(adjusted);
        }
        return qrCodeResult;
    }
    
    @FXML
    private void upload_press(ActionEvent event) throws IOException {
        result_handler.setVisible(false);
        File file = choose_file(2);
        if (file == null) return; 

        result_handler.setText("Looking or code...");
        result_handler.setVisible(true);

        String[] temp = Read.decode_qr_code(file.getAbsolutePath());   
        String decoded_string = temp[0];

        if (decoded_string != null) {
            BufferedImage coloredImage = ImageIO.read(new File(temp[1]));
            display_generated(coloredImage, output_image);
            result_handler.setText("Code found. Type: Qr Code");

        } /* else {
            BufferedImage coloredImage = ImageIO.read(file);
            Result qrCodeResult = decode_code(coloredImage);

            if (qrCodeResult == null) {
                result_handler.setVisible(false);
                bad_result.setText("Code cannot be detected. Please use another image.");
                bad_result.setVisible(true);
                return;
            }
            BarcodeFormat code_type = qrCodeResult.getBarcodeFormat();
            decoded_string = qrCodeResult.getText();
            result_handler.setText("Code found. Type: "+code_type);
        }
 */
        bad_result.setVisible(false);
        result_handler.setVisible(true);
        String textresult = decoded_string;
        generatetext.setText(textresult); 
    }
               

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //generatetext.setText("rest");
        //inner_color.setValue(Color.WHITE);

        output_choice.getItems().addAll("Qr Code", "Code 39 (Standard Barcode)", "Code 93", "Code 128", "AZTEC", "CODABAR", "Data Matrix", "EAN 13", "EAN 8", "ITF", "MAXICODE", "PDF 417" );
        output_choice.setValue("Qr Code");

        correction_choice.getItems().addAll(7,15,25,30);
        correction_choice.setValue(7);
        
        output_image.fitHeightProperty().bind(output_pane.heightProperty());
        output_image.fitWidthProperty().bind(output_pane.widthProperty());
        logo_image.fitHeightProperty().bind(logo_pane.heightProperty());
        logo_image.fitWidthProperty().bind(logo_pane.widthProperty());

        logo_size.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (current_logo == null) return;
            set_logo();
        });

        

        logo_size.setOnMouseReleased(event -> {
            if (current_combined == null) return;
            Result qrCodeResult = decode_code(current_combined);
            if (qrCodeResult == null) {
                result_handler.setVisible(false);
                bad_result.setText("Code cannot be detected, please try another logo size");
                bad_result.setVisible(true);
            }
            else {
                BarcodeFormat output_type = qrCodeResult.getBarcodeFormat();
                if (dictionary.get(output_type) != output_choice.getValue()) {
                    result_handler.setVisible(false);
                    bad_result.setText("Code detected incorrectly. Use a different logo image/size");
                    bad_result.setVisible(true);
                }
                else {
                    bad_result.setVisible(false);
                    result_handler.setText("Code can be detected with logo. It is safe to download.");
                    result_handler.setVisible(true);
                }
                
            }
        });

    }
    private static Map<BarcodeFormat,String> dictionary = new HashMap<BarcodeFormat, String>();
    static {
        dictionary.put(BarcodeFormat.QR_CODE, "Qr Code");
        dictionary.put(BarcodeFormat.CODE_39, "Code 39 (Standard Barcode)");
        dictionary.put(BarcodeFormat.CODE_93, "Code 93");
        dictionary.put(BarcodeFormat.CODE_128, "Code 128");
        dictionary.put(BarcodeFormat.AZTEC, "AZTEC");
        dictionary.put(BarcodeFormat.CODABAR, "CODABAR");
        dictionary.put(BarcodeFormat.DATA_MATRIX, "Data Matrix");
        dictionary.put(BarcodeFormat.EAN_13, "EAN 13");
        dictionary.put(BarcodeFormat.EAN_8, "EAN 8");
        dictionary.put(BarcodeFormat.ITF, "ITF");
        dictionary.put(BarcodeFormat.MAXICODE, "MAXICODE");
        dictionary.put(BarcodeFormat.PDF_417, "PDF 417");
    }
}