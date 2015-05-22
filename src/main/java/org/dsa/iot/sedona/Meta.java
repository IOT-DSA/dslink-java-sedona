package org.dsa.iot.sedona;

import org.dsa.iot.dslink.node.Node;
import org.dsa.iot.dslink.node.NodeBuilder;
import org.dsa.iot.dslink.node.value.Value;
import org.dsa.iot.dslink.node.value.ValueType;

/**
 * @author Samuel Grenier
 */
public class Meta {

    private final int meta;

    private int x;
    private int y;

    private boolean groupOne;
    private boolean groupTwo;
    private boolean groupThree;
    private boolean groupFour;

    public Meta(int meta) {
        this.meta = meta;
        unpack();
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public boolean isGroupOne() {
        return groupOne;
    }

    public boolean isGroupTwo() {
        return groupTwo;
    }

    public boolean isGroupThree() {
        return groupThree;
    }

    public boolean isGroupFour() {
        return groupFour;
    }

    private void unpack() {
        x = meta >> 24;
        y = meta << 8;
        y >>= 24;

        int securityGroup = meta << 24;
        securityGroup >>= 24;
        groupOne = (securityGroup & 0x01) != 0;
        groupTwo = (securityGroup & 0x02) != 0;
        groupThree = (securityGroup & 0x04) != 0;
        groupFour = (securityGroup & 0x08) != 0;
    }

    public static int pack(int x, int y,
                             boolean groupOne, boolean groupTwo,
                             boolean groupThree, boolean groupFour) {
        int packed = 0;
        packed |= groupOne ? 0x01 : 0x00;
        packed |= groupTwo ? 0x02 : 0x00;
        packed |= groupThree ? 0x04 : 0x00;
        packed |= groupFour ? 0x08 : 0x00;

        packed |= x << 0x18;
        packed |= y << 0x10;

        return packed;
    }

    public static void buildMetaCoord(Node node, String name, int coord) {
        Value v = new Value(coord);
        NodeBuilder b = node.createChild(name);
        b.setValueType(ValueType.NUMBER);
        b.build().setValue(v);
    }

    public static void buildSecGroup(Node node, String name, boolean checked) {
        Value v = new Value(checked);
        NodeBuilder b = node.createChild(name);
        b.setValueType(ValueType.BOOL);
        b.build().setValue(v);
    }
}
