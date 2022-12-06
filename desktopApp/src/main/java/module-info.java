module es.unizar.urlshortener.desktopapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;
    requires java.desktop;
    requires com.google.gson;

    opens es.unizar.urlshortener.desktopapp to javafx.fxml;
    exports es.unizar.urlshortener.desktopapp;
    exports es.unizar.urlshortener.desktopapp.models;
    opens es.unizar.urlshortener.desktopapp.models to com.google.gson;
}