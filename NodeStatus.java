import java.util.ArrayList;
import java.util.List;

import config.SocketInfo;

public class NodeStatus {

    public long lastHeartbeat;
    public List<String> fileList;
    public boolean isAlive;

    public SocketInfo socketInfo;

    public NodeStatus(String ip, int port) {
        this.socketInfo = new SocketInfo(ip, port);
        this.lastHeartbeat = System.currentTimeMillis();
        this.fileList = new ArrayList<>();
        this.isAlive = true;
    }

}
