package chingdim.lokeon;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.logging.Logger;

/**
 * HTTP related code
 */
public class Http {
    // Master HTTP Client for interacting with DimBot
    private static final HttpClient client = HttpClient.newBuilder().build();
    private final String address;
    private final Logger logger;

    Http(String address, Logger logger) {
        this.address = address;
        this.logger = logger;
    }

    // Shorthands for sending various HTTP requests to DimBot
    void hook() {get("hook");}
    void join(String name) {post("join", name);}
    void quit(String name) {post("quit", name);}
    void shutdown(String name) {get("shutdown?name=" + name);}


    private void request_handle(HttpRequest request, String path) {
        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    int code = response.statusCode();
                    if (code >= 400) logger.severe(String.format("Request %s responded with %s", path, code));
                    else logger.info(String.format("Request %s responded with %s", path, code));
                });
    }

    /**
     * A HTTP GET constructor for requests
     * @param path Path for the GET request
     */
    private void get(String path) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(address + path))
                .build();
        request_handle(request, path);
    }

    /**
     * A HTTP POST constructor for requests
     * @param path Path for the POST request
     */
    private void post(String path, String data) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(address + path))
                .POST(HttpRequest.BodyPublishers.ofString(data))
                .build();
        request_handle(request, path);
    }

}
