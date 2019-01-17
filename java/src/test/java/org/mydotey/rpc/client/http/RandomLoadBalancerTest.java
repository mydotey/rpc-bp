package org.mydotey.rpc.client.http;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

/**
 * @author koqizhao
 *
 * Jan 17, 2019
 */
@RunWith(Parameterized.class)
public class RandomLoadBalancerTest {

    @Parameters(name = "{index}: serviceUrls={0}, ttl={1}, updateInterval={2}, errorClass={3}")
    public static Collection<Object[]> data() throws NoSuchMethodException, SecurityException {
        List<Object[]> parameterValues = new ArrayList<>();

        parameterValues.add(new Object[] { null, 0, 0, IllegalArgumentException.class });
        parameterValues.add(new Object[] { new ArrayList<>(), 0, 0, IllegalArgumentException.class });

        List<String> serviceUrls = Arrays.asList("url1", "url2");
        parameterValues.add(new Object[] { serviceUrls, 0, 0, null });
        parameterValues.add(new Object[] { serviceUrls, -1, 0, IllegalArgumentException.class });
        parameterValues.add(new Object[] { serviceUrls, 0, -1, IllegalArgumentException.class });
        parameterValues.add(new Object[] { serviceUrls, -1, -1, IllegalArgumentException.class });
        parameterValues.add(new Object[] { serviceUrls, 1, 1, null });

        serviceUrls = Arrays.asList("url1");
        parameterValues.add(new Object[] { serviceUrls, 0, 0, null });

        return parameterValues;
    }

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Parameter(0)
    public List<String> serviceUrls;

    @Parameter(1)
    public long ttl;

    @Parameter(2)
    public long updateInterval;

    @Parameter(3)
    public Class<Throwable> errorClass;

    @Test
    public void emptyServiceUrls() {
        if (errorClass != null)
            expectedException.expect(errorClass);

        new RandomLoadBalancer(serviceUrls, ttl, updateInterval);
    }

}
