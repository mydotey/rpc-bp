package org.mydotey.rpc.response;

/**
 * @author koqizhao
 *
 * Jan 17, 2019
 */
public class TestResponse implements Response {

    private ResponseStatus _status;

    public TestResponse() {

    }

    public TestResponse(ResponseStatus status) {
        _status = status;
    }

    @Override
    public ResponseStatus getStatus() {
        return _status;
    }

    @Override
    public String toString() {
        return "TestResponse [_status=" + _status + "]";
    }

}
