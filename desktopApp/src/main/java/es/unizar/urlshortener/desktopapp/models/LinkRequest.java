package es.unizar.urlshortener.desktopapp.models;

public class LinkRequest {
    private String url;
    private String limit;
    private String lat;
    private String lon;

    public LinkRequest(String url, String limit, String lat, String lon) {
        this.url = url;
        this.limit = limit;
        this.lat = lat;
        this.lon = lon;
    }
}
