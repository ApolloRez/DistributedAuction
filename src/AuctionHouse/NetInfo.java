package AuctionHouse;
import java.io.Serializable;

public class NetInfo implements Serializable {
    private String ip;
    private String hostname;
    private int port;

    public NetInfo(){

    }
    public NetInfo(String ip, String hostname,int port){
        this.ip = ip;
        this.hostname = hostname;
        this.port = port;
    }
    public void setPort(int port){
        this.port = port;
    }

    public void setIp(String ip){
        this.ip = ip;
    }

    public void setHostname(String hostname){
        this.hostname = hostname;
    }

    public String getIp(){
        return ip;
    }

    public String getHostname(){
        return hostname;
    }

    public int getPort(){
        return port;
    }

    @Override
    public String toString(){
        String main = new String("");
        main = main.concat(ip+"\n");
        main = main.concat(hostname+"\n");
        main = main.concat(port+"\n");
        return main;
    }
}
