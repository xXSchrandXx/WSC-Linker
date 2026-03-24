package de.xxschrandxx.wsc.wsclinker.velocity.api.event;

import de.xxschrandxx.wsc.wscbridge.core.api.command.ISender;
import de.xxschrandxx.wsc.wscbridge.velocity.api.event.AbstractWSCConfigReloadEventVelocity;

public final class WSCLinkerConfigReloadEventVelocity extends AbstractWSCConfigReloadEventVelocity {
    public WSCLinkerConfigReloadEventVelocity(ISender<?> sender) {
        super(sender);
    }   
}
