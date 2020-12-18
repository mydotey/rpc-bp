package org.mydotey.rpc.client.http.helloservice;

import java.io.IOException;

import org.junit.Test;

/**
 * @author koqizhao
 *
 *         Nov 8, 2018
 */
public class HelloAppTest {

	protected int getServerPort() {
		return 8085;
	}

	@Test
	public void testServerStart() throws IOException, InterruptedException {
		int serverPort = getServerPort();
		try (HelloApp app = new HelloApp(serverPort);) {
			app.start();
			Thread.sleep(5 * 1000);
		}
	}

}
