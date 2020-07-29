package chingdim.lokeon;

import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class EventListener implements Listener {
    private final Lokeon plugin;

    EventListener(Lokeon plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onServerConnected(ServerConnectedEvent event) {
        plugin.getLogger().info(event.getPlayer().getName() + " <-> " + event.getServer().getInfo().getName());
        plugin.getProxy().getServerInfo("lobby").ping((result, error) -> plugin.getLogger().info(result.toString() + error));
        plugin.getProxy().getChannels()
    }
}
