package client.techniques;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.offbynull.portmapper.PortMapperFactory;
import com.offbynull.portmapper.gateway.Bus;
import com.offbynull.portmapper.gateway.Gateway;
import com.offbynull.portmapper.gateways.network.NetworkGateway;
import com.offbynull.portmapper.gateways.network.internalmessages.KillNetworkRequest;
import com.offbynull.portmapper.gateways.process.ProcessGateway;
import com.offbynull.portmapper.gateways.process.internalmessages.KillProcessRequest;
import com.offbynull.portmapper.mapper.MappedPort;
import com.offbynull.portmapper.mapper.PortMapper;
import com.offbynull.portmapper.mapper.PortType;

public class UPNP {

    public static final Logger logger = LoggerFactory.getLogger(UPNP.class);

    private MappedPort mappedPort = null;

    /**
     * Attempt to forward the requested port using portmapper
     * 
     * @param port
     *          The port to forward
     * @return
     *          true if port has been succesfully forwarded
     *          false if the port could not be forwarded
     */
    public boolean forwardport(int port){
        // Start gateways
        Gateway network = NetworkGateway.create();
        Gateway process = ProcessGateway.create();
        Bus networkBus = network.getBus();
        Bus processBus = process.getBus();

        logger.info("Portmapper might throw errors messages, please ignore these. It is robust enough to still succeed");
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
            mappedPort = mapper.mapPort(PortType.TCP, port, port, 120);
            logger.info("Port mapping added: " + mappedPort);
        }catch (IllegalStateException | InterruptedException e){
            logger.info("Could not create a port mapping: " + e.getMessage());
            return false;
        }
        
        if(port != getMappedPort()){
            logger.warn("Port was not mapped to " + port + " but to " + getMappedPort() + " instead");
        }

        // Refresh mapping half-way through the lifetime of the mapping (for example,
        // if the mapping is available for 40 seconds, refresh it every 20 seconds)
        //while(!shutdown) {
        //    mappedPort = mapper.refreshPort(mappedPort, mappedPort.getLifetime() / 2L);
        //    System.out.println("Port mapping refreshed: " + mappedPort);
        //    Thread.sleep(mappedPort.getLifetime() * 1000L);
        //}

        // Unmap port
        //mapper.unmapPort(mappedPort);

        // Stop gateways
        networkBus.send(new KillNetworkRequest());
        processBus.send(new KillProcessRequest()); // can kill this after discovery

        return true;
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
}
