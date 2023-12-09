package me.blueslime.minedis.extension.authenticator.cache;

import me.blueslime.minedis.modules.cache.Cache;

import java.util.HashMap;

public class DiscordCache extends Cache<String, String> {
    public DiscordCache() {
        super(new HashMap<>());
    }
}
