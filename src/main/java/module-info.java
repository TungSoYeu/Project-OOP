module com.ecosim {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires java.sql;

    opens com.ecosim to javafx.fxml, javafx.graphics;
    opens com.ecosim.view to javafx.fxml;

    exports com.ecosim;
    exports com.ecosim.model;
    exports com.ecosim.strategy;
    exports com.ecosim.engine;
    exports com.ecosim.view;
    exports com.ecosim.sound;
    exports com.ecosim.util;
}
