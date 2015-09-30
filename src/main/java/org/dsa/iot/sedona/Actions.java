package org.dsa.iot.sedona;

import org.dsa.iot.dslink.node.Node;
import org.dsa.iot.dslink.node.NodeBuilder;
import org.dsa.iot.dslink.node.Permission;
import org.dsa.iot.dslink.node.SubscriptionManager;
import org.dsa.iot.dslink.node.actions.Action;
import org.dsa.iot.dslink.node.actions.ActionResult;
import org.dsa.iot.dslink.node.actions.Parameter;
import org.dsa.iot.dslink.node.actions.table.Row;
import org.dsa.iot.dslink.node.actions.table.Table;
import org.dsa.iot.dslink.node.value.Value;
import org.dsa.iot.dslink.node.value.ValueType;
import org.dsa.iot.dslink.util.json.JsonArray;
import org.dsa.iot.dslink.util.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.dsa.iot.dslink.util.handler.Handler;
import sedona.Slot;
import sedona.Type;
import sedona.sox.KitVersion;
import sedona.sox.SoxComponent;
import sedona.sox.VersionInfo;

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
        });
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

    public static Action getVersion(final Sedona sed) {
        Action a = new Action(Permission.READ, new Handler<ActionResult>() {
            @Override
            public void handle(ActionResult event) {
                try {
                    VersionInfo info = sed.getClient().readVersion();

                    Table table = event.getTable();
                    table.addRow(Row.make(new Value(info.platformId)));
                    table.addRow(Row.make(new Value(info.scodeFlags)));
                    {
                        JsonArray kits = new JsonArray();
                        for (KitVersion kit : info.kits) {
                            JsonObject obj = new JsonObject();
                            obj.put("name", kit.name);
                            obj.put("checksum", kit.checksum);
                            obj.put("version", kit.version.toString());
                            kits.add(obj);
                        }
                        table.addRow(Row.make(new Value(kits)));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        a.addResult(new Parameter("platformID", ValueType.STRING));
        a.addResult(new Parameter("scodeFlags", ValueType.NUMBER));
        a.addResult(new Parameter("kits", ValueType.ARRAY));
        return a;
    }
}
