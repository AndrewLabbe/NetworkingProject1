import java.net.DatagramPacket;

public class ProtocolPacket {

    public static final int HEADER_SIZE = 16;
    private byte version;
    private byte type;
    private int senderId;
    private long timestamp;
    private short dataLength;
    private byte[] data;
    private NodeInfo info;
    private DatagramPacket packet;

    public ProtocolPacket(byte version, byte type, int senderId, long timestamp, byte[] data, NodeInfo info) {
        this.version = version;
        this.type = type;
        this.senderId = senderId;
        this.timestamp = timestamp;
        this.data = data;
        this.dataLength = (short) data.length;
        this.info = info;
        generatePacket(data);
    }

    public void generatePacket(byte[] data){
        packet = new DatagramPacket(data, data.length);
    }

    public static int getHeaderSize() {
        return HEADER_SIZE;
    }

    public byte getVersion() {
        return version;
    }

    public void setVersion(byte version) {
        this.version = version;
    }

    public byte getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }

    public int getSenderId() {
        return senderId;
    }

    public void setSenderId(int senderId) {
        this.senderId = senderId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public short getDataLength() {
        return dataLength;
    }

    public void setDataLength(short dataLength) {
        this.dataLength = dataLength;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public NodeInfo getInfo() {
        return info;
    }

    public void setInfo(NodeInfo info) {
        this.info = info;
    }

    public DatagramPacket getPacket() {
        return packet;
    }

    public void setPacket(DatagramPacket packet) {
        this.packet = packet;
    }

    

}
