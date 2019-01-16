package org.mydotey.rpc.client.http;

/**
 * @author koqizhao
 *
 * Jan 16, 2019
 */
public interface HttpLoadBalancer {

    String getServiceUrl();

    void forceUpdate();

}
