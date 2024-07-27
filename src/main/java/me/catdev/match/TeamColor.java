package me.catdev.match;

import org.bukkit.DyeColor;

public enum TeamColor {

    RED("red", "&4", DyeColor.RED.getData()),
    BLUE("blue", "&1", DyeColor.BLUE.getData()),
    GREEN("green", "&2", DyeColor.LIME.getData()),
    YELLOW("yellow", "&e", DyeColor.YELLOW.getData()),
    AQUA("aqua", "&b", DyeColor.CYAN.getData()),
    WHITE("white", "&f", DyeColor.WHITE.getData()),
    PINK("pink", "&d", DyeColor.PINK.getData()),
    GRAY("gray", "&7", DyeColor.GRAY.getData());

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
