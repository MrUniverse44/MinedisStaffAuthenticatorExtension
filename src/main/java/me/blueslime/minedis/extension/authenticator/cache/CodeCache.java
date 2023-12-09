package me.blueslime.minedis.extension.authenticator.cache;

import me.blueslime.minedis.modules.cache.Cache;

import java.util.HashMap;
import java.util.UUID;

public class CodeCache extends Cache<UUID, String> {
    public CodeCache() {
        super(new HashMap<>());
    }
}
