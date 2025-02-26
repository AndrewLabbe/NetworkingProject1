import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class ProtocolPacket implements Serializable {

    public static final int HEADER_SIZE = 16;
    private byte version = 0;
    private byte type = 0;
    private int senderId = 0;
    private long timestamp = 0;

    private String[] fileNames = null;

    private ProtocolPacket(int senderId, long timestamp, String[] fileNames) {
        this.version = 1;
        this.type = 1;
        this.senderId = senderId;
        this.timestamp = timestamp;
        this.fileNames = fileNames;
    }

    private static byte[] serialize(final Object obj) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        try (ObjectOutputStream out = new ObjectOutputStream(bos)) {
            out.writeObject(obj);
            out.flush();
            return bos.toByteArray();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static ProtocolPacket deserializePacket(byte[] bytes)
            throws ClassNotFoundException, IOException {
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);

        ObjectInput in = new ObjectInputStream(bis);
        return (ProtocolPacket) in.readObject();
    }

    public static DatagramPacket generateDatagramPacket(int id, String[] fileNames, String ip, int port)
            throws UnknownHostException, StreamCorruptedException {
        // serialize
        ProtocolPacket packet = new ProtocolPacket(id, System.currentTimeMillis(), fileNames);
        byte[] data = ProtocolPacket.serialize(packet);
        return new DatagramPacket(data, data.length, InetAddress.getByName(ip), port);
    }

    public static void main(String[] args) throws ClassNotFoundException, IOException {
        DatagramPacket packet = ProtocolPacket.generateDatagramPacket(1, new String[] { "file1", "file2" }, "localhost",
                1234);
        ProtocolPacket obj = ProtocolPacket.deserializePacket(packet.getData());
        System.out.println(obj.senderId);
    }

    public byte getVersion() {
        return version;
    }

    public byte getType() {
        return type;
    }

    public int getSenderId() {
        return senderId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String[] getFileNames() {
        return fileNames;
    }

    public void setFileNames(String[] fileNames) {
        this.fileNames = fileNames;
    }

}
