package org.dsa.iot.sedona;

import org.dsa.iot.dslink.node.value.Value;
import sedona.*;
import sedona.Byte;
import sedona.Double;
import sedona.Float;
import sedona.Long;
import sedona.Short;

/**
 * @author Samuel Grenier
 */
public class Utils {

    public static sedona.Value fromSdkValue(Value value, int typeId) {
        sedona.Value val;
        if (typeId == Type.byteId) {
            val = sedona.Byte.make(value.getNumber().byteValue());
        } else if (typeId == Type.shortId) {
            val = sedona.Short.make(value.getNumber().shortValue());
        } else if (typeId == Type.intId) {
            val = sedona.Int.make(value.getNumber().intValue());
        } else if (typeId == Type.longId) {
            val = sedona.Long.make(value.getNumber().longValue());
        } else if (typeId == Type.floatId) {
            val = sedona.Float.make(value.getNumber().floatValue());
        } else if (typeId == Type.doubleId) {
            val = sedona.Double.make(value.getNumber().doubleValue());
        } else if (typeId == Type.strId) {
            val = sedona.Str.make(value.getString());
        } else if (typeId == Type.bufId) {
            val = sedona.Buf.fromString(value.getString());
        } else if (typeId == Type.boolId) {
            val = sedona.Bool.make(value.getBool());
        } else {
            throw new RuntimeException("Unknown type ID: " + typeId);
        }
        return val;
    }

    public static Value fromSedonaValue(sedona.Value val, Slot slot) {
        if (val != null) {
            return Utils.fromSedonaValue(val);
        } else {
            return Utils.fromSedonaSlot(slot);
        }
    }

    public static Value fromSedonaValue(sedona.Value val) {
        final int typeId = val.typeId();
        final Value value;
        if (typeId == Type.byteId) {
            value = new Value(((Byte) val).val);
        } else if (typeId == Type.shortId) {
            value = new Value(((Short) val).val);
        } else if (typeId == Type.intId) {
            value = new Value(((Int) val).val);
        } else if (typeId == Type.longId) {
            value = new Value(((Long) val).val);
        } else if (typeId == Type.floatId) {
            value = new Value(((Float) val).val);
        } else if (typeId == Type.doubleId) {
            value = new Value(((Double) val).val);
        } else if (typeId == Type.strId) {
            value = new Value(((Str) val).val);
        } else if (typeId == Type.bufId) {
            value = new Value(val.toString());
        } else if (typeId == Type.boolId) {
            value = new Value(((Bool) val).val);
        } else {
            throw new RuntimeException("Unknown type ID: " + typeId);
        }
        return value;
    }

    public static Value fromSedonaSlot(Slot slot) {
        final int typeId = slot.type.id;
        final Value value;
        if (typeId == Type.byteId
                || typeId == Type.shortId
                || typeId == Type.intId
                || typeId == Type.longId
                || typeId == Type.floatId
                || typeId == Type.doubleId) {
            value = new Value((Number) null);
        } else if (typeId == Type.strId
                || typeId == Type.bufId) {
            value = new Value((String) null);
        } else if (typeId == Type.boolId) {
            value = new Value((Boolean) null);
        } else {
            throw new RuntimeException("Unknown type ID: " + typeId);
        }
        return value;
    }

}
