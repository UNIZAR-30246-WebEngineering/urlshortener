package es.unizar.urlshortener.desktopapp.models;

public class LinkResponseEroor {
    private String code;
    private String message;

    public LinkResponseEroor(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return this.code;
    }

    public String getMessage() {
        return this.message;
    }
}
