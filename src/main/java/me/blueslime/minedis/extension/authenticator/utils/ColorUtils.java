package me.blueslime.minedis.extension.authenticator.utils;

import java.awt.*;
import java.util.Locale;

public class ColorUtils {
    public static Color getColor(String color) {
        switch (color.toLowerCase(Locale.ENGLISH)) {
            default:
            case "&e":
            case "e":
            case "yellow":
                return Color.YELLOW;
            case "white":
            case "&r":
            case "&f":
            case "r":
            case "f":
                return Color.WHITE;
            case "light-gray":
            case "light_gray":
            case "light gray":
            case "7":
            case "&7":
                return Color.LIGHT_GRAY;
            case "gray":
                return Color.GRAY;
            case "dark_gray":
            case "dark gray":
            case "dark-gray":
            case "&8":
            case "8":
                return Color.DARK_GRAY;
            case "black":
            case "0":
            case "&0":
                return Color.BLACK;
            case "red":
            case "&4":
            case "4":
                return Color.RED;
            case "&d":
            case "d":
            case "pink":
                return Color.PINK;
            case "&6":
            case "6":
            case "orange":
                return Color.ORANGE;
            case "green":
            case "dark green":
            case "dark-green":
            case "dark_green":
            case "&2":
            case "2":
            case "a":
            case "&a":
            case "lime":
                return Color.GREEN;
            case "magenta":
            case "&5":
            case "5":
                return Color.MAGENTA;
            case "cyan":
            case "&b":
            case "b":
                return Color.CYAN;
            case "blue":
            case "&1":
            case "&9":
            case "&3":
            case "1":
            case "9":
            case "3":
                return Color.BLUE;
        }
    }
}
