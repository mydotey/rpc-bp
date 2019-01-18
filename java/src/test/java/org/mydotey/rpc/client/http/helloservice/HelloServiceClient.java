package org.mydotey.rpc.client.http.helloservice;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.mydotey.rpc.client.http.HttpServiceClient;
import org.mydotey.rpc.client.http.HttpServiceClientConfig;

/**
 * @author koqizhao
 *
 * Jan 18, 2019
 */
public class HelloServiceClient implements HelloService {

    private static final String PROCEDURE_HELLO = "hello";

    private static final Map<String, String> PROCEDURE_REST_PATH_MAP;

    static {
        Map<String, String> restPathMap = new HashMap<>();
        restPathMap.put(PROCEDURE_HELLO, "/hello");
        PROCEDURE_REST_PATH_MAP = Collections.unmodifiableMap(restPathMap);
    }

    private HttpServiceClient _client;

    public HelloServiceClient(HttpServiceClientConfig.Builder clientConfigBuilder) {
        clientConfigBuilder.setProcedureRestPathMap(PROCEDURE_REST_PATH_MAP);
        _client = new HttpServiceClient(clientConfigBuilder.build());
    }

    public HttpServiceClientConfig getConfig() {
        return _client.getConfig();
    }

    @Override
    public HelloResponse hello(HelloRequest request) {
        return _client.invoke(PROCEDURE_HELLO, request, HelloResponse.class);
    }

    @Override
    public HelloResponse hello() {
        return _client.invoke(PROCEDURE_HELLO, null, HelloResponse.class);
    }

    public CompletableFuture<HelloResponse> helloAsync(HelloRequest request) {
        return _client.invokeAsync(PROCEDURE_HELLO, request, HelloResponse.class);
    }

    public CompletableFuture<HelloResponse> helloAsync() {
        return _client.invokeAsync(PROCEDURE_HELLO, null, HelloResponse.class);
    }

}
