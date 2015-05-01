package org.dsa.iot.sedona;

import org.dsa.iot.dslink.DSLink;
import org.dsa.iot.dslink.DSLinkFactory;
import org.dsa.iot.dslink.DSLinkHandler;
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
        Sedona.init(link.getNodeManager().getSuperRoot());
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
