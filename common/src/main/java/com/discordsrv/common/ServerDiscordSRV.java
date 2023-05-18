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

package com.discordsrv.common;

import com.discordsrv.common.bootstrap.IBootstrap;
import com.discordsrv.common.config.connection.ConnectionConfig;
import com.discordsrv.common.config.main.MainConfig;
import com.discordsrv.common.messageforwarding.game.AwardMessageModule;
import com.discordsrv.common.messageforwarding.game.DeathMessageModule;
import com.discordsrv.common.player.ServerPlayerProvider;
import com.discordsrv.common.scheduler.ServerScheduler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.OverridingMethodsMustInvokeSuper;
import java.util.concurrent.CompletableFuture;

public abstract class ServerDiscordSRV<B extends IBootstrap, C extends MainConfig, CC extends ConnectionConfig> extends AbstractDiscordSRV<B, C, CC> {

    public ServerDiscordSRV(B bootstrap) {
        super(bootstrap);
    }

    @Override
    public abstract ServerScheduler scheduler();

    @Override
    public abstract @NotNull ServerPlayerProvider<?, ?> playerProvider();

    @Override
    protected void enable() throws Throwable {
        super.enable();

        registerModule(AwardMessageModule::new);
        registerModule(DeathMessageModule::new);
    }

    public final CompletableFuture<Void> invokeServerStarted() {
        return invokeLifecycle(() -> {
            if (status().isShutdown()) {
                return null;
            }
            this.serverStarted();
            return null;
        });
    }

    @OverridingMethodsMustInvokeSuper
    protected void serverStarted() {
        startedMessage();
    }
}