package org.mydotey.rpc.client.http;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.mydotey.codec.json.JacksonJsonCodec;
import org.mydotey.java.CloseableExtension;
import org.mydotey.rpc.ack.Acks;
import org.mydotey.rpc.client.http.HttpServiceClientConfig.Builder;
import org.mydotey.rpc.client.http.apache.async.DynamicPoolingNHttpClientProvider;
import org.mydotey.rpc.client.http.apache.sync.DynamicPoolingHttpClientProvider;
import org.mydotey.rpc.client.http.helloservice.HelloApp;
import org.mydotey.rpc.client.http.helloservice.HelloRequest;
import org.mydotey.rpc.client.http.helloservice.HelloServiceClient;
import org.mydotey.scf.ConfigurationManager;
import org.mydotey.scf.ConfigurationManagerConfig;
import org.mydotey.scf.facade.ConfigurationManagers;
import org.mydotey.scf.facade.SimpleConfigurationSources;
import org.mydotey.scf.source.stringproperty.memorymap.MemoryMapConfigurationSource;

/**
 * @author koqizhao
 *
 * Jan 18, 2019
 */
@RunWith(Parameterized.class)
public class HttpServiceClientTest2 {

    private static final long SLEEP_MS = 500;

    @Parameters(name = "{index}: serverPorts={0}, badServerPorts={1}, ttl={2}, updateInterval={3}, runTimes={4}, errorTimes={5}")
    public static Collection<Object[]> data() throws NoSuchMethodException, SecurityException {
        List<Object[]> parameterValues = new ArrayList<>();
        List<Integer> serverPorts = Arrays.asList(18085);
        List<Integer> badServerPorts = Arrays.asList();

        parameterValues.add(new Object[] { serverPorts, badServerPorts, 1, 0, 1, Arrays.asList(0) });

        badServerPorts = Arrays.asList(19085);
        parameterValues.add(new Object[] { serverPorts, badServerPorts, 4 * SLEEP_MS, 0, 10, Arrays.asList(2, 3) });
        parameterValues
                .add(new Object[] { serverPorts, badServerPorts, 4 * SLEEP_MS, 5 * SLEEP_MS, 10, Arrays.asList(3, 4) });

        return parameterValues;
    }

    @Parameter(0)
    public List<Integer> serverPorts;

    @Parameter(1)
    public List<Integer> badServerPorts;

    @Parameter(2)
    public long ttl;

    @Parameter(3)
    public long updateInterval;

    @Parameter(4)
    public int runTimes;

    @Parameter(5)
    public List<Integer> alternativeErrorTimes;

    private List<HelloApp> _apps;

    private HelloServiceClient _client;

    @Before
    public void setUp() {
        _apps = new ArrayList<>();
        for (int port : serverPorts) {
            HelloApp app = new HelloApp(port);
            app.start();
            _apps.add(app);
        }

        List<String> serviceUrls = new ArrayList<>();
        serverPorts.forEach(p -> serviceUrls.add("http://localhost:" + p));
        badServerPorts.forEach(p -> serviceUrls.add("http://localhost:" + p));

        MemoryMapConfigurationSource memoryMapConfigurationSource = SimpleConfigurationSources.newMemoryMapSource("memory");
        ConfigurationManagerConfig configurationManagerConfig = ConfigurationManagers.newConfigBuilder()
                .setName("http-service-client-config").addSource(1, memoryMapConfigurationSource).build();
        ConfigurationManager configurationManager = ConfigurationManagers.newManager(configurationManagerConfig);
        Builder builder = new HttpServiceClientConfig.Builder().setCodec(JacksonJsonCodec.DEFAULT)
                .setLoadBalancer(new RandomLoadBalancer(serviceUrls, ttl, updateInterval))
                .setAsyncClientProvider(
                        new DynamicPoolingNHttpClientProvider("async-http-client", configurationManager))
                .setSyncClientProvider(new DynamicPoolingHttpClientProvider("sync-http-client", configurationManager));
        _client = new HelloServiceClient(builder);
    }

    @After
    public void tearDown() {
        _apps.forEach(CloseableExtension::close);
        CloseableExtension.close(_client.getConfig().getSyncClientProvider().get());
        CloseableExtension.close(_client.getConfig().getAsyncClientProvider().get());
    }

    @Test
    public void get() throws InterruptedException {
        int errorTimes = 0;
        for (int i = 0; i < runTimes; i++) {
            try {
                _client.hello();
                System.out.printf("\ntimes: %s, result: success\n", i);
            } catch (Exception e) {
                errorTimes++;
                System.out.printf("\ntimes: %s, result: fail\n", i);
            }

            Thread.sleep(SLEEP_MS);
        }

        System.out.println("errorTimes: " + errorTimes);
        Assert.assertTrue(String.format("errorTimes %s not in %s", errorTimes, alternativeErrorTimes),
                alternativeErrorTimes.contains(errorTimes));
    }

    @Test
    public void post() throws InterruptedException {
        HelloRequest request = new HelloRequest();
        request.setAck(Acks.SUCCESS);
        request.setHello("world");

        int errorTimes = 0;
        for (int i = 0; i < runTimes; i++) {
            try {
                _client.hello(request);
                System.out.printf("\ntimes: %s, result: success\n", i);
            } catch (Exception e) {
                errorTimes++;
                System.out.printf("\ntimes: %s, result: fail\n", i);
            }

            Thread.sleep(SLEEP_MS);
        }

        System.out.println("errorTimes: " + errorTimes);
        Assert.assertTrue(String.format("errorTimes %s not in %s", errorTimes, alternativeErrorTimes),
                alternativeErrorTimes.contains(errorTimes));
    }

    @Test
    public void getAsync() throws InterruptedException, ExecutionException {
        int errorTimes = 0;
        for (int i = 0; i < runTimes; i++) {
            try {
                _client.helloAsync().get();
                System.out.printf("\ntimes: %s, result: success\n", i);
            } catch (Exception e) {
                errorTimes++;
                System.out.printf("\ntimes: %s, result: fail\n", i);
            }

            Thread.sleep(SLEEP_MS);
        }

        System.out.println("errorTimes: " + errorTimes);
        Assert.assertTrue(String.format("errorTimes %s not in %s", errorTimes, alternativeErrorTimes),
                alternativeErrorTimes.contains(errorTimes));
    }

    @Test
    public void postAsync() throws Throwable {
        HelloRequest request = new HelloRequest();
        request.setAck(Acks.SUCCESS);
        request.setHello("world");

        int errorTimes = 0;
        for (int i = 0; i < runTimes; i++) {
            try {
                _client.helloAsync(request).get();
                System.out.printf("\ntimes: %s, result: success\n", i);
            } catch (ExecutionException e) {
                errorTimes++;
                System.out.printf("\ntimes: %s, result: fail\n", i);
            }

            Thread.sleep(SLEEP_MS);
        }

        System.out.println("errorTimes: " + errorTimes);
        Assert.assertTrue(String.format("errorTimes %s not in %s", errorTimes, alternativeErrorTimes),
                alternativeErrorTimes.contains(errorTimes));
    }

}
