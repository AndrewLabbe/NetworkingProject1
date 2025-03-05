import java.io.Serializable;

import config.SocketInfo;

public class NodeStatus implements Serializable {
    public static long TIMEOUT = 30000;

    private int nodeId;
    private long lastHeartbeat;
    private String[] fileNames;

    public SocketInfo socketInfo;

    public NodeStatus(int nodeID, String[] fileNames, String ip, int port) {
        this.nodeId = nodeID;
        this.socketInfo = new SocketInfo(ip, port);
        this.lastHeartbeat = System.currentTimeMillis();
        this.fileNames = fileNames;
    }

    public NodeStatus(int nodeID, String ip, int port) {
        this.nodeId = nodeID;
        this.socketInfo = new SocketInfo(ip, port);
        this.lastHeartbeat = System.currentTimeMillis();
        this.fileNames = new String[0];
    }

    public int getNodeId() {
        return nodeId;
    }

    public long getLastHeartbeat() {
        return lastHeartbeat;
    }

    public String[] getFileList() {
        return fileNames;
    }

    /**
     *
     * @param fileList : list of files namnes from node home dir
     * @param time     : time of the heartbeat recieved
     */
    public void updateStatus(String[] fileList, long time) {
        this.fileNames = fileList;
        if (this.lastHeartbeat < time)
            this.lastHeartbeat = time;
    }

    /**
     * Check if node has sent a heartbeat within the last 30 seconds
     * 
     * @return
     */
    public boolean checkAlive() {
        return System.currentTimeMillis() - lastHeartbeat < TIMEOUT;
    }

    public void setLastHeartbeat(long timeStamp) {
        this.lastHeartbeat = timeStamp;
    }
}
