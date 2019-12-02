package shared;

import java.io.Serializable;

/**
 * This class contains the ip, hostname, and port of an Auction House server
 */
public class NetInfo implements Serializable {
    private String ip;
    private int port;

    public NetInfo(){

    }
    public NetInfo(String ip,int port){
        this.ip = ip;
        this.port = port;
    }
    public void setPort(int port){
        this.port = port;
    }

    public void setIp(String ip){
        this.ip = ip;
    }

    public String getIp(){
        return ip;
    }

    public int getPort(){
        return port;
    }

    @Override
    public String toString() {
        return "NetInfo{" +
                "ip='" + ip + '\'' +
                ", port=" + port +
                '}';
    }
}
