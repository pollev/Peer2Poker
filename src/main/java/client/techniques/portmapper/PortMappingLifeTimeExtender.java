package client.techniques.portmapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.offbynull.portmapper.gateway.Bus;
import com.offbynull.portmapper.gateways.network.internalmessages.KillNetworkRequest;
import com.offbynull.portmapper.mapper.MappedPort;
import com.offbynull.portmapper.mapper.PortMapper;

public class PortMappingLifeTimeExtender implements Runnable{

    public static final Logger logger = LoggerFactory.getLogger(PortMappingLifeTimeExtender.class);

    private final Bus networkBus;
    private final PortMapper mapper;
    private MappedPort mappedPort;

    private volatile boolean shutdown = false;

    public PortMappingLifeTimeExtender(Bus networkBus, PortMapper mapper, MappedPort mappedPort) {
        this.networkBus = networkBus;
        this.mapper = mapper;
        this.mappedPort = mappedPort;
    }

    @Override
    public void run() {
        // Refresh mapping half-way through the lifetime of the mapping (for example,
        // if the mapping is available for 40 seconds, refresh it every 20 seconds)
        try {
            while(!shutdown) {
                Thread.sleep(mappedPort.getLifetime() * 1000L / 2L);
                mappedPort = mapper.refreshPort(mappedPort, mappedPort.getLifetime());
                logger.info("Port mapping refreshed: " + mappedPort);
            }
            // Unmap port
            mapper.unmapPort(mappedPort);
        } catch (InterruptedException e) {
            logger.error("Port mapping lifetime extender got interrupted: " + e.getMessage());
        }

        // Stop gateways
        networkBus.send(new KillNetworkRequest());
    }
    
    /**
     * Stop the PortMappingLifeTimeExtender (and unmap the port) 
     */
    public void shutdown(){
        this.shutdown = true;
    }

}
