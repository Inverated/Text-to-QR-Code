module main {
    requires transitive javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires com.google.zxing;
    requires com.google.zxing.javase;
    requires java.desktop;

    opens main to javafx.fxml;
    exports main;
}
