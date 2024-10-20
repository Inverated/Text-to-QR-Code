package main;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.QRCodeDetector;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;


public class Read {
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME); // Load the OpenCV native library
    }
    private static ArrayList<String[]> confidence = new ArrayList<>();

    private final static String[] split_dir = System.getProperty("user.dir").split("\\\\"); 
    private final static String temp_dir = System.getProperty("user.dir") + (split_dir[split_dir.length-1].equals("code_builder") ? "\\src\\main\\resources\\temp_img" : "\\code_builder\\src\\main\\resources\\temp_img");


    public static String[] decode_qr_code(String path) {
        return decoding(Imgcodecs.imread(path));
    }
    public static String[] decode_qr_code(Mat image) {
        return decoding(image);
    }
    public static String[] decode_qr_code(BufferedImage image) {
        return decoding(buffToMat(image));
    }

    private static String[] decoding(Mat image) {
        Imgcodecs.imwrite(temp_dir+"/plain.png", image);

        //Mat annotated = image.clone();

        Mat gray = new Mat();
        Imgproc.cvtColor(image, gray, Imgproc.COLOR_BGR2GRAY);

        int border = 15;
        Core.copyMakeBorder(gray, gray, border, border, border, border, Core.BORDER_CONSTANT, new  Scalar(255, 255, 255));

        Imgcodecs.imwrite(temp_dir+"/gray.png", gray);

        String[] result = decode(gray);
        if (result != null) {
            add_confidence(result);
        }

        Mat blackAndWhiteImage = new Mat();
        Imgproc.threshold(gray, blackAndWhiteImage, 170, 255, Imgproc.THRESH_BINARY);
        //Imgcodecs.imwrite(temp_dir+"/black and wite.png", blackAndWhiteImage);

        Mat edges = new Mat();
        Imgproc.Canny(blackAndWhiteImage, edges, 0, 300);
        //Imgcodecs.imwrite(temp_dir+"/edges.png", edges);

        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(edges, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

        ArrayList<Point> points = new ArrayList<>();

        // Approximate contours to polygons and get the bounding rectangles
        for (MatOfPoint contour : contours) {
            MatOfPoint2f contour2f = new MatOfPoint2f(contour.toArray());
            double perimeter = Imgproc.arcLength(contour2f, true);

            // Approximate contour to polygon
            MatOfPoint2f approx = new MatOfPoint2f();
            Imgproc.approxPolyDP(contour2f, approx, 0.02 * perimeter, true);

            // If the approximated polygon has 4 points, assume it's the slanted plane
            if (approx.total() == 4) {
                // Convert MatOfPoint2f to List<Point>
                points = new ArrayList<>(approx.toList());
                int len = points.size();

                //Exclude very small boxes 
                if (    Math.abs(points.get(0).x-points.get(1).x) + (Math.abs(points.get(2).x-points.get(3).x)) < 3 |
                        Math.abs(points.get(0).y-points.get(1).y) + (Math.abs(points.get(2).y-points.get(3).y)) < 3) {
                    continue;
                }

                boolean exit=false;
                double min_len = image.rows(); //for later to cal ratio
                //lets do meth, dot product to get angle fuk
                for (int i = 0; i < 4; i++) {
                    double[] A = {points.get(i).x - points.get((i+1)%4).x, points.get(i).y - points.get((i+1)%4).y};
                    double[] B = {points.get((i+2)%4).x - points.get((i+1)%4).x, points.get((i+2)%4).y - points.get((i+1)%4).y};
                    double cos_angle = (A[0]*B[0] + A[1]*B[1])/     // a.b 
                                        (Math.sqrt((A[0]*A[0] + A[1]*A[1]) * (B[0]*B[0] + B[1]*B[1])) // |a||b|
                    );

                    double angle = (180*Math.acos(cos_angle))/Math.PI;
                    if (angle > 160 || angle < 30) {
                        exit = true;
                        break;
                    }

                    min_len = Math.min(Math.sqrt(A[0]*A[0] + A[1]*A[1]), min_len);
                    double ratio = (A[0]*A[0] + A[1]*A[1])/(B[0]*B[0] + B[1]*B[1]); //exclude loooong rectangle
                    if (ratio > 16 || ratio < 0.0625) {
                        exit = true;
                        break;
                    }
                }
                if (exit) continue;


                /* System.out.println();
                for (Point p : points) {
                    System.out.println(p.x + " " + p.y);
                } */

                /* draw(points, annotated, new Scalar(0,255,0));
                Imgproc.putText(annotated, points.get(0).x+" "+points.get(0).y, points.get(0), 1, 1, new Scalar(255,100,200));
 */
                double cenx = (points.get(0).x + points.get(1).x + points.get(2).x + points.get(3).x) / len;
                double ceny = (points.get(0).y + points.get(1).y + points.get(2).y + points.get(3).y) / len;

                Point center = new Point(cenx, ceny);

                double ratio = Math.max(image.rows()/min_len, image.cols()/min_len);
                //System.out.println(min_len);
                // Scale each point relative to the center
                for (int i=0; i<len; i++) {
                    Point p = points.get(i);
                    double scaledX = center.x + (p.x - cenx) * ratio;
                    double scaledY = center.y + (p.y - ceny) * ratio;                
                    p.set(new double[]{scaledX, scaledY});
                }

                /* draw(points, annotated, new Scalar(255,0,0));
                Imgcodecs.imwrite(temp_dir+"/annotated.png", annotated);
                */
                Mat transformed = transform_image(points, gray);        //make sure image grey to feed into decode
                //Imgcodecs.imwrite(temp_dir + "/Transformed.png", transformed);

                result = decode(transformed);
                if (result != null) {
                    add_confidence(result);
                }
                if (confidence.size() > 5) {
                    break;
                } 
            }
        }
        if (confidence.size() == 0) {
            return null;
        } else {
            confidence.sort((arr1, arr2) -> arr2[2].compareTo(arr1[2]));
            String[] output = {confidence.get(0)[0], confidence.get(0)[1]};
            confidence = new ArrayList<>(); //Stupid method needing static so need reset manually
            return output;
        }
    }

    private static void add_confidence(String[] result) {
        boolean added = false;
        for (String[] stuff : confidence) {
            if (stuff[0].equals(result[0])) {
                stuff[2] = String.valueOf( Integer.parseInt(stuff[2]) + 1 );
                added = true;
                break;
            }
        }
        if (!added) {
            confidence.add(new String[]{result[0], result[1], "1"});
        }
    }

    private static String[] decode(Mat image) {
        QRCodeDetector qrdetector = new QRCodeDetector();
        String decoded = qrdetector.detectAndDecode(image);
        if (!decoded.isEmpty()) {
            return new String[]{decoded, "QR_CODE"};
        }; 

        Map<DecodeHintType, Boolean> hintMap = new HashMap<>();
        hintMap.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);

        BufferedImage bufferedImage = MatToBuff(image);
        BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(
            new BufferedImageLuminanceSource(
                bufferedImage))
        );

        Result qrCodeResult = null;

        try {
            qrCodeResult = new MultiFormatReader().decode(binaryBitmap, hintMap);
            decoded = qrCodeResult.getText();
            return new String[]{decoded, qrCodeResult.getBarcodeFormat().toString()};
        } catch (NotFoundException e) {}
        return null;
    }

    private static BufferedImage MatToBuff(Mat grey_image) {
        BufferedImage bufferedImage = new BufferedImage(grey_image.cols(), grey_image.rows(), BufferedImage.TYPE_BYTE_GRAY);
        byte[] data = ((DataBufferByte) bufferedImage.getRaster().getDataBuffer()).getData();
        grey_image.get(0, 0, data);
        return bufferedImage;
    }

    private static Mat buffToMat(BufferedImage bufferedImage) {
            // Create a Mat object with the same dimensions and type as the BufferedImage.TYPE_3BYTE_BGR
        Mat mat = new Mat(bufferedImage.getHeight(), bufferedImage.getWidth(), CvType.CV_8UC3);

        // Get the pixel data from the BufferedImage
        byte[] data = ((DataBufferByte) bufferedImage.getRaster().getDataBuffer()).getData();

        // Put the data into the Mat object
        mat.put(0, 0, data);

        return mat;
    }

    private static Mat transform_image(ArrayList<Point> points, Mat original_img) {
        Point topLeft = points.get(0);
        Point topRight = points.get(1);
        Point bottomRight = points.get(2);
        Point bottomLeft = points.get(3);

        double width = Math.sqrt(Math.pow(topLeft.x-topRight.x, 2) + Math.pow(topLeft.y-topRight.y, 2));
        double height = Math.sqrt(Math.pow(topRight.x-bottomRight.x, 2) + Math.pow(topRight.y-bottomRight.y, 2));

        if (width>height) height = width;
        else width = height;

        Point outputTopLeft = new Point(0, 0);
        Point outputTopRight = new Point(width*1.1, 0);
        Point outputBottomRight = new Point(width*1.1, height*1.1);
        Point outputBottomLeft = new Point(0, height*1.1);

        // Store the points in MatOfPoint2f (for input and output points)
        MatOfPoint2f srcPoints = new MatOfPoint2f(topLeft, topRight, bottomRight, bottomLeft);
        MatOfPoint2f dstPoints = new MatOfPoint2f(outputTopLeft, outputTopRight, outputBottomRight, outputBottomLeft);

        // Calculate the transformation matrix
        Mat perspectiveTransform = Imgproc.getPerspectiveTransform(srcPoints, dstPoints);

        // Apply the perspective transformation
        Mat outputImage = new Mat();
        
        Imgproc.warpPerspective(original_img, outputImage, perspectiveTransform, new Size(width, height));
        
        return outputImage;
    }

    @SuppressWarnings("unused")
    private static void draw(ArrayList<Point> points, Mat image, Scalar color) {
        for (int i = 0; i < points.size(); i++) {
            Point p1 = points.get(i);
            Point p2 = points.get((i + 1) % points.size());
            Imgproc.circle(image, p1, 0, new Scalar(255,0,0));
            Imgproc.line(image, p1, p2, color, 1);
        }
    }
}
