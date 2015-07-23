package org.dsa.iot.sedona;

import org.dsa.iot.dslink.DSLink;
import org.dsa.iot.dslink.DSLinkFactory;
import org.dsa.iot.dslink.DSLinkHandler;
import org.dsa.iot.dslink.node.Node;
import org.dsa.iot.dslink.node.SubscriptionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Map;

/**
 * @author Samuel Grenier
 */
public class Main extends DSLinkHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
    private DSLink link;

    @Override
    public boolean isResponder() {
        return true;
    }

    @Override
    public void stop() {
        super.stop();
        if (link != null) {
            Node node = link.getNodeManager().getSuperRoot();
            Map<String, Node> children = node.getChildren();
            if (children != null) {
                for (Node child : children.values()) {
                    if (child.getAction() == null) {
                        Sedona sedona = child.getMetaData();
                        if (sedona != null) {
                            sedona.destroy();
                        }
                    }
                }
            }
        }
    }

    @Override
    public void preInit() {
        File file = new File("").getAbsoluteFile();
        if (file.getName().equals("bin")) {
            file = file.getParentFile();
        }
        String path = file.getPath();
        String home = System.getProperty("sedona.home", path);
        System.setProperty("sedona.home", home);
        LOGGER.info("Sedona home: {}", home);
    }

    @Override
    public void onResponderInitialized(DSLink link) {
        this.link = link;
        LOGGER.info("Connected");
        Node superRoot = link.getNodeManager().getSuperRoot();
        SubscriptionManager manager = link.getSubscriptionManager();
        Sedona.init(superRoot, manager);
    }

    public static void main(String[] args) {
        DSLinkFactory.start(args, new Main());
    }

}
