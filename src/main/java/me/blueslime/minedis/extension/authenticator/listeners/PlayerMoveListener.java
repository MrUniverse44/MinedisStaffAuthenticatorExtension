package me.blueslime.minedis.extension.authenticator.listeners;

import me.blueslime.minedis.extension.authenticator.MStaffAuthenticator;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

public class PlayerMoveListener implements Listener {
    private final MStaffAuthenticator main;

    public PlayerMoveListener(MStaffAuthenticator main) {
        this.main = main;
    }

    @EventHandler
    public void on(ServerSwitchEvent event) {
        ProxiedPlayer player = event.getPlayer();

        if (main.getCache("mstaff-mc-codes").contains(player.getUniqueId()) || main.getCodeCache().contains(player.getUniqueId())) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            try (DataOutputStream out = new DataOutputStream(stream)) {
                out.writeUTF(event.getPlayer().getUniqueId().toString() + ":pending");
                player.getServer().sendData(MStaffAuthenticator.MESSAGE_CHANNEL, stream.toByteArray());
            } catch (Exception ignored) {}
        } else {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            try (DataOutputStream out = new DataOutputStream(stream)) {
                out.writeUTF(event.getPlayer().getUniqueId().toString() + ":completed");
                player.getServer().sendData(MStaffAuthenticator.MESSAGE_CHANNEL, stream.toByteArray());
            } catch (Exception ignored) {}
        }
    }
}
