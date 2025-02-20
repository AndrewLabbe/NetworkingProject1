
public class ProtocolPacket {

    public static final int HEADER_SIZE = 16;
    private byte version;
    private byte type;
    private int senderId;
    private long timestamp;
    private short dataLength;
    private String data;

    public ProtocolPacket(byte version, byte type, int senderId, long timestamp, String data) {
        this.version = version;
        this.type = type;
        this.senderId = senderId;
        this.timestamp = timestamp;
        this.data = data;
        this.dataLength = (short) data.length();
    }

}
