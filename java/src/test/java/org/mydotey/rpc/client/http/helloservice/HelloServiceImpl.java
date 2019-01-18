package org.mydotey.rpc.client.http.helloservice;

import org.mydotey.java.StringExtension;
import org.mydotey.rpc.ack.Acks;
import org.mydotey.rpc.error.ErrorCodes;
import org.mydotey.rpc.response.ResponseError;
import org.mydotey.rpc.response.ResponseStatus;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author koqizhao
 *
 * Sep 21, 2018
 */
@RestController
@EnableAutoConfiguration
public class HelloServiceImpl implements HelloService {

    @Override
    @RequestMapping(path = "/hello", method = RequestMethod.POST)
    public HelloResponse hello(@RequestBody HelloRequest request) {
        return handleRequest(request);
    }

    @Override
    @RequestMapping(path = "/hello", method = RequestMethod.GET)
    public HelloResponse hello() {
        return handleRequest(null);
    }

    protected HelloResponse handleRequest(HelloRequest request) {
        HelloResponse response = new HelloResponse();
        response.setStatus(new ResponseStatus());
        if (request == null) {
            response.setData(ANONYMOUS);
            response.getStatus().setAck(Acks.SUCCESS);
            return response;
        }

        if (StringExtension.isBlank(request.getHello())) {
            response.getStatus().setAck(Acks.FAIL);
            ResponseError error = new ResponseError();
            response.getStatus().setError(error);
            error.setCode(ErrorCodes.BAD_REQUEST);
            error.setMessage("hello is empty");
            return response;
        }

        String ack = StringExtension.isBlank(request.getAck()) ? Acks.SUCCESS : request.getAck();
        response.getStatus().setAck(ack);
        if (Acks.isFail(ack)) {
            ResponseError error = new ResponseError();
            response.getStatus().setError(new ResponseError());
            error.setCode(ErrorCodes.SERVICE_EXCEPTION);
            error.setMessage("some exception happened when request was handled");
            return response;
        }

        response.setData(request.getHello());
        return response;
    }

}
