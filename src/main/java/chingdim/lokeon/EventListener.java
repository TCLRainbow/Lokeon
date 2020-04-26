package chingdim.lokeon;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class EventListener implements Listener {
    private Http http;

    EventListener(Http http) {
        this.http = http;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        http.join(event.getPlayer().getName());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        http.quit(event.getPlayer().getName());
    }

}
