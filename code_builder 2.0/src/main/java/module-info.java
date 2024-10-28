module main {
    requires transitive javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires transitive com.google.zxing;
    requires javafx.swing;
    requires com.google.zxing.javase;
    requires transitive java.desktop;
    requires transitive org.bytedeco.opencv;
    requires org.bytedeco.javacv;

    opens main to javafx.fxml;
    exports main;
}
