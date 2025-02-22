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

}
