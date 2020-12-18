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
import org.mydotey.rpc.error.BadRequestException;
import org.mydotey.rpc.error.ServiceUnavailableException;

/**
 * @author koqizhao
 *
 *         Jan 17, 2019
 */
@RunWith(Parameterized.class)
public class RandomLoadBalancerTest3 {

	@Parameters(name = "{index}: serviceUrls={0}, ttl={1}, updateInterval={2}, duplicate={3}, sleepMs={4}, errorClass={5}")
	public static Collection<Object[]> data() throws NoSuchMethodException, SecurityException {
		List<Object[]> parameterValues = new ArrayList<>();

		List<String> serviceUrls = Arrays.asList("url1", "url2");
		parameterValues.add(new Object[] { serviceUrls, 1, 0, false, 0, HttpRuntimeException.class });
		parameterValues.add(new Object[] { serviceUrls, 1, 0, false, 0, ServiceUnavailableException.class });
		parameterValues.add(new Object[] { serviceUrls, 1, 0, true, 0, BadRequestException.class });
		parameterValues.add(new Object[] { serviceUrls, 100, 0, false, 0, HttpRuntimeException.class });
		parameterValues.add(new Object[] { serviceUrls, 100, 0, false, 0, ServiceUnavailableException.class });
		parameterValues.add(new Object[] { serviceUrls, 100, 0, true, 0, BadRequestException.class });

		serviceUrls = Arrays.asList("url1");
		parameterValues.add(new Object[] { serviceUrls, 1, 0, true, 0, HttpRuntimeException.class });
		parameterValues.add(new Object[] { serviceUrls, 100, 0, true, 0, HttpRuntimeException.class });
		parameterValues.add(new Object[] { serviceUrls, 1, 100, true, 0, HttpRuntimeException.class });
		parameterValues.add(new Object[] { serviceUrls, 1, 100, true, 50, HttpRuntimeException.class });
		parameterValues.add(new Object[] { serviceUrls, 1, 100, true, 150, HttpRuntimeException.class });

		serviceUrls = Arrays.asList("url1", "url2");
		parameterValues.add(new Object[] { serviceUrls, 100, 50, true, 0, HttpRuntimeException.class });
		parameterValues.add(new Object[] { serviceUrls, 100, 50, false, 51, HttpRuntimeException.class });

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

	@Parameter(5)
	public Class<Throwable> errorClass;

	private RandomLoadBalancer _loadBalancer;
	private int _times = 10;

	@Before
	public void setUp() {
		_loadBalancer = new RandomLoadBalancer(serviceUrls, ttl, updateInterval);
	}

	@SuppressWarnings("deprecation")
	@Test
	public void forceUpdate() throws InterruptedException {
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

				throw errorClass.newInstance();
			} catch (Throwable e) {
				executionContext.setExecutionError(e);
			} finally {
				executionContext.complete();
			}
		}
	}

}
