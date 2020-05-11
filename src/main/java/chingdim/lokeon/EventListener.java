package chingdim.lokeon;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class EventListener implements Listener {
    private final Lokeon plugin;
    private CompletableFuture<Void> future = new CompletableFuture<>();

    EventListener(Lokeon plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        plugin.getHttp().join(event.getPlayer().getName());
        future.cancel(true);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.getHttp().quit(event.getPlayer().getName());
        if (plugin.getServer().getOnlinePlayers().size() == 1) {
            future = CompletableFuture.runAsync(() -> {
                try {
                    plugin.getLogger().info("Starting server shutdown clock");
                    int time = plugin.getConfig().getBoolean("debug") ? 10000 : 900000;
                    Thread.sleep(time);
                    if (!future.isCancelled()) {
                        plugin.getLogger().info("Server shutdown clock completed");
                        postprocessShutdown("");
                    }
                } catch (InterruptedException e) {
                    plugin.getLogger().severe("Future Interrupted Exception");
                }
            });
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCmdPreprocess(PlayerCommandPreprocessEvent event) {
        if (event.getMessage().equalsIgnoreCase("/stop")) {
            event.setCancelled(true);
            plugin.getLogger().info("A player executed /stop");
            postprocessShutdown(event.getPlayer().getName());
        }
    }

    private void postprocessShutdown(String name) {
        plugin.getHttp().shutdown(name);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                if (!plugin.getConfig().getBoolean("debug")) Runtime.getRuntime().exec("shutdown -h");
                plugin.getLogger().info("Added JVM shutdown hook");
            } catch (IOException e) {
                plugin.getLogger().severe("IOException when executing shutdown command");
            }
        }));
        plugin.getServer().shutdown();
    }

}
