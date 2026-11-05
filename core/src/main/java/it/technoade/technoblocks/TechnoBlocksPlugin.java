package it.technoade.technoblocks;

import it.technoade.technoblocks.adapter.AdapterLoader;
import it.technoade.technoblocks.api.VersionAdapter;
import it.technoade.technoblocks.block.ActiveBlocksStore;
import it.technoade.technoblocks.block.TemporaryBlockManager;
import it.technoade.technoblocks.command.TechnoBlocksCommand;
import it.technoade.technoblocks.config.ConfigUpdater;
import it.technoade.technoblocks.config.Messages;
import it.technoade.technoblocks.config.PluginSettings;
import it.technoade.technoblocks.config.SettingsLoader;
import it.technoade.technoblocks.data.DataStore;
import it.technoade.technoblocks.hook.PlaceholderHook;
import it.technoade.technoblocks.listener.InventoryListener;
import it.technoade.technoblocks.listener.PlacementListener;
import it.technoade.technoblocks.listener.PlayerListener;
import it.technoade.technoblocks.listener.ProtectionListener;
import it.technoade.technoblocks.player.PlayerItemsService;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;
import java.util.logging.Level;

public final class TechnoBlocksPlugin extends JavaPlugin {

    private static final long DATA_SAVE_PERIOD = 6000L;

    private VersionAdapter adapter;
    private DataStore data;
    private TemporaryBlockManager manager;
    private PlayerItemsService items;
    private PluginSettings settings;
    private Messages messages;

    @Override
    public void onEnable() {
        adapter = AdapterLoader.load(this);
        if (adapter == null) {
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        getLogger().info("Using adapter " + adapter.getClass().getName());

        loadFiles();
        data = new DataStore(new File(getDataFolder(), "data.yml"));
        data.load();
        items = new PlayerItemsService(adapter, data);
        manager = new TemporaryBlockManager(this, adapter, new ActiveBlocksStore(new File(getDataFolder(), "active-blocks.yml")));
        manager.start(settings);

        Supplier<PluginSettings> settingsSupplier = new Supplier<PluginSettings>() {
            @Override
            public PluginSettings get() {
                return settings;
            }
        };
        Supplier<Messages> messagesSupplier = new Supplier<Messages>() {
            @Override
            public Messages get() {
                return messages;
            }
        };
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new PlacementListener(this, adapter, settingsSupplier, manager, data, items), this);
        pm.registerEvents(new ProtectionListener(manager), this);
        pm.registerEvents(new PlayerListener(this, adapter, settingsSupplier, manager, items), this);
        InventoryListener inventoryListener = new InventoryListener(adapter, settingsSupplier, messagesSupplier, data, items);
        pm.registerEvents(inventoryListener, this);
        inventoryListener.registerSwapHandGuard(this);

        PluginCommand command = getCommand("technoblocks");
        TechnoBlocksCommand executor = new TechnoBlocksCommand(this);
        command.setExecutor(executor);
        command.setTabCompleter(executor);

        if (pm.getPlugin("PlaceholderAPI") != null) PlaceholderHook.registerFor(this, data);

        for (Player player : getServer().getOnlinePlayers()) items.apply(player, settings);

        getServer().getScheduler().runTask(this, new Runnable() {
            @Override
            public void run() {
                manager.recoverFromCrash();
            }
        });
        getServer().getScheduler().runTaskTimer(this, new Runnable() {
            @Override
            public void run() {
                data.saveAsync(TechnoBlocksPlugin.this);
            }
        }, DATA_SAVE_PERIOD, DATA_SAVE_PERIOD);
    }

    @Override
    public void onDisable() {
        if (manager != null) {
            manager.restoreAll();
            manager.stop();
        }
        // Tagged items left in inventories would place permanent blocks while the plugin is off.
        if (items != null) {
            for (Player player : getServer().getOnlinePlayers()) items.strip(player);
        }
        if (data != null) data.saveSync();
    }

    public void reload() {
        manager.restoreAll();
        loadFiles();
        manager.applySettings(settings);
        for (Player player : getServer().getOnlinePlayers()) items.apply(player, settings);
    }

    public int clearBlocks() {
        return manager.restoreAll();
    }

    public Messages getMessages() {
        return messages;
    }

    /** Reads config.yml and messages.yml directly (not getConfig(): its jar defaults would bring back deleted blocks). */
    private void loadFiles() {
        saveDefaultConfig();
        File configFile = new File(getDataFolder(), "config.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        if (ConfigUpdater.isOldFormat(config)) {
            File old = new File(getDataFolder(), "config-old.yml");
            if (old.exists()) old = new File(getDataFolder(), "config-old-" + System.currentTimeMillis() + ".yml");
            if (configFile.renameTo(old)) {
                getLogger().warning("Found a TechnoBlocks 2.x config: moved it to " + old.getName()
                        + " and created a new config.yml. Copy your blocks over by hand.");
                saveResource("config.yml", true);
                config = YamlConfiguration.loadConfiguration(configFile);
            } else {
                getLogger().warning("Found a TechnoBlocks 2.x config but could not rename it; please replace config.yml");
            }
        }
        mergeDefaults(config, configFile, "config.yml");
        settings = SettingsLoader.load(config, adapter, getLogger());

        File messagesFile = new File(getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) saveResource("messages.yml", false);
        YamlConfiguration messagesYml = YamlConfiguration.loadConfiguration(messagesFile);
        mergeDefaults(messagesYml, messagesFile, "messages.yml");
        messages = Messages.load(messagesYml);
    }

    private void mergeDefaults(YamlConfiguration current, File file, String resource) {
        InputStream in = getResource(resource);
        if (in == null) return;
        YamlConfiguration defaults = YamlConfiguration.loadConfiguration(new InputStreamReader(in, StandardCharsets.UTF_8));
        if (!ConfigUpdater.merge(current, defaults)) return;
        if (!supportsComments()) {
            getLogger().warning("Added new settings to " + resource + "; this server version can't keep YAML comments, "
                    + "so comments in that file were lost");
        }
        try {
            current.save(file);
        } catch (IOException e) {
            getLogger().log(Level.WARNING, "Could not save " + resource, e);
        }
    }

    /** YAML comments survive saving from 1.18.1. */
    private static boolean supportsComments() {
        try {
            ConfigurationSection.class.getMethod("getComments", String.class);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }
}
