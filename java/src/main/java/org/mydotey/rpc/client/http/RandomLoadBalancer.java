package org.mydotey.rpc.client.http;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.mydotey.java.ObjectExtension;
import org.mydotey.rpc.error.ServiceUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author koqizhao
 *
 * Jan 16, 2019
 */
public class RandomLoadBalancer implements HttpLoadBalancer {

    private static Logger _logger = LoggerFactory.getLogger(HttpServiceClient.class);

    private long _ttl;
    private long _forceUpdateInterval;

    private List<String> _serviceUrls;
    private volatile int _current;
    private volatile long _lastUpdateTime;
    private volatile long _lastForceUpdateTime;

    public RandomLoadBalancer(List<String> serviceUrls, long ttl, long forceUpdateInterval) {
        ObjectExtension.requireNonEmpty(serviceUrls, "serviceUrls");
        if (ttl <= 0)
            throw new IllegalArgumentException("ttl <= 0: " + ttl);
        if (forceUpdateInterval < 0)
            throw new IllegalArgumentException("forceUpdateInterval <= 0: " + forceUpdateInterval);

        _ttl = ttl;
        _forceUpdateInterval = forceUpdateInterval;

        _serviceUrls = new ArrayList<>(serviceUrls);
        Collections.shuffle(_serviceUrls);
        _lastUpdateTime = System.currentTimeMillis();
        _logger.info("init with serviceUrls: {}", _serviceUrls);
    }

    @Override
    public HttpExecutionContext newExecutionContext() {
        String serviceUrl = getServiceUrl();
        return new ExecutionContext(serviceUrl);
    }

    protected void complete(HttpExecutionContext executionContext) {
        ObjectExtension.requireNonNull(executionContext, "executionContext");

        Throwable ex = executionContext.getExecutionError();
        if (ex == null)
            return;

        if (ex instanceof HttpRuntimeException || ex instanceof ServiceUnavailableException)
            forceUpdate();
    }

    protected String getServiceUrl() {
        if (_serviceUrls.size() > 1) {
            if (System.currentTimeMillis() - _lastUpdateTime > _ttl) {
                synchronized (this) {
                    if (System.currentTimeMillis() - _lastUpdateTime > _ttl) {
                        update();
                    }
                }
            }
        }

        return _serviceUrls.get(_current);
    }

    protected void forceUpdate() {
        if (_serviceUrls.size() == 1)
            return;

        if (System.currentTimeMillis() - _lastForceUpdateTime < _forceUpdateInterval)
            return;

        synchronized (this) {
            if (System.currentTimeMillis() - _lastForceUpdateTime < _forceUpdateInterval)
                return;

            update();
            _lastForceUpdateTime = _lastUpdateTime;
        }
    }

    private void update() {
        int old = _current;
        _current = (_current + 1) % _serviceUrls.size();
        _lastUpdateTime = System.currentTimeMillis();
        _logger.info("serviceUrl changed from {} to {}", _serviceUrls.get(old), _serviceUrls.get(_current));
    }

    protected class ExecutionContext implements HttpExecutionContext {

        private String _serviceUrl;
        private volatile long _startTime;
        private volatile long _endTime;
        private volatile Throwable _executionError;

        public ExecutionContext(String serviceUrl) {
            ObjectExtension.requireNonBlank(serviceUrl, "serviceUrl");

            _serviceUrl = serviceUrl;
            _startTime = System.currentTimeMillis();
        }

        @Override
        public String getServiceUrl() {
            return _serviceUrl;
        }

        @Override
        public long getStartTime() {
            return _startTime;
        }

        @Override
        public long getEndTime() {
            return _endTime;
        }

        @Override
        public Throwable getExecutionError() {
            return _executionError;
        }

        @Override
        public void setExecutionError(Throwable executionError) {
            ObjectExtension.requireNonNull(executionError, "executionError");
            _executionError = executionError;
        }

        @Override
        public void complete() {
            if (_endTime != 0)
                return;

            synchronized (this) {
                if (_endTime != 0)
                    return;

                _endTime = System.currentTimeMillis();
                RandomLoadBalancer.this.complete(this);
            }
        }

        @Override
        public String toString() {
            return "ExecutionContext [_serviceUrl=" + _serviceUrl + ", _startTime=" + _startTime + ", _endTime="
                    + _endTime + ", _executionError=" + _executionError + "]";
        }

    }

}
