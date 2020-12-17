package city.kube.bot;

import city.kube.bot.discord.BotInstance;
import city.kube.bot.features.*;
import city.kube.bot.minecraft.DiscordCommandHandler;
import city.kube.bot.minecraft.KubeCityBotCommandHandler;
import city.kube.bot.minecraft.EventListener;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class KubeCityBotPlugin extends JavaPlugin {

    private static KubeCityBotPlugin INSTANCE;
    public static KubeCityBotPlugin getInstance() {
        return INSTANCE;
    }

    private BotInstance bot = new BotInstance();
    private String serverId;
    private boolean iconStorageEnabled;

    private final File dataFile = new File(getDataFolder(), "data.yml");
    private YamlConfiguration dataConfiguration;

    private final File messagesFile = new File(getDataFolder(), "messages.yml");
    private YamlConfiguration messagesConfiguration;

    private final List<Feature> features = new ArrayList<>();

    @Override
    public void onEnable() {
        INSTANCE = this;
        getDataFolder().mkdirs();
        saveDefaultConfig();
        if(!messagesFile.exists()) saveResource(messagesFile.getName(), false);

        ConfigurationSerialization.registerClass(KubeCityPlayer.class);

        reload();

        getCommand("kubecitybot").setExecutor(new KubeCityBotCommandHandler());
        getCommand("discord").setExecutor(new DiscordCommandHandler());

    }

    @Override
    public void onDisable() {
        saveConfig();
        INSTANCE = null;
    }

    public void reload() {
        INSTANCE = this;
        HandlerList.unregisterAll(this);
        Bukkit.getPluginManager().registerEvents(new EventListener(), this);

        reloadConfig();

        FileConfiguration config = getConfig();
        String botToken = config.getString("bot-token");
        serverId = config.getString("server-id");
        iconStorageEnabled = config.getConfigurationSection("icon-storage").getBoolean("use");

        if(botToken != null) {
            bot.launch(botToken);
        } else {
            getLogger().warning("Discord bot token is not set. Disabling Discord bot.");
        }

        KubeCityPlayer.PLAYER_MAP.clear();
        dataConfiguration = YamlConfiguration.loadConfiguration(dataFile);
        if(dataConfiguration.isList("players")) dataConfiguration.getList("players");

        messagesConfiguration = YamlConfiguration.loadConfiguration(messagesFile);

        features.clear();
        if(config.getConfigurationSection("broadcaster").getBoolean("use")) features.add(new Broadcaster());
        if(config.getConfigurationSection("simple-forwarder").getBoolean("use")) features.add(new SimpleForwarder());
        if(config.getConfigurationSection("translator-forwarder").getBoolean("use")) features.add(new TranslatorForwarder());
        if(config.getConfigurationSection("group-linker").getBoolean("use")) features.add(new GroupLinker());

    }

    @Override
    public void saveConfig() {
        super.saveConfig();

        for(Feature feature : features) {
            feature.save();
        }

        dataConfiguration.set("players", new ArrayList<>(KubeCityPlayer.PLAYER_MAP.values()));
        try {
            dataConfiguration.save(dataFile);
        } catch(IOException ex) {
            ex.printStackTrace();
        }
    }

    public void reloadFeatures() {
        for(Feature feature : features) {
            feature.reload(this);
        }
    }

    public <T extends Feature> Optional<T> getFeature(Class<T> type) {
        return (Optional<T>) features.stream().filter(feature -> feature.getClass().equals(type)).findAny();
    }

    public String getMessage(String key, String def) {
        return messagesConfiguration.getString(key, def);
    }

    public BotInstance getBot() {
        return bot;
    }

    public String getServerId() {
        return serverId;
    }

    public boolean isIconStorageEnabled() {
        return iconStorageEnabled;
    }

}
