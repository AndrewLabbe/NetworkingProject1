import java.io.Serializable;

import config.SocketInfo;

public class NodeStatus implements Serializable {
    // Number of seconds without heartbeat that node is considered "dead"
    public static long TIMEOUT = 30000;

    // Descriptive fields regarding node information
    private int nodeId;
    private long lastHeartbeat;
    private String[] fileNames;

    public SocketInfo socketInfo;

    public boolean hasUpdated = false;

    public boolean isHasUpdated() {
        return hasUpdated;
    }

    public void setHasUpdated(boolean hasUpdated) {
        this.hasUpdated = hasUpdated;
    }

    // Constructor to create a node status object for each node in the network
    // Explicitly given fileNames array 
    public NodeStatus(int nodeID, String[] fileNames, String ip, int port) {
        this.nodeId = nodeID;
        this.socketInfo = new SocketInfo(ip, port);
        this.lastHeartbeat = System.currentTimeMillis();
        hasUpdated = true;
        this.fileNames = fileNames;
    }

    // Alternate constructor 
    // Not given file names
    public NodeStatus(int nodeID, String ip, int port) {
        this(nodeID, new String[0], ip, port);
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
     * @param fileList : list of files names from node home directory
     * @param time     : time of the heartbeat recieved
     */
    public void updateStatus(String[] fileList, long time) {
        if (this.lastHeartbeat < time){
            this.fileNames = fileList;
            this.lastHeartbeat = time;
        }
        hasUpdated = true;
    }

    /**
     * Check if node has sent a heartbeat within the last 30 seconds
     * 
     * @return
     */
    public boolean checkAlive() {
        return System.currentTimeMillis() - lastHeartbeat < TIMEOUT;
    }

}
