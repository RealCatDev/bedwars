package me.catdev.match;

public enum TeamColor {

    RED("red", "&4", (short)14),
    BLUE("blue", "&1", (short)11),
    GREEN("green", "&2", (short)13),
    YELLOW("yellow", "&e", (short)4),
    AQUA("aqua", "&b", (short)9),
    WHITE("white", "&f", (short)0),
    PINK("pink", "&d", (short)6),
    GRAY("gray", "&7", (short)7);

    private final String name;
    private final String style;
    private final short data;

    TeamColor(String name, String style, short data) {
        this.name = name;
        this.style = style;
        this.data = data;
    }

    public String getName() {
        return name;
    }

    public String getStyle() {
        return style;
    }

    public short getData() {
        return data;
    }
}
