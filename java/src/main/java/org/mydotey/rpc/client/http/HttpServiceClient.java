package org.mydotey.rpc.client.http;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.mydotey.java.ObjectExtension;
import org.mydotey.java.StringExtension;
import org.mydotey.rpc.client.RpcClient;
import org.mydotey.rpc.client.http.HttpLoadBalancer.HttpExecutionContext;
import org.mydotey.rpc.client.http.apache.HttpRequestFactory;
import org.mydotey.rpc.client.http.apache.async.HttpRequestAsyncExecutors;
import org.mydotey.rpc.client.http.apache.sync.HttpRequestExecutors;
import org.mydotey.rpc.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author koqizhao
 *
 * Nov 29, 2018
 */
public class HttpServiceClient implements RpcClient {

    private static Logger _logger = LoggerFactory.getLogger(HttpServiceClient.class);

    private HttpServiceClientConfig _config;

    public HttpServiceClient(HttpServiceClientConfig config) {
        ObjectExtension.requireNonNull(config, "config");
        _config = config;
    }

    public HttpServiceClientConfig getConfig() {
        return _config;
    }

    protected <Req> HttpUriRequest toHttpUriRequest(String serviceUrl, String procedure, Req request) {
        String restPath = _config.getProcedureRestPathMap().get(procedure);
        if (restPath == null)
            throw new IllegalArgumentException("unknown procedure: " + procedure);

        String requestUrl;
        if (serviceUrl.endsWith("/") && !restPath.startsWith("/")
                || !serviceUrl.endsWith("/") && restPath.startsWith("/"))
            requestUrl = serviceUrl + restPath;
        else if (serviceUrl.endsWith("/"))
            requestUrl = serviceUrl + StringExtension.trimStart(restPath, '/');
        else
            requestUrl = serviceUrl + "/" + restPath;

        if (request == null)
            return HttpRequestFactory.createRequest(requestUrl, HttpGet.METHOD_NAME);
        return HttpRequestFactory.createRequest(requestUrl, HttpPost.METHOD_NAME, request, _config.getCodec());
    }

    @Override
    public <Req, Res> Res invoke(String procedure, Req request, Class<Res> clazz) {
        HttpExecutionContext executionContext = _config.getLoadBalancer().newExecutionContext();
        Res response = null;
        try {
            HttpUriRequest httpUriRequest = toHttpUriRequest(executionContext.getServiceUrl(), procedure, request);
            response = HttpRequestExecutors.execute(_config.getSyncClientProvider().get(), httpUriRequest,
                    _config.getCodec(), clazz);
            Response.checkResponse((Response) response);
            return response;
        } catch (Throwable ex) {
            executionContext.setExecutionError(ex);
            logError(procedure, request, response, executionContext);
            throw ex;
        } finally {
            executionContext.complete();
        }
    }

    @Override
    public <Req, Res> CompletableFuture<Res> invokeAsync(String procedure, Req request, Class<Res> clazz) {
        HttpExecutionContext executionContext = _config.getLoadBalancer().newExecutionContext();
        AtomicReference<Res> responseRef = new AtomicReference<>();
        try {
            HttpUriRequest httpUriRequest = toHttpUriRequest(executionContext.getServiceUrl(), procedure, request);
            return HttpRequestAsyncExecutors
                    .executeAsync(_config.getAsyncClientProvider().get(), httpUriRequest, _config.getCodec(), clazz)
                    .thenApply(r -> {
                        responseRef.set(r);
                        Response.checkResponse((Response) r);
                        return r;
                    }).whenComplete((r, e) -> {
                        try {
                            if (e == null)
                                return;

                            if (e instanceof ExecutionException)
                                e = e.getCause();
                            executionContext.setExecutionError(e);
                            logError(procedure, request, responseRef.get(), executionContext);
                        } finally {
                            executionContext.complete();
                        }
                    });
        } catch (Throwable e) {
            executionContext.setExecutionError(e);
            executionContext.complete();
            logError(procedure, request, responseRef.get(), executionContext);
            throw e;
        }
    }

    protected <Req, Res> void logError(String procedure, Req request, Res response,
            HttpExecutionContext executionContext) {
        _logger.info("rpc failed, procedure: {}, codec: {}, context: {}", procedure, _config.getCodec(),
                executionContext);
    }

    @Override
    public void close() throws IOException {

    }

}
