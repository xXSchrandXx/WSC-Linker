package de.xxschrandxx.wsc.wsclinker.velocity.listener;

import com.velocitypowered.api.event.Subscribe;

import de.xxschrandxx.wsc.wscbridge.velocity.api.event.WSCBridgePluginReloadEventVelocity;
import de.xxschrandxx.wsc.wsclinker.core.LinkerVars;
import de.xxschrandxx.wsc.wsclinker.velocity.MinecraftLinkerVelocity;
import de.xxschrandxx.wsc.wsclinker.velocity.api.event.WSCLinkerPluginReloadEventVelocity;

public class WSCLinkerPluginReloadListenerVelocity {
    @Subscribe
    public void onPluginReload(WSCBridgePluginReloadEventVelocity event) {
        MinecraftLinkerVelocity instance = MinecraftLinkerVelocity.getInstance();
        String apiStart = instance.getConfiguration().getString(LinkerVars.Configuration.LangCmdReloadAPIStart);
        event.getSender().sendMessage(apiStart);
        instance.loadAPI(event.getSender());
        String apiSuccess = instance.getConfiguration().getString(LinkerVars.Configuration.LangCmdReloadAPISuccess);
        event.getSender().sendMessage(apiSuccess);
        instance.getProxy().getEventManager().fireAndForget(new WSCLinkerPluginReloadEventVelocity(event.getSender()));
    }
}
