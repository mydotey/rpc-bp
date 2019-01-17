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

    static void check(Response response) {
        if (response == null)
            throw new ServiceException("response is null");

        if (response.getStatus() == null)
            throw new ServiceException("response no status");

        if (StringExtension.isBlank(response.getStatus().getAck()))
            throw new ServiceException("response status no ack: " + response.getStatus());

        if (Acks.isFail(response.getStatus().getAck())) {
            ResponseError error = response.getStatus().getError();
            String message = "unknown error";
            if (error == null)
                throw new ServiceException(message + ", status: " + response.getStatus());

            if (!StringExtension.isBlank(error.getMessage()))
                message = error.getMessage();

            String errorCode = error.getCode();
            if (errorCode == null)
                throw new ServiceException(message + ", status: " + response.getStatus());

            switch (errorCode) {
                case ErrorCodes.BAD_REQUEST:
                    throw new BadRequestException(message);
                case ErrorCodes.SERVICE_EXCEPTION:
                    throw new ServiceException(message);
                case ErrorCodes.SERVICE_UNAVAILABLE:
                    throw new ServiceUnavailableException(message);
                default:
                    throw new ServiceException("errorCode: " + error.getCode() + ", message: " + message);
            }
        }
    }

}
