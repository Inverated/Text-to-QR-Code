package qr_builder;

// Java code to generate QR code

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import javafx.scene.paint.Color;


class Write {
    private static Map<EncodeHintType, Object> qr_formatting(Map<EncodeHintType, Object> hashMap, int error_lvl) {
        hashMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
        hashMap.put(EncodeHintType.MARGIN, 1);
        switch (error_lvl) {
            case 7:
                hashMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
                break;
            case 15:
                hashMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
                break;
            case 25:
                hashMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.Q);
                break;
            case 30:
                hashMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
                break;
        }     
        return hashMap;
    }

    private static BufferedImage color_qr(int width, int height, BitMatrix matrix, Color innerColor, Color outerColor) {
        BufferedImage colored = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        byte[] data = ((DataBufferByte) colored.getRaster().getDataBuffer()).getData();  
        //apparently writing to databufferbyte faster than set(x,y)

        if (innerColor == Color.BLACK && outerColor == Color.WHITE) { //Normal black and wite
            return MatrixToImageWriter.toBufferedImage(matrix);
        }

        int innerARGB = colorToARGB(innerColor);
        int outerARGB = colorToARGB(outerColor);

        // Apply colors 
        for (int y = 0; y < matrix.getHeight(); y++) {
            for (int x = 0; x < matrix.getWidth(); x++) {
                boolean pixel = matrix.get(x, y);
                int index = (y * width + x) * 3;

                //??????
                if (pixel) {
                    // Set the inner color (QR code pixel)
                    data[index] = (byte) (innerARGB & 0xFF);             // Blue
                    data[index + 1] = (byte) ((innerARGB >> 8) & 0xFF);  // Green
                    data[index + 2] = (byte) ((innerARGB >> 16) & 0xFF); // Red
                } else {
                    // Set the outer color (background pixel)
                    data[index] = (byte) (outerARGB & 0xFF);             // Blue
                    data[index + 1] = (byte) ((outerARGB >> 8) & 0xFF);  // Green
                    data[index + 2] = (byte) ((outerARGB >> 16) & 0xFF); // Red
                }
            }
        }
        return colored;
    }

    public static int colorToARGB(Color color) { //no fkging clue how it works
        int alpha = (int) (color.getOpacity() * 255) << 24; // Alpha channel
        int red   = (int) (color.getRed()     * 255) << 16;       // Red channel
        int green = (int) (color.getGreen()   * 255) << 8;    // Green channel
        int blue  = (int) (color.getBlue()    * 255);           // Blue channel
        return alpha | red | green | blue;                  // Combine channels
    }
    
    // Function to create the QR code
    public static BufferedImage create_temp(String data, int error_lvl, String output_type, Color inner, Color outer) 
        
    throws WriterException, IOException  {
            //save_temp_img(image);
            int height = 500; int width = 500; 
    
            Map<EncodeHintType, Object> hashMap = new HashMap<>();
    
            BarcodeFormat output_format = dictionary.get(output_type);
            if (output_format == BarcodeFormat.QR_CODE) { //Qr code have additional error correction
                hashMap = qr_formatting(hashMap, error_lvl);
            } 

            BitMatrix matrix = new MultiFormatWriter().encode(
                new String(data.getBytes("UTF-8"), "UTF-8"),
                output_format, width, height, hashMap);
            
            BufferedImage  image = color_qr(width,height, matrix, inner, outer);
                
            return image;
        }
      
    @SuppressWarnings("unused")
    private static void save_temp_img(BufferedImage image) throws IOException {
        // The path where the image will get saved
        long time = System.currentTimeMillis();
        String file_name = time + ".png";

        String path = System.getProperty("user.dir");
        String[] temp = path.split("\\\\");
        if (temp[temp.length-1].equals("code_builder 2.0")) {
            path += "\\src\\main\\resources\\temp_img\\" + file_name;
        } else {
            path += "\\code_builder 2.0\\src\\main\\resources\\temp_img\\" + file_name;
        }

        File file = new File(path);
        ImageIO.write(image,"PNG",file);
    }

    private static Map<String,BarcodeFormat> dictionary = new HashMap<>();
    static {
        dictionary.put("Qr Code", BarcodeFormat.QR_CODE);
        dictionary.put("Code 39 (Standard Barcode)", BarcodeFormat.CODE_39);
        dictionary.put("Code 93", BarcodeFormat.CODE_93);
        dictionary.put("Code 128", BarcodeFormat.CODE_128);
        dictionary.put("AZTEC", BarcodeFormat.AZTEC);
        dictionary.put("CODABAR", BarcodeFormat.CODABAR);
        dictionary.put("Data Matrix", BarcodeFormat.DATA_MATRIX);
        dictionary.put("EAN 13", BarcodeFormat.EAN_13);
        dictionary.put("EAN 8", BarcodeFormat.EAN_8);
        dictionary.put("ITF", BarcodeFormat.ITF);
        dictionary.put("PDF 417", BarcodeFormat.PDF_417);
        dictionary.put("UPC A", BarcodeFormat.UPC_A);
        dictionary.put("UPC E", BarcodeFormat.UPC_E);
        dictionary.put("UPC EAN Extension (Not supported)", BarcodeFormat.UPC_EAN_EXTENSION);
        dictionary.put("MAXICODE (Not supported)", BarcodeFormat.MAXICODE);
        dictionary.put("RSS 14 (Not supported)", BarcodeFormat.RSS_14);
        dictionary.put("RSS Expanded (Not supported)", BarcodeFormat.RSS_EXPANDED);
    }
    
}
