import java.util.ArrayList;
import java.util.List;

import config.SocketInfo;

public class NodeInfo {

    public int nodeId;
    public long lastHeartbeat;
    public List<String> fileList;
    public boolean isAlive;

    // public String ip;
    // public int port;

    public SocketInfo socketInfo;

    public NodeInfo(int nodeId, String ip, int port) {
        this.nodeId = nodeId;
        this.socketInfo = new SocketInfo(ip, port);
        this.lastHeartbeat = System.currentTimeMillis();
        this.fileList = new ArrayList<>();
        this.isAlive = true;
    }

    public boolean isSocket(String ip, int port) {
        return this.socketInfo.getIp().equals(ip) && this.socketInfo.getPort() == port;
    }

}
