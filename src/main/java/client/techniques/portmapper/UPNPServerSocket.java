package client.techniques.portmapper;

import java.io.IOException;
import java.net.ServerSocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class UPNPServerSocket extends ServerSocket{

    public static final Logger logger = LoggerFactory.getLogger(UPNPServerSocket.class);
    
    private final PortMappingLifeTimeExtender lifeTimeExtender;
    public UPNPServerSocket(PortMappingLifeTimeExtender lifeTimeExtender) throws IOException {
        super();
        if(lifeTimeExtender == null){
            logger.warn("No lifetime extender set in UPNP server socket. Will not be able to interrupt the lifetime extender when close() is called on the server socket");
        }
        this.lifeTimeExtender = lifeTimeExtender;
    }
    
    @Override
    public void close() throws IOException{
        super.close();
        this.lifeTimeExtender.shutdown();
    }

}
