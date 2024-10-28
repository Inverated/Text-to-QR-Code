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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ResourceBundle;

import javax.imageio.ImageIO;

import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
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
    @FXML private Label cam_result;

    @FXML private Button downloadButton;
    @FXML private Button uploadButton;
    @FXML private Button cam_butt;
    @FXML private Button upload_logo;
    @FXML private Button generate_button;
    @FXML private Button remove_button;

    @FXML private Slider logo_size;
    
    @FXML private ComboBox<String> output_choice;
    @FXML private ComboBox<Integer> correction_choice;

    @FXML private ColorPicker inner_color;
    @FXML private ColorPicker outer_color;

    @FXML private ContextMenu cam_click; 

    @FXML private ImageView output_image;
    @FXML private ImageView logo_image;
    @FXML private StackPane output_pane;
    @FXML private StackPane logo_pane;

    private BufferedImage current_image = null;
    private BufferedImage current_logo = null;
    private BufferedImage current_combined = null;

    PauseTransition pause = new PauseTransition(Duration.seconds(3));

    private static VideoCapture capture = null;
    private static VideoCapture test_capture = null;

    private int fps = 0;
    private final int fps_max = 10;
    private static boolean capturing = false;
    private boolean download_warning = false;
    private static int selected_cam = 0; //default val 0
    private static Thread cam_thread, process_thread;

    private void set_result(String res) {
        Platform.runLater(()-> {
            result_handler.setText(res);
            result_handler.setTextFill(Color.BLACK);
            result_handler.setVisible(true);
        });
    }

    public void raise_error(String e) {
        Platform.runLater(() -> {
            result_handler.setText(e);
            result_handler.setTextFill(Color.RED);
            result_handler.setVisible(true);
            pause.setOnFinished(_ -> {
                result_handler.setVisible(false);
            });
            pause.play();
        });
    }
    
    public void raise_error(IllegalArgumentException e) {
        Platform.runLater(() -> {
            result_handler.setText(e.toString().split(":")[1]);
            result_handler.setTextFill(Color.RED);
            result_handler.setVisible(true);
            pause.setOnFinished(_ -> {
                result_handler.setVisible(false);
            });
            pause.play();
        });
    }
     

    public static void stop_cam() {
        if (capture != null) {
            capture.release();
        }
        capturing = false;
        if (process_thread != null) process_thread.interrupt();
        if (cam_thread != null) cam_thread.interrupt();
    }

    public static void stop_cam(VideoCapture test_capture) {
        if (test_capture != null) {
            test_capture.release();
        }
    }

    private void display_generated(BufferedImage image, ImageView location) {
        Image writable = SwingFXUtils.toFXImage(image, null);
        location.setImage(writable);      
    }

    @FXML
    private void get_type(ActionEvent event) {
        String output_type = output_choice.getValue();
        if (output_type == "Qr Code") {
            qr_option.setVisible(true);
        } else {
            current_logo = null;
            current_combined = null;
            logo_image.setImage(null);
            remove_button.setDisable(true);
            qr_option.setVisible(false);
        }
    }

    @FXML
    private void onGeneratePush(ActionEvent event) {
        capturing = false;
        stop_cam();
        cam_butt.setText("Open Camera");
        String user_input = generatetext.getText();
        String output_type = output_choice.getValue();
        result_handler.setVisible(false);

        if (user_input.isBlank()) return;
        
        int error_lvl = correction_choice.getValue();

        BufferedImage image;
        try {
            image = Write.create_temp(user_input, error_lvl, output_type, inner_color.getValue(), outer_color.getValue());
        } catch (WriterException e) {
            set_result("Invalid input for type of code selected.");
            return;
        } catch (IOException e) {
            e.printStackTrace();
            return;
        } catch (IllegalArgumentException e) {
            raise_error(e); //code type requirement wrong
            return;
        }
        for (int i = 0; i < 5; i++) { 
            try {
                Thread.sleep(1000); //buffered image might take time to render
                display_generated(image, output_image);
                break;
            } catch (InterruptedException e) {
                raise_error(e.toString());
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

    private void choose_cam() {
        cam_click.getItems().clear();
        boolean found = false;

        for (int i = 0; i < 10; i++) {
            if (i == selected_cam) {
                cam_click.getItems().add(new MenuItem("Camera " + (i+1)));
                continue;
            }
            try {
                test_capture = new VideoCapture(i);
                if (!test_capture.read(new Mat())) {
                    break;
                }
                found = true;
                final int chosen = i;
                MenuItem item = new MenuItem("Camera " + (i+1));

                item.setOnAction(event -> {
                    selected_cam = chosen;  
                    cam_result.setText("Camera " + (selected_cam+1) + " selected");
                    if (capturing) {
                        stop_cam();
                        process_thread.interrupt();
                        cam_thread.interrupt();
                        run_cam();
                    }
                    event.consume();
                });
                cam_click.getItems().add(item);

            } catch (Exception e) {
                continue;
            }
        }
        stop_cam(test_capture);
        if (found) {
            cam_result.setText("Camera " + (selected_cam+1) + " selected");
        } else {
            cam_result.setText("No Camera Detected");
        }
    }

    @FXML
    private void cam_toggle(ActionEvent event) { //show opencv cam in output_image (image view)
        set_result("");
        if (cam_butt.getText().equals("Close Camera")) {
            stop_cam();
            process_thread.interrupt();
            cam_thread.interrupt();
            cam_butt.setText("Open Camera");
            return;
        }
        run_cam();
    }

    private void run_cam() {
        capture = new VideoCapture(selected_cam); 
        cam_result.setText("Camera " + (selected_cam+1) + " selected");
        
        if (!capture.isOpened()) {
            raise_error("Unable to open camera");
            return;
        }
        if (!capture.read(new Mat())) {
            raise_error("Camera is opened in another app");
            return;
        }

        cam_butt.setText("Close Camera");
        capturing = true;

        // Create a separate thread for video capture to avoid freezing the UI
        cam_thread = new Thread(this::start_capture);
        cam_thread.start(); 
    }

    private void start_capture() {
        Mat frame = new Mat();
        while (capturing) {
            try {
                if (!capture.read(frame)) {
                    continue;
                }
            } catch (Exception e) {
                stop_cam();
                process_thread.interrupt();
                cam_thread.interrupt();
                return;
            }

            if (frame.width() < 1) {
                continue;
            }
            BufferedImage bufferedimg = Read.MatToBuff(frame);
        
            Platform.runLater(() -> display_generated(bufferedimg, output_image)); //Show video captured by opencv
            fps++;

            if (fps == fps_max) {
                process_thread = new Thread(() -> { //new thread to process img while still showing video
                    if (capturing) {
                        String[] result = process_img(frame);
                        if (result != null) {
                            Platform.runLater(() -> {       //bring out interaction with fx to run in platform.runlater,  thread cannot interact directly
                                generatetext.setText(result[0]); 
                                set_result("Code found. Type: " + result[1]);
                                output_choice.setValue(result[1]);
                                cam_butt.setText("Open Camera");
                            });
                        }
                        process_thread.interrupt();
                    }
                });
                process_thread.start();
            }
        }
        stop_cam();
        process_thread.interrupt();
        cam_thread.interrupt();
        Platform.runLater(() -> cam_butt.setText("Open Camera"));
    }

    private String[] process_img(Mat frame) {
        fps = 0;
        String[] result = Read.decode_qr_code(frame);
        if (result == null) return null;

        result[1] = dictionary.get(BarcodeFormat.valueOf(result[1]));
        stop_cam();
        process_thread.interrupt();
        cam_thread.interrupt();

        return result;
    }

    @FXML
    private void download_press(ActionEvent event) throws IOException {
        if (current_image == null) {
            raise_error("Please generate an image before downloading");
            return;
        }

        String[] result;
        if (current_combined != null) {
            result = Read.decode_qr_code(current_combined);
        } else {
            result = Read.decode_qr_code(current_image);
        }

        if (!download_warning) { //check if code is readable
            if (result == null) {
                raise_error("Please try another logo/color. Download again to ignore");
                download_warning = true;
                return;
            }      
            if (!dictionary.get(BarcodeFormat.valueOf(result[1])).equals( output_choice.getValue() )) {
                raise_error("Format different from selected. Download again to ignore");
                download_warning = true;
                return;
            }
        }

        download_warning = false;
        set_result("");

        File filetosave = choose_file(1); 
        if (filetosave == null) return;    
        
        if (current_combined != null) {
            current_image = current_combined;
        }
        ImageIO.write(current_image,"PNG",filetosave);

        result_handler.setVisible(false);
        set_result("Image saved at "+filetosave.getAbsolutePath());
        download_status.setVisible(true);

        pause.setDuration(Duration.seconds(3));
        pause.setOnFinished(_ -> {
            download_status.setVisible(false);
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
        int new_width   = (int) ((current_width /3) *(size/100)); 
        int new_height  = (int) ((current_height/3) *(size/100));

        java.awt.Image tmp = logo.getScaledInstance(new_width, new_height, java.awt.Image.SCALE_SMOOTH);
        logo = new BufferedImage(new_width, new_height, BufferedImage.TYPE_3BYTE_BGR);

        Graphics2D g2d = logo.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose(); 

        BufferedImage combined = new BufferedImage(current.getWidth(), current.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
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
        if (file == null) return;
        BufferedImage logo = ImageIO.read(new FileInputStream(file));
        if (logo == null) return;
        display_generated(logo,logo_image);
        current_logo = logo;
        set_logo();
        remove_button.setDisable(false);
    }
    
    @FXML
    private void upload_press(ActionEvent event) throws IOException {
        capturing = false;
        stop_cam();
        cam_butt.setText("Open Camera");
        result_handler.setVisible(false);
        File file = choose_file(2);
        if (file == null) return; 

        set_result("Looking for code...");

        BufferedImage coloredImage = ImageIO.read(file);
        display_generated(coloredImage, output_image);

        String[] result = Read.decode_qr_code(file.getAbsolutePath());   

        if (result == null) {
            raise_error("Code not detected");
            return;
        } 

        String output_type = dictionary.get(BarcodeFormat.valueOf(result[1]));
        set_result("Code found. Type: " + output_type);
        output_choice.setValue(output_type);

        generatetext.setText(result[0]); 
    }
               

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        output_choice.getItems().addAll(dictionary.values());
        output_choice.setValue("Qr Code");

        correction_choice.getItems().addAll(7,15,25,30);
        correction_choice.setValue(7);
        
        output_image.fitHeightProperty().bind(output_pane.heightProperty());
        output_image.fitWidthProperty().bind(output_pane.widthProperty());
        logo_image.fitHeightProperty().bind(logo_pane.heightProperty());
        logo_image.fitWidthProperty().bind(logo_pane.widthProperty());


        choose_cam();
        cam_butt.setOnMousePressed(event -> {
            if (event.isSecondaryButtonDown()) {
                choose_cam();
                cam_click.show(cam_butt, event.getScreenX(), event.getScreenY());
            }

        });

        logo_size.valueProperty().addListener(_ -> {
            if (current_logo == null) return;
            set_logo();
        });

        logo_size.setOnMouseReleased(event -> {  //try to read code when logo size changed
            if (current_combined == null) return;

            String[] result = Read.decode_qr_code(current_combined);
            if (result == null) {
                raise_error("Code cannot be detected, please try another logo size");
            }
            else {
                String output_type = result[1];
                if (dictionary.get(BarcodeFormat.valueOf(output_type)) != output_choice.getValue()) {
                    raise_error("Code detected incorrectly. Use a different logo image/size");
                }
                else {
                    set_result("Code can be detected with logo. It is safe to download.");
                }     
            }
            event.consume();
        });

    }
    private Map<BarcodeFormat,String> dictionary = new LinkedHashMap<BarcodeFormat, String>();
    {
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
        dictionary.put(BarcodeFormat.PDF_417, "PDF 417");
        dictionary.put(BarcodeFormat.UPC_A, "UPC A");
        dictionary.put(BarcodeFormat.UPC_E, "UPC E");
        dictionary.put(BarcodeFormat.UPC_EAN_EXTENSION, "UPC EAN Extension (Not supported)");
        dictionary.put(BarcodeFormat.MAXICODE, "MAXICODE (Not supported)");
        dictionary.put(BarcodeFormat.RSS_14, "RSS 14 (Not supported)");
        dictionary.put(BarcodeFormat.RSS_EXPANDED, "RSS Expanded (Not supported)");
    }
}