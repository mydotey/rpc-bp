package org.mydotey.rpc.response;

import org.mydotey.java.StringExtension;
import org.mydotey.rpc.ack.Acks;
import org.mydotey.rpc.error.BadRequestException;
import org.mydotey.rpc.error.ErrorCodes;
import org.mydotey.rpc.error.ServiceException;
import org.mydotey.rpc.error.ServiceUnavailableException;

/**
 * @author koqizhao
 *
 * Dec 18, 2018
 */
public interface Response {

    ResponseStatus getStatus();

    static void checkResponse(Response response) {
        String message = "unknown error";
        if (response == null)
            throw new ServiceException(message);

        if (response.getStatus() == null)
            throw new ServiceException("no status, response: " + response);

        if (Acks.isFail(response.getStatus().getAck())) {
            ResponseError error = response.getStatus().getError();
            if (error == null)
                throw new ServiceException("no error, response: " + response);

            if (!StringExtension.isBlank(error.getMessage()))
                message = error.getMessage();

            String errorCode = error.getCode();
            if (errorCode == null)
                throw new ServiceException("no error code, response: " + response);

            switch (errorCode) {
                case ErrorCodes.BAD_REQUEST:
                    throw new BadRequestException(error.getMessage());
                case ErrorCodes.SERVICE_EXCEPTION:
                    throw new ServiceException(error.getMessage());
                case ErrorCodes.SERVICE_UNAVAILABLE:
                    throw new ServiceUnavailableException(error.getMessage());
                default:
                    throw new ServiceException("errorCode: " + error.getCode() + ", message: " + message);
            }
        }
    }

}
