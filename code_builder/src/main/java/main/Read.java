package main;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javax.imageio.ImageIO;

import org.opencv.core.Core;
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
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;


public class Read {
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME); // Load the OpenCV native library
    }

    private final static String[] split_dir = System.getProperty("user.dir").split("\\\\"); 
    private final static String temp_dir = System.getProperty("user.dir") + (split_dir[split_dir.length-1].equals("code_builder") ? "\\src\\main\\resources\\temp_img" : "\\code_builder\\src\\main\\resources\\temp_img");


    public static String decode_qr_code(String path) {
        Mat image = Imgcodecs.imread(path);
        Mat annotated = image.clone();

        Mat gray = new Mat();
        Imgproc.cvtColor(image, gray, Imgproc.COLOR_BGR2GRAY);
        Imgcodecs.imwrite(temp_dir+"/gray.png", gray);

        Mat blackAndWhiteImage = new Mat();
        Imgproc.threshold(gray, blackAndWhiteImage, 100, 255, Imgproc.THRESH_BINARY);
        Imgcodecs.imwrite(temp_dir+"/black and wite.png", blackAndWhiteImage);

        String output = decode(gray);
        if (output != null) {
            return output;
        }

        Mat filteredImage = new Mat();
        // Apply Bilateral Filter
        int diameter = 50;  // Diameter of pixel neighborhood
        double sigmaColor = 80;  // Filter sigma in the color space (higher value = more blur)
        double sigmaSpace = 200;  // Filter sigma in the coordinate space (higher value = more blur)
        Imgproc.bilateralFilter(blackAndWhiteImage, filteredImage, diameter, sigmaColor, sigmaSpace);
        Imgcodecs.imwrite(temp_dir+"/bilateral blurred.png", filteredImage);

        Mat edges = new Mat();
        Imgproc.Canny(blackAndWhiteImage, edges, 100, 200);
        Imgcodecs.imwrite(temp_dir+"/edges.png", edges);

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
               
                //Exxclude looong rectangles
                double widthratio = Math.abs(points.get(0).x-points.get(1).x)/(Math.abs(points.get(1).x-points.get(2).x));
                if (widthratio > 4 | widthratio < 0.25) {
                    continue;
                }
                double heightratio = Math.abs(points.get(0).y-points.get(1).y)/(Math.abs(points.get(1).y-points.get(2).y));
                if (heightratio > 4 | heightratio < 0.25) {
                    continue;
                }

                //Exclude very small boxes 
                if (    Math.abs(points.get(0).x-points.get(1).x) + (Math.abs(points.get(2).x-points.get(3).x)) < 3 |
                        Math.abs(points.get(0).y-points.get(1).y) + (Math.abs(points.get(2).y-points.get(3).y)) < 3) {
                    continue;
                }

                boolean exit=false;
                for (int i = 0; i < len; i++) {
                    exit = false;
                    for (int j = i+1; j < points.size(); j++) {
                        if (Math.abs(points.get(i).x-points.get(j).x) <= 2 && Math.abs(points.get(i).y-points.get(j).y) <= 2) {
                            exit = true;
                            break;
                        }
                    }
                    if (exit) break;
                }
                if (exit) continue;

                System.out.println();
                for (Point p : points) {
                    System.out.println(p.x + " " + p.y);
                }
                draw(points, annotated, new Scalar(0,255,0));
                Imgproc.putText(annotated, points.get(0).x+" "+points.get(0).y, points.get(0), 1, 1, new Scalar(255,100,200));

                double cenx = (points.get(0).x + points.get(1).x + points.get(2).x + points.get(3).x) / len;
                double ceny = (points.get(0).y + points.get(1).y + points.get(2).y + points.get(3).y) / len;

                Point center = new Point(cenx, ceny);

                double ratio = 32;
                // Scale each point relative to the center
                for (int i=0; i<len; i++) {
                    Point p = points.get(i);
                    double scaledX = center.x + (p.x - cenx) * ratio;
                    double scaledY = center.y + (p.y - ceny) * ratio;                
                    p.set(new double[]{scaledX, scaledY});
                }

                draw(points, annotated, new Scalar(255,0,0));
                Imgcodecs.imwrite(temp_dir+"/annotated.png", annotated);

                Mat transformed = transform_image(points, gray);
                Imgcodecs.imwrite(temp_dir + "/Transformed.png", transformed);

                output = decode(transformed);

                /* try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } */

                if (output!=null) return output;
                break;
            }
        }
        return null;
    }
    public static Scanner sc = new Scanner(System.in);
    public static int i = 1;

    private static String decode(Mat image) {
        QRCodeDetector qrdetector = new QRCodeDetector();
        String decoded = qrdetector.detectAndDecode(image);
        if (!decoded.isEmpty()) return decoded; 

        Map<DecodeHintType, Boolean> hintMap = new HashMap<>();
            hintMap.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
            com.google.zxing.Result qrCodeResult = null;
        try {
            BufferedImage bufferedImage = new BufferedImage(image.cols(), image.rows(), BufferedImage.TYPE_BYTE_GRAY);
            byte[] data = ((DataBufferByte) bufferedImage.getRaster().getDataBuffer()).getData();
            image.get(0, 0, data);

            ImageIO.write(bufferedImage, "png", new File(temp_dir+"/"+i+"uuuu.png"));
            i+=1;
            
            BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(
                new BufferedImageLuminanceSource(
                    bufferedImage))
            );
            qrCodeResult = new QRCodeReader().decode(binaryBitmap, hintMap);
            System.out.println("Zxing"+qrCodeResult.getText());
            return qrCodeResult.getText();
        } catch (Exception e) { }

        return null;
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
        Point outputTopRight = new Point(width, 0);
        Point outputBottomRight = new Point(width, height);
        Point outputBottomLeft = new Point(0, height);

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
