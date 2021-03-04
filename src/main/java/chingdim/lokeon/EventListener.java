package chingdim.lokeon;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

/**
 * In-game event handlers
 */
public class EventListener implements Listener {
    private final Lokeon plugin;
    private CompletableFuture<Void> future = new CompletableFuture<>(); // Stores the async task

    EventListener(Lokeon plugin) {
        this.plugin = plugin;
    }

    /**
     * Event handler when a player joins
     * @param event Event context by Spigot
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        plugin.getLogger().info("Someone joined, cancelling shutdown clock");
        // Notifies DimBot that a player has joined
        plugin.getHttp().join(event.getPlayer().getName());
        // As someone is playing, stops the idle timer so the server won't suddenly shut down.
        future.cancel(true);
    }

    /**
     * Starts the idle timer.
     */
    void startTimer() {
        // Starts the timer concurrently
        future = CompletableFuture.runAsync(() -> {
            try {
                // The idle period is defined in configuration file, with a minimum defaults of 30s
                int time = Math.max(plugin.getConfig().getInt("idle"), 30);
                plugin.getLogger().info(String.format("Starting server shutdown clock: %s seconds", time));
                Thread.sleep(time * 1000);
                // Time is up
                if (!future.isCancelled()) {
                    plugin.getLogger().info("Server shutdown clock completed");
                    postprocessShutdown("");  // Lokeon shuts it
                }
            } catch (InterruptedException e) {
                plugin.getLogger().severe("Future Interrupted Exception");
            }
        });
    }

    /**
     * Event handler when a player left the server
     * @param event Event context by Spigot
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.getHttp().quit(event.getPlayer().getName());
        // The plugin counts itself as an online player, so when there is 1 online player left,
        // that means no one is in the server, so initiate timer
        if (plugin.getServer().getOnlinePlayers().size() == 1) startTimer();
    }

    /**
     * Event handler when preprocessing a command
     * This always runs first in the event handler to capture /stop
     * @param event Event context by Spigot
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onCmdPreprocess(PlayerCommandPreprocessEvent event) {
        if (event.getMessage().equalsIgnoreCase("/stop")) {
            event.setCancelled(true);
            plugin.getLogger().info("A player executed /stop");
            // Directly shuts down server
            postprocessShutdown(event.getPlayer().getName());
        }
    }

    /**
     * The actual shutdown logic
     * @param name The user who shuts down
     */
    private void postprocessShutdown(String name) {
        plugin.getHttp().shutdown(name);
        // Runs this when the process exits
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                // The real instance is running Linux
                if (!plugin.getConfig().getBoolean("debug")) Runtime.getRuntime().exec("shutdown 0");
            } catch (IOException e) {
                plugin.getLogger().severe("IOException when executing shutdown command");
            }
        }));
        plugin.getServer().shutdown();
    }

}
