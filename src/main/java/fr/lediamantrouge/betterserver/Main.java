package fr.lediamantrouge.betterserver;

import de.dytanic.cloudnet.driver.CloudNetDriver;
import de.dytanic.cloudnet.driver.service.ServiceInfoSnapshot;
import de.dytanic.cloudnet.driver.service.ServiceTask;
import de.dytanic.cloudnet.ext.bridge.BridgeServiceProperty;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Random;

public final class Main extends Plugin implements Listener {

    private static Main instance;

    @Override
    public void onEnable() {
        instance = this;
        getProxy().getPluginManager().registerListener(this, this);
        for(ServiceTask taskInfo : CloudNetDriver.getInstance().getServiceTaskProvider().getPermanentServiceTasks()) {
            String name = taskInfo.getName();
            if (!taskInfo.getName().equalsIgnoreCase("Proxy")) {
                ProxyServer.getInstance().getServers().put(name, ProxyServer.getInstance().constructServerInfo(name, new InetSocketAddress("0.0.0.0", 25565), name, false));
            }
        }
    }

    @Override
    public void onDisable() {
        for(ServiceTask taskInfo : CloudNetDriver.getInstance().getServiceTaskProvider().getPermanentServiceTasks()) {
            String name = taskInfo.getName();
            if (!taskInfo.getName().equalsIgnoreCase("Proxy")) {
                ProxyServer.getInstance().getServers().remove(name, ProxyServer.getInstance().constructServerInfo(name, new InetSocketAddress("0.0.0.0", 25565), name, false));
            }
        }
    }

    public static Main getInstance() {
        return instance;
    }

    @EventHandler
    public void onConnect(ServerConnectEvent e) {
        for(ServiceTask task : CloudNetDriver.getInstance().getServiceTaskProvider().getPermanentServiceTasks()) {
            if(e.getTarget().getName().equals(task.getName())) {
                Boolean found = false;
                ArrayList<String> servers = new ArrayList<>();
                e.setCancelled(true);
                for(ServiceInfoSnapshot s : CloudNetDriver.getInstance().getCloudServiceProvider().getCloudServices(task.getName())) {
                    if(!s.getProperty(BridgeServiceProperty.IS_STARTING).orElse(true) && !s.getProperty(BridgeServiceProperty.IS_FULL).orElse(true) && !s.getProperty(BridgeServiceProperty.IS_IN_GAME).orElse(true) && !s.getName().equalsIgnoreCase(e.getPlayer().getServer().getInfo().getName())) {
                        servers.add(s.getName());
                        found = true;
                    }
                }
                if(!found) {
                    e.getPlayer().sendMessage(new TextComponent(ChatColor.RED + "Aucun serveur trouvé !"));
                } else {
                    int randomserver = new Random().nextInt(servers.toArray().length);
                    ServerInfo serverInfo = Main.getInstance().getProxy().getServerInfo(servers.get(randomserver));
                    e.getPlayer().connect(serverInfo);
                }
            }
        }
    }
}
