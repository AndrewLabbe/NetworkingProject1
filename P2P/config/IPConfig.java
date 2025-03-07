package config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

public class IPConfig {

    private static File configFile = new File("P2PConfig.properties");
    private static Properties config = null;
    public static int DEFAULT_PORT = 9876;

    private static SocketInfo[] socketInfos = null;

    // "static class" equiv, prevents instantiation
    private IPConfig() {
    };

    /**
     * 
     * @return num of ip port pairs loaded from config
     * @throws IOException
     */
    public static int num_sockets() throws IOException {
        if (socketInfos == null) {
            try {
                loadSockets();
            } catch (IOException e) {
                throw e;
            }
        }
        return socketInfos.length;
    }

    /**
     * Loads from config ip port pairs to socketInfos
     *
     * @throws IOException
     */
    private static void loadSockets() throws IOException {
        System.out.println("Loading sockets (if no port provided for a node, default port is " + DEFAULT_PORT + ")");

        if (config != null)
            return;
        try {
            config = new Properties();
            FileInputStream propsInput = new FileInputStream(configFile);
            config.load(propsInput);
        } catch (IOException e) {
            System.out.println();
            System.err.println(
                    "ERROR: Config file not valid, make sure to create and properly configure the file: "
                            + configFile.getPath());
            System.exit(1);
        }
        int index = 0;
        ArrayList<SocketInfo> sockets = new ArrayList<>();
        System.out.println("Loading sockets");
        do {
            try {
                String nodeKey = "node" + index + ".";

                String ip = config.getProperty(nodeKey + "ip");
                if (ip == null) {
                    if (index == 0) {
                        System.out.println();
                        System.err.println(
                                "ERROR: No ips found in config file, make sure to create and properly configure the file:"
                                        + configFile.getPath());
                        System.exit(1);
                    }
                    break;
                }
                int port = DEFAULT_PORT;
                if (config.getProperty(nodeKey + "port") != null) {
                    port = Integer.parseInt(config.getProperty(nodeKey + "port"));
                }

                sockets.add(new SocketInfo(ip, port));
                index++;
            } catch (Exception e) {
                throw new IOException("Error loading sockets from config file please check if config is valid");
            }
        } while (true);

        if (sockets.size() == 0) {
            System.out.println();
            System.err.println(
                    "ERROR: No sockets found in config file, make sure to create and properly configure the file:"
                            + configFile.getPath());
            System.exit(1);
        }
        socketInfos = sockets.toArray(new SocketInfo[sockets.size()]);
    }

    /**
     * 
     * @param index
     * @return SocketInfo object with ip and port
     * @throws IOException
     */
    public static SocketInfo getNodeSocket(int index) throws IOException {
        if (socketInfos == null) {
            try {
                loadSockets();
            } catch (IOException e) {
                throw e;
            }
        }
        return socketInfos[index];
    }

    public static void main(String[] args) {
        System.out.println("Testing IPConfig...");
        try {
            for (int i = 0; i < IPConfig.num_sockets(); i++) {
                System.out.println(IPConfig.getNodeSocket(i).getIp() + ":" + IPConfig.getNodeSocket(i).getPort());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
