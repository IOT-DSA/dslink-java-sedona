package org.dsa.iot.sedona;

import org.dsa.iot.dslink.DSLink;
import org.dsa.iot.dslink.DSLinkFactory;
import org.dsa.iot.dslink.DSLinkHandler;
import org.dsa.iot.dslink.node.Node;
import org.dsa.iot.dslink.node.SubscriptionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * @author Samuel Grenier
 */
public class Main extends DSLinkHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    @Override
    public void onResponderInitialized(DSLink link) {
        LOGGER.info("Connected");
        Node superRoot = link.getNodeManager().getSuperRoot();
        SubscriptionManager manager = link.getSubscriptionManager();
        Sedona.init(superRoot, manager);
    }

    public static void main(String[] args) {
        File file = new File("").getAbsoluteFile();
        if (file.getName().equals("bin")) {
            file = file.getParentFile();
        }
        String path = file.getPath();
        String home = System.getProperty("sedona.home", path);
        System.setProperty("sedona.home", home);
        LOGGER.info("Sedona home: {}", home);
        DSLinkFactory.startResponder("sedona", args, new Main());
    }

}
