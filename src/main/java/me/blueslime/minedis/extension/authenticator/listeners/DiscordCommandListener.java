package me.blueslime.minedis.extension.authenticator.listeners;

import me.blueslime.minedis.extension.authenticator.MStaffAuthenticator;
import me.blueslime.minedis.extension.authenticator.cache.CodeCache;
import me.blueslime.minedis.extension.authenticator.cache.DiscordCache;
import me.blueslime.minedis.extension.authenticator.utils.CodeGenerator;
import me.blueslime.minedis.extension.authenticator.utils.EmbedSection;
import me.blueslime.minedis.utils.player.PlayerTools;
import me.blueslime.minedis.utils.text.TextReplacer;
import me.blueslime.minedis.utils.text.TextUtilities;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public class DiscordCommandListener extends ListenerAdapter {
    private final MStaffAuthenticator main;

    public DiscordCommandListener(MStaffAuthenticator main) {
        this.main = main;
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (event.getName().equals(main.getConfiguration().getString("settings.commands.link.command", "link"))) {
            event.deferReply(true).queue();
            OptionMapping option = event.getOption("Username");
            if (option != null) {
                String user = option.getAsString();
                ProxiedPlayer player = main.getProxy().getPlayer(user);
                if (player == null || !player.isConnected()) {
                    event.getHook().sendMessage(
                        main.getConfiguration().getString("settings.commands.link.player-is-not-online", "This player is not online.")
                    ).queueAfter(1, TimeUnit.SECONDS);
                    return;
                }
                if (event.getMember() == null) {
                    return;
                }

                if (main.getDiscordStorage().contains("storage.id." + event.getMember().getId())) {
                    event.getHook().sendMessage(
                        main.getConfiguration().getString(
                            "settings.commands.link.already",
                            "Your account is already linked to a minecraft account."
                        )
                    ).queueAfter(1, TimeUnit.SECONDS);
                    return;
                }

                player.sendMessage(
                    TextUtilities.component(
                        main.getConfiguration().getString(
                            "settings.commands.link.player-message",
                            "&aDiscord account %discord% is trying to link your username to his discord, please use the command on your discord account to confirm."
                        ).replace(
                        "%discord%",
                            event.getMember() != null ?
                                event.getMember().getUser().getName() :
                                "¿?"
                        )
                    )
                );
                String code = CodeGenerator.generate(15);
                main.getCache(CodeCache.class).set(
                    player.getUniqueId(),
                    code
                );
                main.getCache(DiscordCache.class).set(
                    code,
                    event.getMember().getId()
                );

                TextReplacer replacer = TextReplacer.builder()
                    .replace("%ip_current%", PlayerTools.getIP(player))
                    .replace("%ip_last%", main.getConfiguration().getString("settings.auth.address-not-found", "Not yet"))
                    .replace("%command%", "/" + main.getConfiguration().getString("settings.auth.command", "staffcode"))
                    .replace("%code%", code)
                    .replace("%nick%", player.getName())
                    .replace("%name%", player.getName());
                event.getHook().setEphemeral(true).sendMessage(
                    main.getConfiguration().getString("settings.commands.link.check-md", "Check your MD")
                ).queueAfter(1, TimeUnit.SECONDS);
                event.getMember().getUser().openPrivateChannel().queue(
                    channel -> {
                        if (channel != null && channel.canTalk()) {
                            channel.sendMessageEmbeds(
                                new EmbedSection(
                                        main.getConfiguration().getSection("settings.commands.link")
                                ).build(replacer)
                            ).queue();
                        }
                    }
                );
            }
        }
    }
}
