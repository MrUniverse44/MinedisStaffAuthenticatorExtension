package me.blueslime.minedis.extension.authenticator.utils;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class CodeGenerator {
    public static String generate(int length) {
        Random random = ThreadLocalRandom.current();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < length; i++) {
            code.append(
                    random.nextInt(10)
            );
        }
        return code.toString();
    }
}
