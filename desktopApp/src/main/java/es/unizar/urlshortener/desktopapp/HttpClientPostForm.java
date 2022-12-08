package es.unizar.urlshortener.desktopapp;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

//https://mkyong.com/java/java-11-httpclient-examples/
public class HttpClientPostForm {
    private String apiUrl;
    HttpClientPostForm(String apiUrl) {
        this.apiUrl = apiUrl;
    }
    private final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public HttpResponse<String> apiLink(String url, String limit, String lat, String lon) throws IOException, InterruptedException {

        // form parameters
        Map<Object, Object> data = new HashMap<>();
        data.put("url", url);
        data.put("limit", limit);
        data.put("lat", lat);
        data.put("lon", lon);

        HttpResponse<String> response = null;
        return post(data);
    }

    private HttpResponse<String> post(Map<Object, Object> data) throws IOException, InterruptedException {
        System.out.println(ofFormData(data));
        HttpRequest request = HttpRequest.newBuilder()
                .POST(ofFormData(data))
                .uri(URI.create(apiUrl))
                .setHeader("User-Agent", "Java 11 HttpClient Bot") // add request header
                .header("Content-Type", "application/x-www-form-urlencoded")
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return response;
    }

    // Sample: 'password=123&custom=secret&username=abc&ts=1570704369823'
    public HttpRequest.BodyPublisher ofFormData(Map<Object, Object> data) {
        var builder = new StringBuilder();
        for (Map.Entry<Object, Object> entry : data.entrySet()) {
            if (builder.length() > 0) {
                builder.append("&");
            }
            builder.append(URLEncoder.encode(entry.getKey().toString(), StandardCharsets.UTF_8));
            builder.append("=");
            builder.append(URLEncoder.encode(entry.getValue().toString(), StandardCharsets.UTF_8));
        }
        return HttpRequest.BodyPublishers.ofString(builder.toString());
    }

}
