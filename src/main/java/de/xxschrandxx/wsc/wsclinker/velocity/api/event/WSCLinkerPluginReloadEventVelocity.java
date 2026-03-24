package de.xxschrandxx.wsc.wsclinker.velocity.api.event;

import de.xxschrandxx.wsc.wscbridge.core.api.command.ISender;
import de.xxschrandxx.wsc.wscbridge.velocity.api.event.AbstractWSCPluginReloadEventVelocity;

public final class WSCLinkerPluginReloadEventVelocity extends AbstractWSCPluginReloadEventVelocity {
    public WSCLinkerPluginReloadEventVelocity(ISender<?> sender) {
        super(sender);
    }    
}
