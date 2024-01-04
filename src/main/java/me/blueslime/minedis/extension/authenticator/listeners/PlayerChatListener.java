package me.blueslime.minedis.extension.authenticator.listeners;

import me.blueslime.minedis.extension.authenticator.MStaffAuthenticator;
import me.blueslime.minedis.extension.authenticator.utils.EmbedSection;
import me.blueslime.minedis.utils.player.PlayerTools;
import me.blueslime.minedis.utils.text.TextReplacer;
import me.blueslime.minedis.utils.text.TextUtilities;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.event.EventHandler;

public class PlayerChatListener implements Listener {
    private final MStaffAuthenticator main;

    public PlayerChatListener(MStaffAuthenticator main) {
        this.main = main;
    }

    @EventHandler
    public void on(ChatEvent event) {

        if (!(event.getSender() instanceof ProxiedPlayer)) {
            return;
        }

        String command = main.getConfiguration().getString("settings.auth.command", "staffcode");

        ProxiedPlayer player = (ProxiedPlayer) event.getSender();

        if (main.getCache("mstaff-mc-codes").contains(player.getUniqueId()) || main.getCodeCache().contains(player.getUniqueId())) {

            if (event.getMessage().contains("login ") || event.getMessage().contains("register ")) {
                if (!event.getMessage().contains(command + " ")) {
                    return;
                }
            }

            event.setCancelled(true);

            if (event.isCommand()) {
                if (event.getMessage().contains(command + " ")) {

                    String code = (String)main.getCache("mstaff-mc-codes").get(player.getUniqueId());

                    if (code == null) {
                        code = main.getCodeCache().get(player.getUniqueId());
                    }

                    if (event.getMessage().contains(code)) {

                        main.getCache("mstaff-mc-codes").remove(player.getUniqueId());
                        main.getCodeCache().remove(player.getUniqueId());

                        if (main.getCache("mstaff-discord").contains(code)) {
                            String user = (String)main.getCache("mstaff-discord").get(code);

                            main.getMinecraftStorage().set(
                                    "storage.id." + player.getName(),
                                    user
                            );
                            main.getDiscordStorage().set(
                                    "storage.id." + user, player.getName()
                            );

                            main.getCache("mstaff-discord").remove(code);

                            player.sendMessage(
                                    TextUtilities.component(
                                            main.getConfiguration().getString(
                                                    "settings.commands.link.account-linked",
                                                    "&aNow your minecraft account has been linked to your discord account ;)"
                                            )
                                    )
                            );
                        }

                        player.sendMessage(
                                TextUtilities.component(
                                        main.getConfiguration().getString(
                                                "settings.auth.welcome",
                                                "&aWelcome!&f Your IP Address has been registered."
                                        )
                                )
                        );

                        String IP = PlayerTools.getIP(player);

                        main.getMinecraftStorage().set("storage.ip." + player.getName(), IP);

                        main.saveDatabase();

                        main.reloadDatabase();
                        return;
                    }
                }
                player.sendMessage(
                        TextUtilities.component(
                                main.getConfiguration().getString(
                                        "settings.auth.login-msg", "&6Staff > &eYou are not logged yet."
                                )
                        )
                );
                if (main.getConfiguration().getBoolean("settings.logs.fail-login-attempt-log.enabled", true)) {
                    Configuration settings = main.getConfiguration();

                    String channelID = settings.getString("settings.logs.fail-login-attempt-log.channel-id", "NOT_SET");
                    String guildID = settings.getString("settings.logs.fail-login-attempt-log.guild-id", "NOT_SET");

                    if (channelID.equalsIgnoreCase("NOT_SET") || guildID.equalsIgnoreCase("NOT_SET")) {
                        return;
                    }

                    Guild announce = main.getJDA().getGuildById(guildID);

                    if (announce == null) {
                        main.getLogger().info("Guild-ID was not found for join logs: " + guildID);
                        return;
                    }

                    TextChannel textChannel = announce.getTextChannelById(
                            channelID
                    );

                    if (textChannel == null) {
                        main.getLogger().info("Channel for join logs was not found: " + channelID);
                        return;
                    }

                    if (player.isConnected()) {
                        textChannel.sendMessageEmbeds(
                            new EmbedSection(
                                    settings.getSection("settings.logs.fail-login-attempt-log")
                            ).build(
                                TextReplacer.builder()
                                    .replace("%nick%", player.getName())
                                    .replace("%command%", "/" + event.getMessage())
                                    .replace("%name%", player.getName())
                                    .replace("%uuid%", player.getUniqueId().toString())
                                    .replace("%id%", player.getUniqueId().toString().replace("-", ""))
                            )
                        ).queue();
                    }
                }
            }
            return;
        }

        if (event.getMessage().contains(command + " ")) {
            event.setCancelled(true);
        }
    }
}
