package org.mydotey.rpc.response;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.mydotey.rpc.ack.Acks;
import org.mydotey.rpc.error.BadRequestException;
import org.mydotey.rpc.error.ErrorCodes;
import org.mydotey.rpc.error.ServiceException;
import org.mydotey.rpc.error.ServiceUnavailableException;

/**
 * @author koqizhao
 *
 * Jan 17, 2019
 */
@RunWith(Parameterized.class)
public class ResponseTest {

    @Parameters(name = "{index}: response={0}, errorClass={1}")
    public static Collection<Object[]> data() throws NoSuchMethodException, SecurityException {
        List<Object[]> parameterValues = new ArrayList<>();

        parameterValues.add(new Object[] { null, ServiceException.class });
        parameterValues.add(new Object[] { new TestResponse(), ServiceException.class });

        ResponseStatus responseStatus = new ResponseStatus();
        parameterValues.add(new Object[] { new TestResponse(responseStatus), ServiceException.class });

        responseStatus = new ResponseStatus();
        responseStatus.setAck(Acks.SUCCESS);
        parameterValues.add(new Object[] { new TestResponse(responseStatus), null });

        responseStatus = new ResponseStatus();
        responseStatus.setAck(Acks.PARTIAL_FAIL);
        parameterValues.add(new Object[] { new TestResponse(responseStatus), null });

        responseStatus = new ResponseStatus();
        responseStatus.setAck(Acks.FAIL);
        parameterValues.add(new Object[] { new TestResponse(responseStatus), ServiceException.class });

        responseStatus = new ResponseStatus();
        responseStatus.setAck(Acks.FAIL);
        responseStatus.setError(new ResponseError());
        parameterValues.add(new Object[] { new TestResponse(responseStatus), ServiceException.class });

        responseStatus = new ResponseStatus();
        responseStatus.setAck(Acks.FAIL);
        responseStatus.setError(new ResponseError());
        responseStatus.getError().setCode(ErrorCodes.BAD_REQUEST);
        parameterValues.add(new Object[] { new TestResponse(responseStatus), BadRequestException.class });

        responseStatus = new ResponseStatus();
        responseStatus.setAck(Acks.FAIL);
        responseStatus.setError(new ResponseError());
        responseStatus.getError().setCode(ErrorCodes.SERVICE_EXCEPTION);
        parameterValues.add(new Object[] { new TestResponse(responseStatus), ServiceException.class });

        responseStatus = new ResponseStatus();
        responseStatus.setAck(Acks.FAIL);
        responseStatus.setError(new ResponseError());
        responseStatus.getError().setCode(ErrorCodes.SERVICE_UNAVAILABLE);
        parameterValues.add(new Object[] { new TestResponse(responseStatus), ServiceUnavailableException.class });

        responseStatus = new ResponseStatus();
        responseStatus.setAck(Acks.FAIL);
        responseStatus.setError(new ResponseError());
        responseStatus.getError().setCode("unknown");
        parameterValues.add(new Object[] { new TestResponse(responseStatus), ServiceException.class });

        responseStatus = new ResponseStatus();
        responseStatus.setAck(Acks.FAIL);
        responseStatus.setError(new ResponseError());
        responseStatus.getError().setCode("some-code");
        responseStatus.getError().setMessage("some-message");
        parameterValues.add(new Object[] { new TestResponse(responseStatus), ServiceException.class });

        return parameterValues;
    }

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Parameter(0)
    public Response response;

    @Parameter(1)
    public Class<Throwable> errorClass;

    @Test
    public void checkResponse() {
        if (errorClass != null)
            expectedException.expect(errorClass);

        try {
            Response.check(response);
        } catch (Throwable e) {
            System.out.println();
            System.out.println(e);
            System.out.println();
            throw e;
        }
    }

}
