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

package com.discordsrv.fabric;

import com.discordsrv.common.config.configurate.manager.ConnectionConfigManager;
import com.discordsrv.common.config.configurate.manager.MainConfigManager;
import com.discordsrv.common.config.configurate.manager.MessagesConfigManager;
import com.discordsrv.common.config.messages.MessagesConfig;
import com.discordsrv.common.feature.messageforwarding.game.MinecraftToDiscordChatModule;
import com.discordsrv.fabric.config.connection.FabricConnectionConfig;
import com.discordsrv.fabric.config.main.FabricConfig;
import com.discordsrv.fabric.config.manager.FabricConfigManager;
import com.discordsrv.fabric.config.manager.FabricConnectionConfigManager;
import com.discordsrv.fabric.config.manager.FabricMessagesConfigManager;
import com.discordsrv.fabric.console.FabricConsole;
import com.discordsrv.fabric.game.handler.FabricCommandHandler;
import com.discordsrv.fabric.listener.FabricChatListener;
import com.discordsrv.fabric.player.FabricPlayerProvider;
import com.discordsrv.fabric.plugin.FabricModManager;
import com.discordsrv.common.AbstractDiscordSRV;
import com.discordsrv.common.abstraction.plugin.PluginManager;
import com.discordsrv.common.command.game.abstraction.handler.ICommandHandler;
import com.discordsrv.common.core.scheduler.StandardScheduler;
import com.discordsrv.common.feature.debug.data.OnlineMode;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;

import java.net.URL;
import java.util.jar.JarFile;

public class FabricDiscordSRV extends AbstractDiscordSRV<DiscordSRVFabricBootstrap, FabricConfig, FabricConnectionConfig, MessagesConfig> {

    private final StandardScheduler scheduler;
    private final FabricConsole console;
    private final FabricPlayerProvider playerProvider;
    private final FabricModManager modManager;
    private final FabricCommandHandler commandHandler;

    private final FabricConnectionConfigManager connectionConfigManager;
    private final FabricConfigManager configManager;
    private final FabricMessagesConfigManager messagesConfigManager;

    public FabricDiscordSRV(DiscordSRVFabricBootstrap bootstrap) {
        super(bootstrap);

        this.scheduler = new StandardScheduler(this);
        this.console = new FabricConsole(this);
        this.playerProvider = new FabricPlayerProvider(this);
        this.modManager = new FabricModManager(this);
        this.commandHandler = new FabricCommandHandler(this);

        // Config
        this.connectionConfigManager = new FabricConnectionConfigManager(this);
        this.configManager = new FabricConfigManager(this);
        this.messagesConfigManager = new FabricMessagesConfigManager(this);

        registerEvents();
        load();
    }

    private void registerEvents() {
        new FabricChatListener(this);
        registerModule(MinecraftToDiscordChatModule::new);
    }


    //TODO: Implement this method. Maybe with KnotClassloader?
    @Override
    protected URL getManifest() {
        ClassLoader classLoader = getClass().getClassLoader();

        return classLoader.getResource(JarFile.MANIFEST_NAME);
//        if (classLoader instanceof URLClassLoader) {
//            return ((URLClassLoader) classLoader).findResource(JarFile.MANIFEST_NAME);
//        } else {
//            throw new IllegalStateException("Class not loaded by a URLClassLoader, unable to get manifest");
//        }
    }

    public FabricModManager getModManager() {
        return modManager;
    }

    public MinecraftServer getServer() {
        return bootstrap.getServer();
    }

    @Override
    public ServerType serverType() {
        return ServerType.SERVER;
    }

    @Override
    public StandardScheduler scheduler() {
        return scheduler;
    }

    @Override
    public FabricConsole console() {
        return console;
    }

    @Override
    public @NotNull FabricPlayerProvider playerProvider() {
        return playerProvider;
    }

    @Override
    public PluginManager pluginManager() {
        return modManager;
    }

    @Override
    public OnlineMode onlineMode() {
        return OnlineMode.of(getServer().isOnlineMode());
    }

    @Override
    public ICommandHandler commandHandler() {
        return commandHandler;
    }

    @Override
    public ConnectionConfigManager<FabricConnectionConfig> connectionConfigManager() {
        return connectionConfigManager;
    }

    @Override
    public MainConfigManager<FabricConfig> configManager() {
        return configManager;
    }

    @Override
    public MessagesConfigManager<MessagesConfig> messagesConfigManager() {
        return messagesConfigManager;
    }

    @Override
    protected void enable() throws Throwable {
        super.enable();
    }
}
