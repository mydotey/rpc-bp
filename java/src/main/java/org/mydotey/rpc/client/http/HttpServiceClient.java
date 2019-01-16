package org.mydotey.rpc.client.http;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.mydotey.codec.Codec;
import org.mydotey.java.ObjectExtension;
import org.mydotey.java.StringExtension;
import org.mydotey.rpc.ack.Acks;
import org.mydotey.rpc.client.http.HttpRuntimeException;
import org.mydotey.rpc.client.http.apache.ApacheHttpRpcClient;
import org.mydotey.rpc.client.http.apache.HttpRequestFactory;
import org.mydotey.rpc.error.BadRequestException;
import org.mydotey.rpc.error.ErrorCodes;
import org.mydotey.rpc.error.ServiceException;
import org.mydotey.rpc.error.ServiceUnavailableException;
import org.mydotey.rpc.response.Response;
import org.mydotey.rpc.response.ResponseError;

/**
 * @author koqizhao
 *
 * Nov 29, 2018
 */
public class HttpServiceClient extends ApacheHttpRpcClient {

    private HttpServiceClientConfig _config;

    public HttpServiceClient(HttpServiceClientConfig config) {
        ObjectExtension.requireNonNull(config, "config");
        _config = config;
    }

    public HttpServiceClientConfig getConfig() {
        return _config;
    }

    @Override
    public void close() throws IOException {

    }

    @Override
    protected CloseableHttpClient getHttpClient() {
        return _config.getSyncClientProvider().get();
    }

    @Override
    protected CloseableHttpAsyncClient getHttpAsyncClient() {
        return _config.getAsyncClientProvider().get();
    }

    @Override
    protected <Req> HttpUriRequest toHttpUriRequest(String procedure, Req request) {
        String restPath = _config.getProcedureRestPathMap().get(procedure);
        if (restPath == null)
            throw new IllegalArgumentException("unknown procedure: " + procedure);

        String requestUrl = _config.getLoadBalancer().getServiceUrl() + restPath;

        if (request == null)
            return HttpRequestFactory.createRequest(requestUrl, HttpGet.METHOD_NAME);
        return HttpRequestFactory.createRequest(requestUrl, HttpPost.METHOD_NAME, request, getCodec());
    }

    @Override
    public <Req, Res> Res invoke(String procedure, Req request, Class<Res> clazz) {
        try {
            Res res = super.invoke(procedure, request, clazz);
            checkResponse((Response) res);
            return res;
        } catch (HttpRuntimeException | ServiceUnavailableException e) {
            _config.getLoadBalancer().forceUpdate();
            throw e;
        }
    }

    @Override
    public <Req, Res> CompletableFuture<Res> invokeAsync(String procedure, Req request, Class<Res> clazz) {
        return super.invokeAsync(procedure, request, clazz).thenApply(r -> {
            checkResponse((Response) r);
            return r;
        }).whenComplete((r, e) -> {
            if (e == null)
                return;

            if (e instanceof ExecutionException)
                e = e.getCause();
            if (e instanceof HttpRuntimeException || e instanceof ServiceUnavailableException)
                _config.getLoadBalancer().forceUpdate();
        });
    }

    @Override
    protected Codec getCodec() {
        return _config.getCodec();
    }

    protected void checkResponse(Response res) {
        String message = "unknown error";
        if (res == null)
            throw new ServiceException(message);

        if (res.getStatus() == null)
            throw new ServiceException("no status, response: " + res);

        if (Acks.isFail(res.getStatus().getAck())) {
            ResponseError error = res.getStatus().getError();
            if (error == null)
                throw new ServiceException("no error, response: " + res);

            if (!StringExtension.isBlank(error.getMessage()))
                message = error.getMessage();

            String errorCode = error.getCode();
            if (errorCode == null)
                throw new ServiceException("no error code, response: " + res);

            switch (errorCode) {
                case ErrorCodes.BAD_REQUEST:
                    throw new BadRequestException(error.getMessage());
                case ErrorCodes.SERVICE_EXCEPTION:
                    throw new ServiceException(error.getMessage());
                case ErrorCodes.SERVICE_UNAVAILABLE:
                    throw new ServiceUnavailableException(error.getMessage());
                default:
                    throw new ServiceException("errorCode: " + error.getCode() + ", message: " + message);
            }
        }
    }

}
