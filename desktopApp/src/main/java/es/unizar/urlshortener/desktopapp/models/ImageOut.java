package es.unizar.urlshortener.desktopapp.models;

import javafx.scene.image.WritableImage;

public class ImageOut {
    private WritableImage image;
    private String message;

    public ImageOut() {}
    public ImageOut(WritableImage image, String message) {
        this.image = image;
        this.message = message;
    }

    public void setImage(WritableImage image) {
        this.image = image;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public WritableImage getImage() {
        return this.image;
    }

    public String getMessage() {
        return this.message;
    }
}

