module gui.memory {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.smartcardio;


    opens gui.memory to javafx.fxml;
    exports gui.memory;
}