package main;

// Java code to generate QR code

import java.awt.image.BufferedImage;
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


public class Make {
    public static Map<EncodeHintType, Object> qr_formatting(Map<EncodeHintType, Object> hashMap, int error_lvl) {
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

    public static BufferedImage color_qr(
        int width, int height, BitMatrix matrix,
        int inner, int outer) 
    {
        BufferedImage colored = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_INDEXED);
        if (inner == -16777216 && outer == -1) {return MatrixToImageWriter.toBufferedImage(matrix);}
        // Apply colors 
        for (int y = 0; y < matrix.getHeight(); y++) {
            for (int x = 0; x < matrix.getWidth(); x++) {
                boolean pixel = matrix.get(x, y);
                if (pixel == true) {
                    colored.setRGB(x, y, inner);
                } else {
                    colored.setRGB(x, y, outer);
                }
            }
        }
        return colored;
    }

    private static Map<String,BarcodeFormat> dictionary = new HashMap<>();;
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
        dictionary.put("MAXICODE", BarcodeFormat.MAXICODE);
        dictionary.put("PDF 417", BarcodeFormat.PDF_417);
    }
    
    // Function to create the QR code
    public static BufferedImage create(String data, String path,
                                String charset, int error_lvl,
                                String output_type, int inner, int outer)
        throws WriterException, IOException
    {
        int height = 500; int width = 500; 

        Map<EncodeHintType, Object> hashMap = new HashMap<>();

        BarcodeFormat output_format = dictionary.get(output_type);

        if (output_format == BarcodeFormat.QR_CODE) {
            hashMap = qr_formatting(hashMap, error_lvl);
        } 

        BitMatrix matrix = new MultiFormatWriter().encode(
            new String(data.getBytes(charset), charset),
            output_format, width, height, hashMap);
        
        
        BufferedImage  image = color_qr(width,height, matrix, inner, outer);
        
        File file = new File(path);
        ImageIO.write(image,"PNG",file);

        return image;
        }
  
    // Driver code
    public static BufferedImage create_temp(String data, int error_lvl, String output_type, int inner, int outer)
        throws WriterException, IOException             
    {   
        // The path where the image will get saved
        long time = System.currentTimeMillis();
        String file_name = time + ".png";

        String path = System.getProperty("user.dir");
        String[] temp = path.split("\\\\");
        if (temp[temp.length-1].equals("code_builder")) path += "\\src\\main\\resources\\temp_img\\" + file_name;
        else path += "\\code_builder\\src\\main\\resources\\temp_img\\" + file_name;
        
        // Encoding charset
        String charset = "UTF-8";

        // Create the QR code and save in temp folder as a jpg file
        BufferedImage image = create(data, path, charset, error_lvl,  output_type, inner, outer);
        //System.out.println("QR Code Generated!!! ");
        return image;
    }
}
