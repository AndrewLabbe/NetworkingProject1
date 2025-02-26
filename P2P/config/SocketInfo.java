package config;

/**
 * SocketInfo class to store ip and port of nodes/clients/server
 */
public class SocketInfo {
    private String ip;
    private int port;

    /**
     * 
     * @param ip
     * @param port
     */
    public SocketInfo(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public boolean equals(SocketInfo socketInfo) {
        System.out.println(socketInfo);
        if (socketInfo.getIp().equals("localhost"))
            return this.port == socketInfo.getPort();
        return this.ip.equals(socketInfo.getIp()) && this.port == socketInfo.getPort();
    }

    @Override
    public String toString() {
        return "SocketInfo [ip=" + ip + ", port=" + port + "]";
    }

}