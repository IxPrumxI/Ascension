/*
 * This file is part of DiscordSRV, licensed under the GPLv3 License
 * Copyright (c) 2016-2025 Austin "Scarsz" Shapiro, Henri "Vankka" Schubin and DiscordSRV contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.discordsrv.fabric.command.game.sender;

import com.discordsrv.common.command.game.abstraction.sender.ICommandSender;
import com.discordsrv.common.permission.game.Permission;
import com.discordsrv.fabric.FabricDiscordSRV;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.minecraft.network.message.ChatVisibility;
import net.minecraft.server.command.ServerCommandSource;
import org.jetbrains.annotations.NotNull;

public class FabricCommandSender implements ICommandSender {

    protected final FabricDiscordSRV discordSRV;
    protected final ServerCommandSource commandSource;

    public FabricCommandSender(FabricDiscordSRV discordSRV, ServerCommandSource commandSource) {
        this.discordSRV = discordSRV;
        this.commandSource = commandSource;
    }

    @Override
    public boolean hasPermission(Permission permission) {
        int defaultLevel = permission.requiresOpByDefault() ? 4 : 0;
        return Permissions.check(commandSource, permission.permission(), defaultLevel);
    }

    @Override
    public void runCommand(String command) {
        discordSRV.getServer().getCommandManager().executeWithPrefix(commandSource, command);
    }

    @Override
    public @NotNull Audience audience() {
        return new Audience() {
            @SuppressWarnings({"UnstableApiUsage", "deprecation"})
            @Override
            public void sendMessage(final @NotNull Identity source, final @NotNull Component message, final @NotNull MessageType type) {
                ChatVisibility visibility = ChatVisibility.FULL;
                if (commandSource.getPlayer() != null) {
                    visibility = commandSource.getPlayer().getClientChatVisibility();
                }
                final boolean shouldSend = switch (visibility) {
                    case FULL -> true;
                    case SYSTEM -> type == MessageType.SYSTEM;
                    case HIDDEN -> false;
                };

                if (shouldSend) {
                    commandSource.sendMessage(discordSRV.componentFactory().toNative(message));
                }
            }
        };
    }
}
