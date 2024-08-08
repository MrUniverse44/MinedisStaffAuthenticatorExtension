package me.blueslime.minedis.extension.authenticator.utils;

import java.util.ArrayList;
import java.util.List;

public class StringUtil {
    public static List<String> toStringList(List<?> list) {
        List<String> array = new ArrayList<>();

        list.forEach(
            value -> array.add(value.toString())
        );

        return array;
    }
}
