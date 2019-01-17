package org.mydotey.rpc.client.http;

/**
 * @author koqizhao
 *
 * Jan 16, 2019
 */
public interface HttpLoadBalancer {

    HttpExecutionContext newExecutionContext();

    interface HttpExecutionContext {

        String getServiceUrl();

        long getStartTime();

        long getEndTime();

        Throwable getExecutionError();

        void setExecutionError(Throwable executionError);

        void complete();

    }

}
