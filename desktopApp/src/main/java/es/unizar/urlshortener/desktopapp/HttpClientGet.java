package es.unizar.urlshortener.desktopapp;

import es.unizar.urlshortener.desktopapp.models.ImageOut;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.http.HttpClient;
import java.time.Duration;

public class HttpClientGet {
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private URL url;

    public HttpClientGet(String url) throws MalformedURLException {
        this.url = new URL(url);

    }

    public ImageOut getImage() {
        System.out.println("GET image: " + url);
        BufferedImage image = null;
        String message = null;
        ImageOut imageOut = new ImageOut();
        try {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {                                // 200 Ok
                image = ImageIO.read(connection.getInputStream());
                imageOut.setImage(convertToJavaFXImage(image));
            } else if (connection.getResponseCode() == HttpURLConnection.HTTP_FORBIDDEN) {                  // 403 Error msj
                message = "Error: " + connection.getResponseCode() + "URI is not safe";
            } else {                                                                                        // 400 Error msj
                message = "Error: " + connection.getResponseCode() + " URI has not been validated yet";
            }
            connection.disconnect();
        } catch (Exception e) {
            e.getMessage();
        }
        imageOut.setMessage(message);
        return imageOut;
    }

    private WritableImage convertToJavaFXImage(BufferedImage image) {
        WritableImage wr = null;
        if (image != null) {
            wr = new WritableImage(image.getWidth(), image.getHeight());
            PixelWriter pw = wr.getPixelWriter();
            for (int x = 0; x < image.getWidth(); x++) {
                for (int y = 0; y < image.getHeight(); y++) {
                    pw.setArgb(x, y, image.getRGB(x, y));
                }
            }
        }
        return wr;
    }

}