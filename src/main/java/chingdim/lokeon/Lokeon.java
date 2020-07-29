package chingdim.lokeon;

import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public final class Lokeon extends Plugin {
    private String ip;
    Http http;
    private Configuration config;

    private void build_ip(String host) {
        ip = "http://" + host + ":80/";
    }

    @Override
    public void onLoad() {
        super.onLoad();
        try {
            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(getDataFolder(), "config.yml"));
            if (config.getBoolean("debug")) build_ip("localhost");
            else build_ip(new AWS().getInstanceIP());
        } catch (IOException | InterruptedException | ExecutionException e) {
            StringBuilder msg = new StringBuilder();
            for (StackTraceElement element : e.getStackTrace()) {
                msg.append(element.toString()).append("\n");
            }
            getLogger().severe(msg.toString());
        }
        getLogger().info("Address: " + ip);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        http = new Http(ip, getLogger());
        getLogger().info("Sending hook");
        http.hook();
        PluginManager pm = getProxy().getPluginManager();
        EventListener eventListener = new EventListener(this);
        pm.registerListener(this, eventListener);
        // eventListener.startTimer();
    }

}