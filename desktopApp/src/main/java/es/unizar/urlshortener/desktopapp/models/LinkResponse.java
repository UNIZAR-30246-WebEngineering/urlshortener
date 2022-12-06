package es.unizar.urlshortener.desktopapp.models;

public class LinkResponse {
    private String url;
    private String lat;
    private String lon;
    private String country;
    private String city;
    private String state;
    private String road;
    private String cp;
    private Object properties;

    public LinkResponse(String url, String lat, String lon, String country, String city, String state, String road, String cp, Object properties) {
        this.url = url;
        this.lat = lat;
        this.lon = lon;
        this.country = country;
        this.city = city;
        this.state = state;
        this.road = road;
        this.cp = cp;
        this.properties = properties;
    }

    public String getURL() {
        return this.url;
    }
}
