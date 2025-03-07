package config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

public class IPConfig {

    private static File configFile = new File("ClientServerConfig.properties");
    private static Properties config = null;
    public static int DEFAULT_PORT = 9876;

    private static SocketInfo[] clientSocketInfos = null;
    private static SocketInfo serverSocketInfo = null;

    // "static class" equiv, prevents instantiation
    private IPConfig() {
    };

    /**
     *
     * @return num of ip port pairs loaded from config
     * @throws IOException
     */
    public static int num_sockets() throws IOException {
        if (clientSocketInfos == null) {
            try {
                loadSockets();
            } catch (IOException e) {
                throw e;
            }
        }
        return clientSocketInfos.length;
    }

    /**
     * Loads from config ip port pairs to socketInfos
     *
     * @throws IOException
     */

    private static void loadSockets() throws IOException {
        System.out.println(
                "Loading sockets (if no port provided for a server/client, default port is " + DEFAULT_PORT + ")");
        if (config != null)
            return;
        try {
            config = new Properties();
            FileInputStream propsInput = new FileInputStream(configFile);
            config.load(propsInput);
            String ip = config.getProperty("server.ip");
            if (ip == null) {
                System.err.println("ERROR: No server ip provided in config file");
                System.exit(1);
            }
            // def port if none provided
            int port = DEFAULT_PORT;
            if (config.getProperty("server.port") != null) {
                port = Integer.parseInt(config.getProperty("server.port"));
            }
            serverSocketInfo = new SocketInfo(ip, port);
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
                String clientKey = "client" + index + ".";
                String ip = config.getProperty(clientKey + "ip");
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
                if (config.getProperty(clientKey + "port") != null) {
                    port = Integer.parseInt(config.getProperty(clientKey + "port"));
                }
                sockets.add(new SocketInfo(ip, port));
                index++;
            } catch (Exception e) {
                throw new IOException("Error loading sockets from config file please check if config is valid");
            }
        } while (true);

        if (sockets.size() == 0) {
            throw new IOException("No sockets found in config file");
        }
        clientSocketInfos = sockets.toArray(new SocketInfo[sockets.size()]);
    }

    /**
     *
     * @param index
     * @return SocketInfo object with ip and port
     * @throws IOException
     */
    public static SocketInfo getClientSocket(int index) throws IOException {
        if (clientSocketInfos == null) {
            try {
                loadSockets();
            } catch (IOException e) {
                throw e;
            }
        }
        return clientSocketInfos[index];
    }

    public static SocketInfo getServerSocket() throws IOException {
        if (serverSocketInfo == null) {
            try {
                loadSockets();
            } catch (IOException e) {
                throw e;
            }
        }
        return serverSocketInfo;
    }

}