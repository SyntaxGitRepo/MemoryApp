module gui.memory {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.smartcardio;
    requires org.json;


    opens gui.memory to javafx.fxml;
    exports gui.memory;
}