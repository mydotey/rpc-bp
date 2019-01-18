package org.mydotey.rpc.client.http.helloservice;

/**
 * @author koqizhao
 *
 * Jan 18, 2019
 */
public interface HelloService {

    String ANONYMOUS = "anonymous";

    HelloResponse hello(HelloRequest request);

    HelloResponse hello();

}