package org.mydotey.rpc.client.http.helloservice;

import java.io.Closeable;
import java.io.IOException;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * @author koqizhao
 *
 * Sep 21, 2018
 */
public class HelloApp implements Closeable {

    private String[] _args;

    private volatile ConfigurableApplicationContext _applicationContext;

    public HelloApp(int port) {
        _args = new String[] { "--server.port=" + port };
    }

    public HelloApp(String[] args) {
        _args = args;
    }

    public void start() {
        if (_applicationContext != null)
            return;

        synchronized (this) {
            if (_applicationContext != null)
                return;

            _applicationContext = SpringApplication.run(HelloServiceImpl.class, _args);
        }
    }

    public void stop() {
        synchronized (this) {
            if (_applicationContext != null) {
                _applicationContext.close();
                _applicationContext = null;
            }
        }
    }

    @Override
    public void close() throws IOException {
        stop();
    }

}
