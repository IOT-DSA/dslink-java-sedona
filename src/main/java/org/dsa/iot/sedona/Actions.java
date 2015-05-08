package org.dsa.iot.sedona;

import org.dsa.iot.dslink.node.Node;
import org.dsa.iot.dslink.node.NodeBuilder;
import org.dsa.iot.dslink.node.Permission;
import org.dsa.iot.dslink.node.SubscriptionManager;
import org.dsa.iot.dslink.node.actions.Action;
import org.dsa.iot.dslink.node.actions.ActionResult;
import org.dsa.iot.dslink.node.actions.Parameter;
import org.dsa.iot.dslink.node.value.Value;
import org.dsa.iot.dslink.node.value.ValueType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertx.java.core.Handler;
import sedona.Slot;
import sedona.Type;
import sedona.sox.SoxComponent;

/**
 * @author Samuel Grenier
 */
public class Actions {

    private static final Logger LOGGER = LoggerFactory.getLogger(Actions.class);

    public static Action getAddServerAction(final Node node,
                                            final SubscriptionManager man) {
        final ValueType vt = ValueType.STRING;
        Action a = new Action(Permission.READ, new Handler<ActionResult>() {
            @Override
            public void handle(ActionResult event) {
                Value vName = event.getParameter("name", vt);
                Value vUrl = event.getParameter("url", vt);
                Value vPort = event.getParameter("port", ValueType.NUMBER);
                Value vUser = event.getParameter("username", vt);
                Value vPass = event.getParameter("password");

                String name = vName.getString();
                if (node.getChild(name) == null) {
                    NodeBuilder builder = node.createChild(name);
                    builder.setRoConfig("url", vUrl);
                    builder.setRoConfig("port", vPort);
                    builder.setRoConfig("username", vUser);
                    if (vPass != null) {
                        char[] pass = vPass.getString().toCharArray();
                        builder.setPassword(pass);
                    }

                    Node child = builder.build();
                    try {
                        Sedona sedona = new Sedona(child, man);
                        sedona.connect(true);
                    } catch (Exception e) {
                        LOGGER.info("Failed to add server", e);
                        node.removeChild(child);
                    }
                }
            }
        }, Action.InvokeMode.ASYNC);
        a.addParameter(new Parameter("name", vt));
        a.addParameter(new Parameter("url", vt));
        a.addParameter(new Parameter("port", ValueType.NUMBER));
        a.addParameter(new Parameter("username", vt));
        a.addParameter(new Parameter("password", vt));
        return a;
    }

    public static Action getInvokableSedonaNode(final Sedona sed,
                                                final Slot slot,
                                                final SoxComponent comp) {
        final int typeId = slot.type.id;
        final ValueType type;
        if (typeId == Type.byteId
                || typeId == Type.shortId
                || typeId == Type.intId
                || typeId == Type.longId
                || typeId == Type.floatId
                || typeId == Type.doubleId) {
            type = ValueType.NUMBER;
        } else if (typeId == Type.strId) {
            type = ValueType.STRING;
        } else if (typeId == Type.boolId) {
            type = ValueType.BOOL;
        } else {
            type = null;
        }
        Action a = new Action(Permission.READ, new Handler<ActionResult>() {
            @Override
            public void handle(ActionResult event) {
                sedona.Value val = null;
                if (type != null) {
                    Value value = event.getParameter("value");
                    if (value != null) {
                        val = Utils.fromSdkValue(value, typeId);
                    }
                }

                sed.invoke(comp, slot, val);
            }
        });
        if (type != null) {
            a.addParameter(new Parameter("value", type));
        }
        return a;
    }
}
