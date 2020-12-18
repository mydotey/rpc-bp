package org.mydotey.rpc.client.http;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
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
import org.mydotey.rpc.client.http.helloservice.HelloResponse;
import org.mydotey.rpc.client.http.helloservice.HelloService;
import org.mydotey.rpc.client.http.helloservice.HelloServiceClient;
import org.mydotey.rpc.error.BadRequestException;
import org.mydotey.rpc.error.ServiceException;
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
public class HttpServiceClientTest {

    @Parameters(name = "{index}: serverPorts={0}, ttl={1}, updateInterval={2}, hello={3}, ack={4}, errorClass={5}, runTimes={6}, runInterval={7}")
    public static Collection<Object[]> data() throws NoSuchMethodException, SecurityException {
        List<Object[]> parameterValues = new ArrayList<>();
        List<Integer> serverPorts = Arrays.asList(18085);

        parameterValues.add(new Object[] { serverPorts, 1, 0, null, Acks.SUCCESS, BadRequestException.class, 1, 0 });
        parameterValues.add(new Object[] { serverPorts, 1, 0, null, Acks.SUCCESS, BadRequestException.class, 10, 0 });
        parameterValues.add(new Object[] { serverPorts, 1, 0, "world", Acks.SUCCESS, null, 1, 0 });
        parameterValues.add(new Object[] { serverPorts, 1, 0, "world", Acks.SUCCESS, null, 10, 0 });
        parameterValues.add(new Object[] { serverPorts, 1, 0, "world", Acks.FAIL, ServiceException.class, 1, 0 });
        parameterValues.add(new Object[] { serverPorts, 1, 0, "world", Acks.FAIL, ServiceException.class, 10, 0 });
        parameterValues.add(new Object[] { serverPorts, 1, 50, "world", Acks.SUCCESS, null, 10, 10 });
        parameterValues.add(new Object[] { serverPorts, 50, 0, "world", Acks.SUCCESS, null, 10, 10 });
        parameterValues.add(new Object[] { serverPorts, 50, 10, "world", Acks.SUCCESS, null, 10, 10 });
        parameterValues.add(new Object[] { serverPorts, 50, 50, "world", Acks.SUCCESS, null, 10, 10 });

        serverPorts = Arrays.asList(18085, 18086);

        parameterValues.add(new Object[] { serverPorts, 1, 0, null, Acks.SUCCESS, BadRequestException.class, 1, 0 });
        parameterValues.add(new Object[] { serverPorts, 1, 0, null, Acks.SUCCESS, BadRequestException.class, 10, 0 });
        parameterValues.add(new Object[] { serverPorts, 1, 0, "world", Acks.SUCCESS, null, 1, 0 });
        parameterValues.add(new Object[] { serverPorts, 1, 0, "world", Acks.SUCCESS, null, 10, 0 });
        parameterValues.add(new Object[] { serverPorts, 1, 0, "world", Acks.FAIL, ServiceException.class, 1, 0 });
        parameterValues.add(new Object[] { serverPorts, 1, 0, "world", Acks.FAIL, ServiceException.class, 10, 0 });
        parameterValues.add(new Object[] { serverPorts, 1, 50, "world", Acks.SUCCESS, null, 10, 10 });
        parameterValues.add(new Object[] { serverPorts, 50, 0, "world", Acks.SUCCESS, null, 10, 10 });
        parameterValues.add(new Object[] { serverPorts, 50, 10, "world", Acks.SUCCESS, null, 10, 10 });
        parameterValues.add(new Object[] { serverPorts, 50, 50, "world", Acks.SUCCESS, null, 10, 10 });

        return parameterValues;
    }

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Parameter(0)
    public List<Integer> serverPorts;

    @Parameter(1)
    public long ttl;

    @Parameter(2)
    public long updateInterval;

    @Parameter(3)
    public String hello;

    @Parameter(4)
    public String ack;

    @Parameter(5)
    public Class<Throwable> errorClass;

    @Parameter(6)
    public int runTimes;

    @Parameter(7)
    public long runInterval;

    private List<HelloApp> _apps;

    private HelloServiceClient _client;

    @Before
    public void setUp() {
        _apps = new ArrayList<>();
        List<String> serviceUrls = new ArrayList<>();
        for (int port : serverPorts) {
            HelloApp app = new HelloApp(port);
            app.start();
            _apps.add(app);
            serviceUrls.add("http://localhost:" + port);
        }

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
    public void get() {
        for (int i = 0; i < runTimes; i++) {
            HelloResponse response = _client.hello();
            Assert.assertEquals(Acks.SUCCESS, response.getStatus().getAck());
            Assert.assertEquals(HelloService.ANONYMOUS, response.getData());
        }
    }

    @Test
    public void post() {
        HelloRequest request = new HelloRequest();
        request.setAck(ack);
        request.setHello(hello);

        if (errorClass != null)
            expectedException.expect(errorClass);

        for (int i = 0; i < runTimes; i++) {
            HelloResponse response = _client.hello(request);
            Assert.assertEquals(ack, response.getStatus().getAck());
            Assert.assertEquals(hello, response.getData());
        }
    }

    @Test
    public void getAsync() throws InterruptedException, ExecutionException {
        for (int i = 0; i < runTimes; i++) {
            HelloResponse response = _client.helloAsync().get();
            Assert.assertEquals(Acks.SUCCESS, response.getStatus().getAck());
            Assert.assertEquals(HelloService.ANONYMOUS, response.getData());
        }
    }

    @Test
    public void postAsync() throws Throwable {
        HelloRequest request = new HelloRequest();
        request.setAck(ack);
        request.setHello(hello);

        if (errorClass != null)
            expectedException.expect(errorClass);

        for (int i = 0; i < runTimes; i++) {
            HelloResponse response;
            try {
                response = _client.helloAsync(request).get();
            } catch (ExecutionException e) {
                throw e.getCause();
            }
            Assert.assertEquals(ack, response.getStatus().getAck());
            Assert.assertEquals(hello, response.getData());
        }
    }

}
