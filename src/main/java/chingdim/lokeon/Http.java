package chingdim.lokeon;

import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * HTTP related code
 */
public class Http {
    // Master HTTP Client for interacting with DimBot
    private final OkHttpClient client = new OkHttpClient.Builder().build();
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

    /**
     * Factory for HTTP requests
     * @param path The query and path of the HTTP request
     * @return Request object
     */
    private Request.Builder build(String path) {
        return new Request.Builder().url(address + path);
    }

    /**
     * Executes the request
     * @param request HTTP Request to be executed
     * @param path Path of the HTTP request
     */
    private void execute_call(Request request, String path) {
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                // The HTTP request failed
                logger.severe(String.format("An error occurred during request %s: %s", path, e.getLocalizedMessage()));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                // The HTTP request has a response
                if (!response.isSuccessful()) logger.severe(String.format("Request %s returned response code %s", path, response.code()));
                else {
                    logger.info(String.format("Request %s succeed: %s", path, response.message()));
                }
            }
        });
    }

    /**
     * A HTTP GET constructor for requests
     * @param path Path for the GET request
     */
    private void get(String path) {
        Request request = build(path).build();
        execute_call(request, path);
    }

    /**
     * A HTTP POST constructor for requests
     * @param path Path for the POST request
     */
    private void post(String path, String data) {
        Request request = build(path).post(RequestBody.create(data, MediaType.get("text/plain; charset=utf-8"))).build();
        execute_call(request, path);
    }

}
