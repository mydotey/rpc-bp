package org.mydotey.rpc.client.http.helloservice;

import org.mydotey.rpc.response.Response;
import org.mydotey.rpc.response.ResponseStatus;

/**
 * @author koqizhao
 *
 * Nov 8, 2018
 */
public class HelloResponse implements Response {

    private String data;
    private ResponseStatus status;

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    @Override
    public ResponseStatus getStatus() {
        return status;
    }

    public void setStatus(ResponseStatus status) {
        this.status = status;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((data == null) ? 0 : data.hashCode());
        result = prime * result + ((status == null) ? 0 : status.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        HelloResponse other = (HelloResponse) obj;
        if (data == null) {
            if (other.data != null)
                return false;
        } else if (!data.equals(other.data))
            return false;
        if (status == null) {
            if (other.status != null)
                return false;
        } else if (!status.equals(other.status))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "HelloResponse [data=" + data + ", status=" + status + "]";
    }

}
