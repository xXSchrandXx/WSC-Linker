package de.xxschrandxx.wsc.wsclinker.velocity;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;
import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandMeta.Builder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;

import de.xxschrandxx.wsc.wscbridge.core.api.MinecraftBridgeLogger;
import de.xxschrandxx.wsc.wscbridge.core.api.command.ISender;
import de.xxschrandxx.wsc.wscbridge.core.IBridgePlugin;
import de.xxschrandxx.wsc.wsclinker.core.LinkerVars;
import de.xxschrandxx.wsc.wsclinker.core.runnable.UnlinkedMessageRunnable;
import de.xxschrandxx.wsc.wsclinker.core.runnable.UpdateNamesRunnable;
import de.xxschrandxx.wsc.wscbridge.velocity.MinecraftBridgeVelocity;
import de.xxschrandxx.wsc.wscbridge.velocity.api.ConfigurationVelocity;
import de.xxschrandxx.wsc.wscbridge.velocity.api.command.SenderVelocity;
import de.xxschrandxx.wsc.wsclinker.velocity.api.MinecraftLinkerVelocityAPI;
import de.xxschrandxx.wsc.wsclinker.velocity.api.event.WSCLinkerConfigReloadEventVelocity;
import de.xxschrandxx.wsc.wsclinker.velocity.commands.WSCLinkerVelocity;
import de.xxschrandxx.wsc.wsclinker.velocity.listener.AddModuleListenerVelocity;
import de.xxschrandxx.wsc.wsclinker.velocity.listener.WSCLinkerConfigReloadListenerVelocity;
import de.xxschrandxx.wsc.wsclinker.velocity.listener.WSCLinkerPluginReloadListenerVelocity;

@Plugin(id = "wsclinker-velocity", name = "wsclinker",
        version = "1.0.8", authors = {"xXSchrandXx"},
        dependencies = {
            @Dependency(id = "wscbridge-velocity")
        })
public class MinecraftLinkerVelocity implements IBridgePlugin<MinecraftLinkerVelocityAPI> {
    // start of api part
    public String getInfo() {
        return null;
    }

    private static MinecraftLinkerVelocity instance;

    public static MinecraftLinkerVelocity getInstance() {
        return instance;
    }

    private MinecraftLinkerVelocityAPI api;

    private MinecraftBridgeLogger bridgeLogger;

    @Override
    public MinecraftBridgeLogger getBridgeLogger() {
        return bridgeLogger;
    }

    public void loadAPI(ISender<?> sender) {
        String urlSendCodeString = getConfiguration().getString(LinkerVars.Configuration.urlSendCode);
        URL urlSendCode;
        try {
            urlSendCode = URI.create(urlSendCodeString).toURL();
        } catch (MalformedURLException e) {
            getBridgeLogger().warn("Could not load api, disabeling plugin!.", e);
            return;
        }
        String urlUpdateNamesString = getConfiguration().getString(LinkerVars.Configuration.urlUpdateNames);
        URL urlUpdateNames;
        try {
            urlUpdateNames = URI.create(urlUpdateNamesString).toURL();
        } catch (MalformedURLException e) {
            getBridgeLogger().warn("Could not load api, disabeling plugin!.", e);
            return;
        }
        String urlGetLinkedString = getConfiguration().getString(LinkerVars.Configuration.urlGetLinked);
        URL urlGetLinked;
        try {
            urlGetLinked = URI.create(urlGetLinkedString).toURL();
        } catch (MalformedURLException e) {
            getBridgeLogger().warn("Could not load api, disabeling plugin!.", e);
            return;
        }
        String urlGetUnlinkedString = getConfiguration().getString(LinkerVars.Configuration.urlGetUnlinked);
        URL urlGetUnlinked;
        try {
            urlGetUnlinked = URI.create(urlGetUnlinkedString).toURL();
        } catch (MalformedURLException e) {
            getBridgeLogger().warn("Could not load api, disabeling plugin!.", e);
            return;
        }
        MinecraftBridgeVelocity wsc = MinecraftBridgeVelocity.getInstance();
        this.api = new MinecraftLinkerVelocityAPI(
            urlSendCode,
            urlUpdateNames,
            urlGetLinked,
            urlGetUnlinked,
            getBridgeLogger(),
            wsc.getAPI()
        );
    }

    public MinecraftLinkerVelocityAPI getAPI() {
        return this.api;
    }
    // end of api part

    // start of plugin part
    private final ProxyServer server;
    public ProxyServer getProxy() {
        return this.server;
    }

    private final Logger logger;
    private final Path dataDirectory;

    @Inject
    public MinecraftLinkerVelocity(
        ProxyServer server,
        Logger logger,
        @DataDirectory Path dataDirectory
    ) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;

        this.configFile = new File(this.dataDirectory.toFile(), "config.json");
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        instance = this;
        bridgeLogger = new MinecraftBridgeLogger(this.logger);

        // Load configuration
        getBridgeLogger().info("Loading Configuration.");
        SenderVelocity sender = new SenderVelocity(this.getProxy().getConsoleCommandSource(), getInstance());
        if (!reloadConfiguration(sender)) {
            getBridgeLogger().info("Could not load config.json, disabeling plugin!");
            return;
        }

        // Load api
        getBridgeLogger().info("Loading API.");
        loadAPI(sender);

        // Load listener
        getBridgeLogger().info("Loading Listener.");
        this.getProxy().getEventManager().register(getInstance(), new AddModuleListenerVelocity());
        this.getProxy().getEventManager().register(getInstance(), new WSCLinkerConfigReloadListenerVelocity());
        this.getProxy().getEventManager().register(getInstance(), new WSCLinkerPluginReloadListenerVelocity());

        // load commands
        getBridgeLogger().info("Loading Commands.");
        Builder commandMeta = this.getProxy().getCommandManager().metaBuilder("wsclinker")
            .plugin(getInstance());
        if (this.getConfiguration().getBoolean(LinkerVars.Configuration.cmdAliasEnabled)) {
            for (String alias : getConfiguration().getStringList(LinkerVars.Configuration.cmdAliases)) {
                commandMeta.aliases(alias);
            }
        }
        this.getProxy().getCommandManager().register(
            commandMeta.build(),
            new WSCLinkerVelocity()
        );

        // load runnable
        getBridgeLogger().info("Loading Runnables.");
        if (getConfiguration().getBoolean(LinkerVars.Configuration.updateNamesEnabled)) {
            int updateNamesInterval = getConfiguration().getInt(LinkerVars.Configuration.updateNamesInterval);
            getProxy().getScheduler()
                .buildTask(getInstance(), new UpdateNamesRunnable(instance))
                .repeat(updateNamesInterval, TimeUnit.MINUTES)
                .schedule();
        }
        if (getConfiguration().getBoolean(LinkerVars.Configuration.unlinkedMessageEnabled)) {
            int unlinkedMessageInterval = getConfiguration().getInt(LinkerVars.Configuration.unlinkedMessageInterval);
            getProxy().getScheduler()
                .buildTask(getInstance(), new UnlinkedMessageRunnable(instance))
                .repeat(unlinkedMessageInterval, TimeUnit.MINUTES)
                .schedule();
        }
    }
    // end of plugin part

    // start config part
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final File configFile;
    private ConfigurationVelocity config;

    public ConfigurationVelocity getConfiguration() {
        return config;
    }

    public boolean reloadConfiguration(ISender sender) {
        if (!dataDirectory.toFile().exists()) {
            dataDirectory.toFile().mkdir();
        }
        if (configFile.exists()) {
            try {
                String json = Files.readString(configFile.toPath());
                this.config = new ConfigurationVelocity(gson.fromJson(json, LinkedTreeMap.class));
            }
            catch (IOException e) {
                getBridgeLogger().warn("Could not load config.json.", e);
                return false;
            }
        }
        else {
            try {
                configFile.createNewFile();
            }
            catch (IOException e) {
                getBridgeLogger().warn("Could not create config.json.", e);
                return false;
            }
            config = new ConfigurationVelocity();
        }

        if (LinkerVars.startConfig(getConfiguration(), getBridgeLogger())) {
            if (!saveConfiguration()) {
                return false;
            }
            return reloadConfiguration(sender);
        }
        this.getProxy().getEventManager().fireAndForget(new WSCLinkerConfigReloadEventVelocity(sender));
        return true;
    }

    public boolean saveConfiguration() {
        if (!this.dataDirectory.toFile().exists()) {
            this.dataDirectory.toFile().mkdir();
        }
        try {
            Path tmp = this.dataDirectory.resolve("config.json.tmp");
            String json = gson.toJson(this.config.getConfiguration());
            Files.writeString(tmp, json, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            try {
                Files.move(tmp, configFile.toPath(), StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException ex) {
                // Falls ATOMIC_MOVE nicht unterstützt wird
                Files.move(tmp, configFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        }
        catch (IOException e) {
            getBridgeLogger().warn("Could not save config.json.", e);
            return false;
        }
        return true;
    }
    // end config part
}
