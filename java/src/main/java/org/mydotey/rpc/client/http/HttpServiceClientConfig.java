package org.mydotey.rpc.client.http;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.mydotey.codec.Codec;
import org.mydotey.java.ObjectExtension;

/**
 * @author koqizhao
 *
 * Dec 18, 2018
 */
public class HttpServiceClientConfig implements Cloneable {

    private Map<String, String> _procedureRestPathMap;

    private Supplier<CloseableHttpClient> _syncClientProvider;
    private Supplier<CloseableHttpAsyncClient> _asyncClientProvider;

    private Codec _codec;

    private HttpLoadBalancer _loadBalancer;

    protected HttpServiceClientConfig() {

    }

    public Map<String, String> getProcedureRestPathMap() {
        return _procedureRestPathMap;
    }

    public Supplier<CloseableHttpClient> getSyncClientProvider() {
        return _syncClientProvider;
    }

    public Supplier<CloseableHttpAsyncClient> getAsyncClientProvider() {
        return _asyncClientProvider;
    }

    public Codec getCodec() {
        return _codec;
    }

    public HttpLoadBalancer getLoadBalancer() {
        return _loadBalancer;
    }

    @Override
    protected HttpServiceClientConfig clone() {
        try {
            HttpServiceClientConfig config = (HttpServiceClientConfig) super.clone();
            if (_procedureRestPathMap != null)
                config._procedureRestPathMap = Collections.unmodifiableMap(new HashMap<>(config._procedureRestPathMap));
            return config;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    public static class Builder {

        private HttpServiceClientConfig _config;

        public Builder() {
            _config = new HttpServiceClientConfig();
        }

        public Builder setProcedureRestPathMap(Map<String, String> procedureRestPathMap) {
            _config._procedureRestPathMap = procedureRestPathMap;
            return this;
        }

        public Builder setSyncClientProvider(Supplier<CloseableHttpClient> syncClientProvider) {
            _config._syncClientProvider = syncClientProvider;
            return this;
        }

        public Builder setAsyncClientProvider(Supplier<CloseableHttpAsyncClient> asyncClientProvider) {
            _config._asyncClientProvider = asyncClientProvider;
            return this;
        }

        public Builder setCodec(Codec codec) {
            _config._codec = codec;
            return this;
        }

        public Builder setLoadBalancer(HttpLoadBalancer loadBalancer) {
            _config._loadBalancer = loadBalancer;
            return this;
        }

        public HttpServiceClientConfig build() {
            ObjectExtension.requireNonEmpty(_config._procedureRestPathMap, "procedureRestPathMap");
            ObjectExtension.requireNonNull(_config._syncClientProvider, "syncClientProvider");
            ObjectExtension.requireNonNull(_config._asyncClientProvider, "asyncClientProvider");
            ObjectExtension.requireNonNull(_config._codec, "codec");
            ObjectExtension.requireNonNull(_config._loadBalancer, "loadBalancer");

            return _config.clone();
        }
    }

}
