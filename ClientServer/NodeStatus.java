import config.SocketInfo;

public class NodeStatus {
    public static long TIMEOUT = 30000;

    private int nodeId;
    private long lastHeartbeat;
    private String[] fileList;

    public SocketInfo socketInfo;

    public NodeStatus(int nodeID, String ip, int port) {
        this.nodeId = nodeID;
        this.socketInfo = new SocketInfo(ip, port);
        this.lastHeartbeat = System.currentTimeMillis();
        this.fileList = new String[0];
    }

    public int getNodeId() {
        return nodeId;
    }

    public long getLastHeartbeat() {
        return lastHeartbeat;
    }

    public String[] getFileList() {
        return fileList;
    }

    /**
     *
     * @param fileList : list of files namnes from node home dir
     * @param time : time of the heartbeat recieved
     */
    public void updateStatus(String[] fileList, long time) {
        this.fileList = fileList;
        if(this.lastHeartbeat < time)
            this.lastHeartbeat = time;
    }


    /**
     * Check if node has sent a heartbeat within the last 30 seconds
     * @return
     */
    public boolean checkAlive() {
        return System.currentTimeMillis() - lastHeartbeat < TIMEOUT;
    }
}
