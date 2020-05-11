package chingdim.lokeon;

import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.logging.Logger;

public class Http {
    private final OkHttpClient client = new OkHttpClient.Builder().build();
    private final String address;
    private final Logger logger;

    Http(String address, Logger logger) {
        this.address = address;
        this.logger = logger;
    }

    void hook() {get("hook");}
    void join(String name) {post("join", name);}
    void quit(String name) {post("quit", name);}
    void shutdown(String name) {get("shutdown?name=" + name);}

    private Request.Builder build(String path) {
        return new Request.Builder().url(address + path);
    }

    private void execute_call(Request request, String path) {
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                logger.severe(String.format("An error occurred during request %s: %s", path, e.getLocalizedMessage()));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                if (!response.isSuccessful()) logger.severe(String.format("Request %s returned response code %s", path, response.code()));
                else {
                    logger.info(String.format("Request %s succeed: %s", path, response.message()));
                }
            }
        });
    }

    private void get(String path) {
        Request request = build(path).build();
        execute_call(request, path);
    }

    private void post(String path, String data) {
        Request request = build(path).post(RequestBody.create(data, MediaType.get("text/plain; charset=utf-8"))).build();
        execute_call(request, path);
    }

}
