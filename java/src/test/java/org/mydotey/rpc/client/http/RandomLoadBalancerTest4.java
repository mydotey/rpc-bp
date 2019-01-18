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
import org.mydotey.java.StringExtension;
import org.mydotey.rpc.client.http.HttpLoadBalancer.HttpExecutionContext;
import org.mydotey.rpc.error.BadRequestException;

/**
 * @author koqizhao
 *
 * Jan 17, 2019
 */
@RunWith(Parameterized.class)
public class RandomLoadBalancerTest4 {

    @Parameters(name = "{index}: errorClass={0}")
    public static Collection<Object[]> data() throws NoSuchMethodException, SecurityException {
        List<Object[]> parameterValues = new ArrayList<>();

        parameterValues.add(new Object[] { null });
        parameterValues.add(new Object[] { HttpRuntimeException.class });
        parameterValues.add(new Object[] { BadRequestException.class });

        return parameterValues;
    }

    @Parameter(0)
    public Class<Throwable> errorClass;

    private RandomLoadBalancer _loadBalancer;

    @Before
    public void setUp() {
        _loadBalancer = new RandomLoadBalancer(Arrays.asList("url1", "url2"), 1, 0);
    }

    @Test
    public void executionContextData() throws InterruptedException {
        HttpExecutionContext executionContext = _loadBalancer.newExecutionContext();
        try {
            String currentUrl = executionContext.getServiceUrl();
            Assert.assertFalse(StringExtension.isBlank(currentUrl));
            Assert.assertNotEquals(0, executionContext.getStartTime());
            Assert.assertEquals(0, executionContext.getEndTime());
            Assert.assertNull(executionContext.getExecutionError());

            if (errorClass != null)
                throw errorClass.newInstance();
        } catch (Throwable e) {
            executionContext.setExecutionError(e);
        } finally {
            executionContext.complete();
            Assert.assertNotEquals(0, executionContext.getStartTime());
            Assert.assertNotEquals(0, executionContext.getEndTime());
            Assert.assertTrue(executionContext.getEndTime() >= executionContext.getStartTime());

            if (errorClass != null)
                Assert.assertNotNull(executionContext.getExecutionError());
            else
                Assert.assertNull(executionContext.getExecutionError());

            Thread.sleep(10);

            long endTime = executionContext.getEndTime();
            executionContext.complete();
            Assert.assertEquals(endTime, executionContext.getEndTime());
        }
    }

}
