package chingdim.lokeon;

import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.ExecutionException;

/**
 * Main class.
 * Allows the Minecraft server plugin loader(Spigot) to identify this .jar file as a Spigot Plugin.
 */
public class Lokeon extends JavaPlugin{
    private String host; // The URL for sending HTTP requests to DimBot Albon
    private Http http; // An instance of the Http class

    /**
     * Sets the URL based on the IP
     * @param ip IP of DimBot
     */
    private void setHost(String ip) {
        host = String.format("http://%s:80/", ip);
    }

    /**
     * Event handler when the plugin is being loaded by Spigot
     */
    @Override
    public void onLoad() {
        super.onLoad();
        AWS aws = new AWS();
        getLogger().info("Checking Europe server instance IP");
        // Normally in debug environments, DimBot is tested alongside with Lokeon
        // Therefore, Lokeon events should be sent to the same host.
        if (this.getConfig().getBoolean("debug")) setHost("localhost");
        else {
            try {  // Sets the host to be the remote DimBot instance
                setHost(aws.getInstanceIP(this.getConfig().getString("instance")));
            } catch (ExecutionException | InterruptedException e) {
                // Cannot set the host
                StringBuilder msg = new StringBuilder();
                for (StackTraceElement element : e.getStackTrace()) {
                    msg.append(element.toString()).append("\n");
                }
                getLogger().severe(msg.toString());
            }
        }
        getLogger().info("Address: " + host);
    }

    /**
     * Event handler when the plugin is being enabled by Spigot
     */
    @Override
    public void onEnable() {
        super.onEnable();
        http = new Http(host, getLogger());
        getLogger().info("Sending hook");
        http.hook();  // Notifies DimBot that Lokeon is up and running
        PluginManager pm = getServer().getPluginManager();
        // Registers in-game event listener for Lokeon
        EventListener eventListener = new EventListener(this);
        pm.registerEvents(eventListener, this);
        // Starts the idle countdown
        eventListener.startTimer();
    }

    Http getHttp() { return http;}

}
