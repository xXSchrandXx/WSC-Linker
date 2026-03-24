package de.xxschrandxx.wsc.wsclinker.velocity.listener;

import com.velocitypowered.api.event.Subscribe;

import de.xxschrandxx.wsc.wscbridge.velocity.api.event.WSCBridgeModuleEventVelocity;
import de.xxschrandxx.wsc.wsclinker.core.listener.AddModuleListenerCore;

public class AddModuleListenerVelocity extends AddModuleListenerCore {
    @Subscribe
    public void addModuleListener(WSCBridgeModuleEventVelocity event) {
        event.addModule(name);
    }
}
