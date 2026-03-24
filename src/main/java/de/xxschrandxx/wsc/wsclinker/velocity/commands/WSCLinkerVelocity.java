package de.xxschrandxx.wsc.wsclinker.velocity.commands;

import com.velocitypowered.api.command.RawCommand;

import de.xxschrandxx.wsc.wscbridge.velocity.api.command.SenderVelocity;
import de.xxschrandxx.wsc.wsclinker.core.commands.WSCLinker;
import de.xxschrandxx.wsc.wsclinker.velocity.MinecraftLinkerVelocity;

public class WSCLinkerVelocity implements RawCommand {
    @Override
    public void execute(final Invocation invocation) {
        MinecraftLinkerVelocity instance = MinecraftLinkerVelocity.getInstance();
        SenderVelocity sv = new SenderVelocity(invocation.source(), instance);
        String[] args = {};
        if (!invocation.arguments().isBlank()) {
            args = invocation.arguments().split(" ");
        }
        new WSCLinker(instance).execute(sv, args);
    }
}
