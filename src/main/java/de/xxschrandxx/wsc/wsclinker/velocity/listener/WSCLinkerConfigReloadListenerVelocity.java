package de.xxschrandxx.wsc.wsclinker.velocity.listener;

import com.velocitypowered.api.event.Subscribe;

import de.xxschrandxx.wsc.wscbridge.velocity.api.event.WSCBridgeConfigReloadEventVelocity;
import de.xxschrandxx.wsc.wsclinker.core.LinkerVars;
import de.xxschrandxx.wsc.wsclinker.velocity.MinecraftLinkerVelocity;
import de.xxschrandxx.wsc.wsclinker.velocity.api.event.WSCLinkerConfigReloadEventVelocity;

public class WSCLinkerConfigReloadListenerVelocity {
    @Subscribe
    public void onConfigReload(WSCBridgeConfigReloadEventVelocity event) {
        MinecraftLinkerVelocity instance = MinecraftLinkerVelocity.getInstance();
        String configStart = instance.getConfiguration().getString(LinkerVars.Configuration.LangCmdReloadConfigStart);
        event.getSender().sendMessage(configStart);
        if (!instance.reloadConfiguration(event.getSender())) {
            String configError = instance.getConfiguration().getString(LinkerVars.Configuration.LangCmdReloadConfigError);
            event.getSender().sendMessage(configError);
            instance.getBridgeLogger().warn("Could not load config.yml!");
            return;
        }
        String configSuccess = instance.getConfiguration().getString(LinkerVars.Configuration.LangCmdReloadConfigSuccess);
        event.getSender().sendMessage(configSuccess);
        instance.getProxy().getEventManager().fireAndForget(new WSCLinkerConfigReloadEventVelocity(event.getSender()));
    }
}
