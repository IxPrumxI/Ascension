/*
 * This file is part of DiscordSRV, licensed under the GPLv3 License
 * Copyright (c) 2016-2023 Austin "Scarsz" Shapiro, Henri "Vankka" Schubin and DiscordSRV contributors
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

package com.discordsrv.common.player.provider;

import com.discordsrv.common.DiscordSRV;
import com.discordsrv.common.player.IOfflinePlayer;
import com.discordsrv.common.player.IPlayer;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public abstract class ServerPlayerProvider<T extends IPlayer, DT extends DiscordSRV> extends AbstractPlayerProvider<T, DT> {

    public ServerPlayerProvider(DT discordSRV) {
        super(discordSRV);
    }

    @Override
    public CompletableFuture<UUID> lookupUUIDForUsername(String username) {
        return lookupOfflinePlayer(username).thenApply(IOfflinePlayer::uniqueId);
    }

    @Override
    public abstract CompletableFuture<IOfflinePlayer> lookupOfflinePlayer(String username);

    @Override
    public abstract CompletableFuture<IOfflinePlayer> lookupOfflinePlayer(UUID uuid);
}
