package org.dsa.iot.sedona;

import org.dsa.iot.dslink.node.Node;
import org.dsa.iot.dslink.node.NodeBuilder;
import org.dsa.iot.dslink.node.value.Value;
import org.dsa.iot.dslink.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sedona.Slot;
import sedona.dasp.DaspSocket;
import sedona.sox.SoxClient;
import sedona.sox.SoxComponent;

import java.net.InetAddress;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author Samuel Grenier
 */
public class Sedona {

    private static final Logger LOGGER = LoggerFactory.getLogger(Sedona.class);
    private final Node parent;

    private ScheduledFuture<?> future;
    private SoxClient client;

    public Sedona(Node parent) {
        this.parent = parent;
    }

    public synchronized void connect(boolean checked) {
        if (future != null) {
            future.cancel(false);
            future = null;
        }
        if (client == null) {
            String url = parent.getRoConfig("url").getString();
            int port = parent.getRoConfig("port").getNumber().intValue();
            String user = parent.getRoConfig("username").getString();
            char[] pass = parent.getPassword();

            try {
                int queue = DaspSocket.SESSION_QUEUING;
                DaspSocket socket = DaspSocket.open(-1, null, queue);
                InetAddress ina = InetAddress.getByName(url);
                String password = "";
                if (pass != null) {
                    password = new String(pass);
                }
                client = new SoxClient(socket, ina, port, user, password);
                client.connect();
                LOGGER.info("Opened connection to '{}'", parent.getName());
                try {
                    buildTree(parent, client.loadApp(), true);
                } catch (Exception e) {
                    LOGGER.error("Failed to build tree", e);
                }
            } catch (Exception e) {
                if (checked) {
                    throw new RuntimeException(e);
                }
                scheduleReconnect();
            }
        }
    }

    private void scheduleReconnect() {
        LOGGER.warn("Reconnection to Sedona server scheduled");
        client = null;
        future = Objects.getDaemonThreadPool().schedule(new Runnable() {
            @Override
            public void run() {
                connect(false);
            }
        }, 5, TimeUnit.SECONDS);
    }

    private void buildTree(Node node, SoxComponent component, boolean clear) {
        if (clear) {
            parent.clearChildren();
        }

        for (Slot slot : component.type.slots) {
            NodeBuilder builder = node.createChild(slot.name);
            sedona.Value val = component.get(slot);
            Value value = new Value((String) null, true);
            if (val != null) {
                value.set(val.toString());
            }
            builder.setValue(value);
            Node child = builder.build();
            child.setSerializable(false);
        }

        for (SoxComponent comp : component.children()) {
            NodeBuilder builder = node.createChild(comp.name());
            Node child = builder.build();
            child.setSerializable(false);
            buildTree(child, comp, false);
        }
    }

    public static void init(Node superRoot) {
        {
            NodeBuilder child = superRoot.createChild("addServer");
            child.setAction(Actions.getAddServerAction(superRoot));
            child.build();
        }

        {
            Map<String, Node> children = superRoot.getChildren();
            if (children != null) {
                for (Node child : children.values()) {
                    if (child.getAction() == null) {
                        Sedona sedona = new Sedona(child);
                        sedona.connect(false);
                    }
                }
            }
        }
    }
}
