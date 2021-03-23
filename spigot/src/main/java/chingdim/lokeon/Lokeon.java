package chingdim.lokeon;

import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Main class.
 * Allows the Minecraft server plugin loader(Spigot) to identify this .jar file as a Spigot Plugin.
 */
public class Lokeon extends JavaPlugin {
    private Http http; // An instance of the Http class

    /**
     * Event handler when the plugin is being loaded by Spigot
     */
    @Override
    public void onLoad() {
        super.onLoad();
        // Normally in debug environments, DimBot is tested alongside with Lokeon
        // Therefore, Lokeon events should be sent to the same host.
        String ip = "http://localhost/";
        if (!this.getConfig().getBoolean("debug")) {
            Path path = Paths.get(System.getProperty("user.dir") + "/ip");
            try {
                ip = String.format("http://%s/", Files.readAllLines(path).get(0));
            } catch (IOException e) {
                e.printStackTrace(System.err);
            }
        }
        getLogger().info("Address: " + ip);
        http = new Http(ip, getLogger());
    }

    /**
     * Event handler when the plugin is being enabled by Spigot
     */
    @Override
    public void onEnable() {
        super.onEnable();
        new BukkitRunnable() {

            @Override
            public void run() {
                getLogger().info("Sending hook");
                http.hook();  // Notifies DimBot that Lokeon is up and running
            }
        }.runTaskLater(this, 1);

        PluginManager pm = getServer().getPluginManager();
        // Registers in-game event listener for Lokeon
        EventListener eventListener = new EventListener(this);
        pm.registerEvents(eventListener, this);
        // Starts the idle countdown
        eventListener.startTimer();
    }

    Http getHttp() { return http;}

}
