package chingdim.lokeon;

import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.ExecutionException;

public class Lokeon extends JavaPlugin{
    //private static Lokeon instance;
    private String ip;
    private Http http;

    //public Lokeon() { instance = this; }

    private void build_ip(String host) {
        ip = String.format("http://%s:80/", host);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        AWS aws = new AWS();
        getLogger().info("Checking Europe server instance IP");
        if (this.getConfig().getBoolean("debug")) build_ip("localhost");
        else {
            try {
                build_ip(aws.getInstanceIP());
            } catch (ExecutionException | InterruptedException e) {
                StringBuilder msg = new StringBuilder();
                for (StackTraceElement element : e.getStackTrace()) {
                    msg.append(element.toString()).append("\n");
                }
                getLogger().severe(msg.toString());
            }
        }
        getLogger().info("Address: " + ip);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        http = new Http(ip, getLogger());
        getLogger().info("Sending hook");
        http.hook();
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new EventListener(this), this);
    }

    @Override
    public void onDisable() {
        super.onDisable();
        getLogger().info("Sending shutdown message");
        http.shutdown();
    }

    Http getHttp() { return http;}

}
