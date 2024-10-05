package main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

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


public class Read {
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME); // Load the OpenCV native library
    }

    public static String[] decode_qr_code(String path) {
        Mat image = Imgcodecs.imread(path);

        Mat gray = new Mat();
        Imgproc.cvtColor(image, gray, Imgproc.COLOR_BGR2GRAY);

        Mat blurred = new Mat();
        Imgproc.GaussianBlur(gray, blurred, new Size(3, 3), 1);

        Mat edges = new Mat();
        Imgproc.Canny(gray, edges, 100, 200);

        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(edges, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

        String[] output = decode(image, path);
        if (output[0]!=null) return output;

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

                double[] horizontal = new double[4],  vertical = horizontal.clone();
                for (int i = 0; i < 4; i++) {
                    horizontal[i] = points.get(i).x;
                    vertical[i] = points.get(i).y;
                }

                

                //Exclude squares
                if (Math.abs(horizontal[0]-horizontal[1]) <= 1 | Math.abs(horizontal[2]-horizontal[3]) <= 1) continue;
                if (Math.abs(vertical[0]-vertical[1]) <= 1 | Math.abs(vertical[2]-vertical[3]) <= 1) continue;
                if (Math.abs(horizontal[0]-horizontal[1])*2 < Math.abs(horizontal[2]-horizontal[3]) | Math.abs(horizontal[0]-horizontal[1]) > Math.abs(horizontal[2]-horizontal[3])*2) continue;
                if (Math.abs(vertical[0]-vertical[1])*2 < Math.abs(vertical[2]-vertical[3]) | Math.abs(vertical[0]-vertical[1]) > Math.abs(vertical[2]-vertical[3])*2) continue;

                Arrays.sort(horizontal);
                Arrays.sort(vertical);

                /* // Draw the detected quadrilateral (Hide)
                draw(points, image); */

               

                System.out.println("Original: ");
                for (int i = 0; i < 4; i++) {
                    System.out.println("Point " + i + ": " + points.get(i));
                }

                //scale the located rectangle
                double width = edges.cols();
                double height = edges.rows();
                double ratio = 0;

 
                //Length will be 1st and 3rd or 2nd and 4th
                if (width/Math.abs(horizontal[0]-horizontal[2]) > ratio) {
                    ratio = width/Math.abs(horizontal[0]-horizontal[2]);
                }
                if (width/Math.abs(horizontal[1]-horizontal[3]) > ratio) {
                    ratio = width/Math.abs(horizontal[1]-horizontal[3]);
                }
                if (height/Math.abs(vertical[0]-vertical[2]) > ratio)    {
                    ratio = height/Math.abs(vertical[0]-vertical[2]);
                }
                if (height/Math.abs(vertical[1]-vertical[3]) > ratio)    {
                    ratio = height/Math.abs(vertical[1]-vertical[3]);
                }

                System.out.println(Arrays.toString(horizontal));
                System.out.println(Arrays.toString(vertical));

                /* for (int i = 0; i < 4; i++) {
                    Point curr = points.get(i);
                    curr.set(new double[] {curr.x-horizontal[0], curr.y-vertical[0]});
                } */
               
                /* for (int i = 0; i < 4; i++) {
                    Point curr = points.get(i);
                    curr.set(new double[] {curr.x*ratio, curr.y*ratio});
                } */

                Point center = new Point(
                    (points.get(0).x + points.get(1).x + points.get(2).x + points.get(3).x) / 4,
                    (points.get(0).y + points.get(1).y + points.get(2).y + points.get(3).y) / 4
                );
                
                // Scale each point relative to the center
                for (int i=0; i<4; i++) {
                    Point p = points.get(i);
                    double scaledX = center.x + (p.x - center.x) * ratio;
                    double scaledY = center.y + (p.y - center.y) * ratio;   
                    horizontal[i] = scaledX;
                    vertical[i] = scaledY;                 
                    p.set(new double[]{scaledX, scaledY});
                }

                Arrays.sort(horizontal);
                Arrays.sort(vertical);

                for (Point p : points) {
                    p.set(new double[] {p.x-horizontal[1], p.y-vertical[1]});
                }
                
                //draw new scalled rectangle (Hide)
                /* draw(points, image);
                Imgcodecs.imwrite("output_image_with_polygon.jpg", image); */


                // Print out the corner points of the quadrilateral (slanted plane)
                System.out.println("Transformed:");
                for (int i = 0; i < 4; i++) {
                    System.out.println("Point " + i + ": " + points.get(i));
                }

                Mat outputimage = transform_image(points, image);
                Imgcodecs.imwrite("output_transformed_image.jpg", outputimage); 
                output = decode(outputimage, path);

                //String[] output = new String[2];
                //int a = sc.nextInt();
                if (output[0]!=null) return output;
            }
        }
        
        return new String[2];
    }
    public static Scanner sc = new Scanner(System.in);

    private static String[] decode(Mat image, String path) {
        QRCodeDetector qrdetector = new QRCodeDetector();
        String decoded = qrdetector.detectAndDecode(image); //place the detected corder coord into points
        System.out.println(decoded);
        String[] out = new String[2];
        if (decoded.isEmpty()) return out;
        out[0] = decoded;
        out[1] = path;

        /* draw_border(image, corners, path + "_modified");
        out[1] = path + "_modified"; */

        return out;
    }

    private static Mat transform_image(ArrayList<Point> points, Mat original_img) {
        Point topLeft = points.get(1);
        Point topRight = points.get(0);
        Point bottomRight = points.get(3);
        Point bottomLeft = points.get(2);

        double width = Math.sqrt(Math.pow(bottomRight.x, 2) + Math.pow(topRight.y, 2));
        double height = Math.sqrt(Math.pow(bottomLeft.x, 2) + Math.pow(bottomLeft.y, 2));

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
        Imgproc.warpPerspective(original_img, outputImage, perspectiveTransform, new Size(width*3, height*3));
        
        return outputImage;
    }

    private static void draw(ArrayList<Point> points, Mat image) {
        for (int i = 0; i < 4; i++) {
            Point p1 = points.get(i);
            Point p2 = points.get((i + 1) % 4);
            Imgproc.line(image, p1, p2, new Scalar(0, 255, 0), 1);
        }
    }
}
