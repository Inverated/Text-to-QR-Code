module main {
    requires transitive javafx.controls;
    requires javafx.fxml;

    opens main to javafx.fxml;
    exports main;
}
