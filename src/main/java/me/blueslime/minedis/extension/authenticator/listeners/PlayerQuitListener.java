package me.blueslime.minedis.extension.authenticator.listeners;

import me.blueslime.minedis.extension.authenticator.MStaffAuthenticator;
import me.blueslime.minedis.extension.authenticator.cache.CodeCache;
import me.blueslime.minedis.extension.authenticator.utils.EmbedSection;
import me.blueslime.minedis.utils.text.TextReplacer;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.event.EventHandler;

public class PlayerQuitListener implements Listener {
    private final MStaffAuthenticator extension;

    public PlayerQuitListener(MStaffAuthenticator extension) {
        this.extension = extension;
    }

    @EventHandler
    public void on(PlayerDisconnectEvent event) {
        if (event.getPlayer().hasPermission(extension.getConfiguration().getString("settings.auth.permission", "staffauth.need"))) {

            if (extension.getConfiguration().getBoolean("settings.logs.quit-log.enabled", true)) {
                Configuration settings = extension.getConfiguration();

                String channelID = settings.getString("settings.logs.quit-log.channel-id", "NOT_SET");
                String guildID = settings.getString("settings.logs.quit-log.guild-id", "NOT_SET");

                if (channelID.equalsIgnoreCase("NOT_SET") || guildID.equalsIgnoreCase("NOT_SET")) {
                    return;
                }

                Guild announce = extension.getJDA().getGuildById(guildID);

                if (announce == null) {
                    extension.getLogger().info("Guild-ID was not found for quit logs: " + guildID);
                    return;
                }

                TextChannel textChannel = announce.getTextChannelById(
                        channelID
                );

                if (textChannel == null) {
                    extension.getLogger().info("Channel for quit logs was not found: " + channelID);
                    return;
                }

                textChannel.sendMessageEmbeds(
                    new EmbedSection(
                        settings.getSection("settings.logs.quit-log")
                    ).build(
                        TextReplacer.builder()
                            .replace("%nick%", event.getPlayer().getName())
                            .replace("%name%", event.getPlayer().getName())
                            .replace("%uuid%", event.getPlayer().getUniqueId().toString())
                            .replace("%id%", event.getPlayer().getUniqueId().toString().replace("-", ""))
                    )
                ).queue();
            }
        }

        if (extension.getCache(CodeCache.class).contains(event.getPlayer().getUniqueId())) {
            extension.getCache(CodeCache.class).remove(event.getPlayer().getUniqueId());
        }
    }
}
