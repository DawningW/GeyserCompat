package io.github.qingchenw.geysercompat;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.geysermc.geyser.adapters.spigot.SpigotAdapters;
import org.geysermc.geyser.platform.spigot.GeyserSpigotPlugin;
import org.geysermc.geyser.platform.spigot.world.manager.GeyserSpigot1_12NativeWorldManager;
import org.geysermc.geyser.platform.spigot.world.manager.GeyserSpigot1_12WorldManager;
import org.geysermc.geyser.platform.spigot.world.manager.GeyserSpigotFallbackWorldManager;
import org.geysermc.geyser.platform.spigot.world.manager.GeyserSpigotWorldManager;

import java.lang.reflect.Field;

public final class GeyserCompat extends JavaPlugin {
    public GeyserSpigotPlugin geyserPlugin;

    @Override
    public void onEnable() {
        if (!Bukkit.getPluginManager().isPluginEnabled("Geyser-Spigot")) {
            getLogger().severe("Geyser-Spigot is not enabled! GeyserCompat will not work without it.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        geyserPlugin = JavaPlugin.getPlugin(GeyserSpigotPlugin.class);

        Bukkit.getPluginManager().registerEvents(new Listener() {
            @EventHandler(priority = EventPriority.MONITOR)
            public void onServerLoaded(ServerLoadEvent event) {
                getLogger().info("Using GeyserSpigotWorldManager before modify: " + geyserPlugin.getWorldManager().getClass());
                try {
                    // https://github.com/GeyserMC/Geyser/commit/07c7b2f7f8166fd1adde6551c2e1ab7cbc0c2501
                    GeyserSpigotWorldManager worldManager;
                    if (Bukkit.getPluginManager().isPluginEnabled("ViaVersion")) {
                        if (SpigotAdapters.getWorldAdapter() != null) {
                            worldManager = new GeyserSpigot1_12NativeWorldManager(geyserPlugin);
                        } else {
                            worldManager = new GeyserSpigot1_12WorldManager(geyserPlugin);
                        }
                    } else {
                        worldManager = new GeyserSpigotFallbackWorldManager(geyserPlugin);
                    }

                    Class<?> clazz = GeyserSpigotPlugin.class;
                    Field field = clazz.getDeclaredField("geyserWorldManager");
                    field.setAccessible(true);
                    field.set(geyserPlugin, worldManager);
                } catch (Exception e) {
                    getLogger().severe("Failed to modify GeyserSpigotWorldManager: " + e.getMessage());
                    e.printStackTrace();
                }
                getLogger().info("Using GeyserSpigotWorldManager after modify: " + geyserPlugin.getWorldManager().getClass());
            }
        }, this);
    }

    @Override
    public void onDisable() {

    }
}
