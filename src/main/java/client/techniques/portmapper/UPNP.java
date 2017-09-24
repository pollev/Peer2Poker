package client.techniques.portmapper;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.offbynull.portmapper.PortMapperFactory;
import com.offbynull.portmapper.gateway.Bus;
import com.offbynull.portmapper.gateway.Gateway;
import com.offbynull.portmapper.gateways.network.NetworkGateway;
import com.offbynull.portmapper.gateways.process.ProcessGateway;
import com.offbynull.portmapper.gateways.process.internalmessages.KillProcessRequest;
import com.offbynull.portmapper.mapper.MappedPort;
import com.offbynull.portmapper.mapper.PortMapper;
import com.offbynull.portmapper.mapper.PortType;

import client.Peer2PokerClient;
import client.Peer2PokerClient.ServerType;

public class UPNP {

    public static final Logger logger = LoggerFactory.getLogger(UPNP.class);

    private MappedPort mappedPort = null;
    private PortMappingLifeTimeExtender lifeTimeExtender = null;
    private final int portMappingLifetime = 1800; // half hour

    /**
     * Attempt to forward the requested port using portmapper
     * 
     * @param port
     *          The port to forward
     * @return
     *          true if port has been succesfully forwarded
     *          false if the port could not be forwarded
     */
    public boolean forwardport(Peer2PokerClient client, int port){
        // Start gateways
        Gateway network = NetworkGateway.create();
        Gateway process = ProcessGateway.create();
        Bus networkBus = network.getBus();
        Bus processBus = process.getBus();

        logger.info("Portmapper might throw errors messages, please ignore these. It is incredibly robust. I recommend turning off its logging in your logging config as it can be really verbose");
        // Discover port forwarding devices and take the first one found
        List<PortMapper> mappers;
        try {
            mappers = PortMapperFactory.discover(networkBus, processBus);
        } catch (InterruptedException e1) {
            logger.info("Could not search for port mapper: " + e1.getMessage());
            return false;
        }

        if(mappers.size() == 0){
            logger.info("No port mappers found");
            return false;
        }

        PortMapper mapper = mappers.get(0);

        // IMPORTANT NOTE: Many devices prevent you from mapping ports that are <= 1024
        // (both internal and external ports). Be mindful of this when choosing which
        // ports you want to map.
        if(port <= 1024){
            logger.warn("Many devices prevent you from mapping ports that are <= 1024");
        }
        
        try{
            mappedPort = mapper.mapPort(PortType.TCP, port, port, portMappingLifetime);
            logger.info("Port mapping added: " + mappedPort);
        }catch (IllegalStateException | InterruptedException e){
            logger.info("Could not create a port mapping: " + e.getMessage());
            return false;
        }
        
        if(port != getMappedPort()){
            logger.warn("Port was not mapped to " + port + " but to " + getMappedPort() + " instead");
            if(client.getServerType() == ServerType.NOSERVER){
                logger.warn("ATTENTION! You are in a special case, a direct connection is possible. But not on the specified port.");
                logger.warn("ATTENTION! Because you are using the NOSERVER setting, this cannot be automatically communicated to the client");
                logger.warn("ATTENTION! You must manually communicate the new port to the client");
                logger.warn("ATTENTION! You can programatically request the current used port by calling server_socket.getLocalPort()");
            }
        }
        
        lifeTimeExtender = new PortMappingLifeTimeExtender(networkBus, mapper, mappedPort);
        (new Thread(lifeTimeExtender)).start();

        // Stop gateway
        processBus.send(new KillProcessRequest()); // can kill this after discovery

        logger.warn("Using automatic port mapping to setup connection.");
        logger.warn("This will create a portmapping for " + portMappingLifetime + " seconds and refresh it as long as you do not call close() on the server socket.");
        logger.warn("You should understand this means that if you kill the application and restart it within this period without calling close() (or if the NAT device does not support removal of portmappings). The direct connection technique will succeed because the portmap is still there.");
        logger.warn("However, this means that the application is not refreshing the portmap, and when the time is up, you will get a connection timeout");
        logger.warn("if this happens. Simply ask for a new connection, it will now detect that a direct connection cannot work, and request a new portmapping that it will refresh properly");
        return true;
    }
    
    /**
     * Start the UPNP direct connection server socket on the given port
     * 
     * @param port
     *          The port for the UPNP direct connection socket
     * @return
     *          The server socket for the direct connection
     */
    public ServerSocket getUPNPServerSocket(int port){
        ServerSocket serverSocket = null;
        try {
            serverSocket = new UPNPServerSocket(lifeTimeExtender);
            serverSocket.setReuseAddress(true);
            serverSocket.bind(new InetSocketAddress("0.0.0.0", port));
        } catch (IOException e) {
            logger.error("Failed to start server socket for UPNP direct connection");
            e.printStackTrace();
        }
        return serverSocket;
    }

    /**
     * Get the mapped port (must call forwardport() first)
     * 
     * @return
     *      The portnumber of the mapped port
     */
    public int getMappedPort(){
        return this.mappedPort.getExternalPort();
    }
    
    /**
     * Get the mapped port (must call forwardport() first)
     * 
     * @return
     *      The portnumber of the mapped port
     */
    public PortMappingLifeTimeExtender getLifeTimeExtender(){
        return this.lifeTimeExtender;
    }
}
