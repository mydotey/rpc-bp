package org.mydotey.rpc.client.http.helloservice;

/**
 * @author koqizhao
 *
 * Nov 8, 2018
 */
public class HelloRequest {

    private String hello;
    private String ack;

    public String getHello() {
        return hello;
    }

    public void setHello(String hello) {
        this.hello = hello;
    }

    public String getAck() {
        return ack;
    }

    public void setAck(String ack) {
        this.ack = ack;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((ack == null) ? 0 : ack.hashCode());
        result = prime * result + ((hello == null) ? 0 : hello.hashCode());
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
        HelloRequest other = (HelloRequest) obj;
        if (ack == null) {
            if (other.ack != null)
                return false;
        } else if (!ack.equals(other.ack))
            return false;
        if (hello == null) {
            if (other.hello != null)
                return false;
        } else if (!hello.equals(other.hello))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "HelloRequest [hello=" + hello + ", ack=" + ack + "]";
    }

}
