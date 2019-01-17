package org.mydotey.rpc.client.http;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.mydotey.rpc.client.http.HttpLoadBalancer.HttpExecutionContext;

/**
 * @author koqizhao
 *
 * Jan 17, 2019
 */
@RunWith(Parameterized.class)
public class RandomLoadBalancerTest2 {

    @Parameters(name = "{index}: serviceUrls={0}, ttl={1}, updateInterval={2}, duplicate={3}, sleepMs={4}")
    public static Collection<Object[]> data() throws NoSuchMethodException, SecurityException {
        List<Object[]> parameterValues = new ArrayList<>();

        List<String> serviceUrls = Arrays.asList("url1", "url2");
        parameterValues.add(new Object[] { serviceUrls, 0, 0, false, 1 });

        serviceUrls = Arrays.asList("url1");
        parameterValues.add(new Object[] { serviceUrls, 0, 0, true, 1 });

        serviceUrls = Arrays.asList("url1", "url2");
        parameterValues.add(new Object[] { serviceUrls, 100, 0, true, 1 });
        parameterValues.add(new Object[] { serviceUrls, 100, 0, false, 101 });

        return parameterValues;
    }

    @Parameter(0)
    public List<String> serviceUrls;

    @Parameter(1)
    public long ttl;

    @Parameter(2)
    public long updateInterval;

    @Parameter(3)
    public boolean duplicate;

    @Parameter(4)
    public long sleepMs;

    private RandomLoadBalancer _loadBalancer;
    private int _times = 10;

    @Before
    public void setUp() {
        _loadBalancer = new RandomLoadBalancer(serviceUrls, ttl, updateInterval);
    }

    @Test
    public void getServiceUrl() throws InterruptedException {
        String oldUrl = null;
        for (int i = 0; i < _times; i++) {
            HttpExecutionContext executionContext = _loadBalancer.newExecutionContext();
            try {
                String currentUrl = executionContext.getServiceUrl();
                if (oldUrl != null) {
                    if (duplicate)
                        Assert.assertEquals(oldUrl, currentUrl);
                    else
                        Assert.assertNotEquals(oldUrl, currentUrl);
                }

                oldUrl = currentUrl;

                if (sleepMs > 0)
                    Thread.sleep(sleepMs);
            } finally {
                executionContext.complete();
            }
        }
    }

}
