/*
 * This file is part of DiscordSRV, licensed under the GPLv3 License
 * Copyright (c) 2016-2026 Austin "Scarsz" Shapiro, Henri "Vankka" Schubin and DiscordSRV contributors
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

package com.discordsrv.forge;

import com.discordsrv.common.abstraction.bootstrap.LifecycleManager;
import com.discordsrv.common.core.logging.Logger;
import com.discordsrv.common.core.logging.backend.impl.Log4JLoggerImpl;
import com.discordsrv.forge.util.ForgeEventBusFacade;
import com.discordsrv.modded.DiscordSRVModdedBootstrap;
import com.discordsrv.modded.util.ClassLoaderUtils;
import com.mojang.brigadier.CommandDispatcher;
import dev.vankka.dependencydownload.classpath.ClasspathAppender;
import dev.vankka.dependencydownload.jarinjar.bootstrap.AbstractBootstrap;
import dev.vankka.dependencydownload.jarinjar.bootstrap.classpath.JarInJarClasspathAppender;
import dev.vankka.dependencydownload.jarinjar.classloader.JarInJarClassLoader;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;

public class DiscordSRVForgeBootstrap extends AbstractBootstrap implements DiscordSRVModdedBootstrap {
    private final static String DEPENDENCIES_RUNTIME = /*$ dependencies_file*/"dependencies/runtimeDownload-1.20.1-forge.txt";

    private final Logger logger;
    private final LifecycleManager lifecycleManager;
    private final Path dataDirectory;

    private JarInJarClasspathAppender classpathAppender;
    private ModContainer modContainer;
    private IEventBus modEventBus;
    private MinecraftServer minecraftServer;
    private ForgeDiscordSRV discordSRV;

    private CommandDispatcher<CommandSourceStack> commandDispatcher;

    /**
     * A facade for the forge event bus, compatible with LP's jar-in-jar packaging
     */
    private final ForgeEventBusFacade eventBus;

    public DiscordSRVForgeBootstrap(JarInJarClassLoader classLoader, ModContainer modContainer, IEventBus modEventBus) {
        super(classLoader);
        ClassLoaderUtils.setClassLoader(classLoader);

        this.classpathAppender = new JarInJarClasspathAppender(classLoader);
        this.modContainer = modContainer;
        this.modEventBus = modEventBus;
        this.eventBus = new ForgeEventBusFacade();

        this.logger = new Log4JLoggerImpl(LogManager.getLogger("DiscordSRV"));
        this.dataDirectory = FMLPaths.CONFIGDIR.get().resolve("DiscordSRV");

        try {
            this.lifecycleManager = new LifecycleManager(
                    this.logger,
                    dataDirectory,
                    Collections.singletonList(DEPENDENCIES_RUNTIME),
                    classpathAppender
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.minecraftServer = null;

        this.eventBus.register(this);
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        this.commandDispatcher = event.getDispatcher();
    }

    @SubscribeEvent()
    public void onServerStarting(ServerAboutToStartEvent event) {
        this.minecraftServer = event.getServer();
        this.lifecycleManager.loadAndEnable(() -> this.discordSRV = new ForgeDiscordSRV(this));
    }

    @SubscribeEvent()
    public void onServerStarted(ServerStartedEvent event) {
        if (this.discordSRV == null) {
            this.logger.error("Server started but ForgeDiscordSRV hasn't initialized properly.\n" +
                    "This is likely due to an error during the loading process. Please check the full logs for more details.");
            return;
        }
        this.discordSRV.runServerStarted();
    }

    @SubscribeEvent()
    public void onServerStopping(ServerStoppingEvent event) {
        if (this.discordSRV != null) this.discordSRV.runDisable();
    }

    @Override
    public Logger logger() {
        return logger;
    }

    @Override
    public ClasspathAppender classpathAppender() {
        return classpathAppender;
    }

    @Override
    public ClassLoader classLoader() {
        return getClass().getClassLoader();
    }

    @Override
    public LifecycleManager lifecycleManager() {
        return lifecycleManager;
    }

    @Override
    public Path dataDirectory() {
        return dataDirectory;
    }

    @Override
    public String platformVersion() {
        return "forge testing";
    }

    public MinecraftServer getServer() {
        return minecraftServer;
    }

    @Override
    public CommandDispatcher<CommandSourceStack> getCommandDispatcher() {
        return commandDispatcher;
    }

    public ForgeDiscordSRV getDiscordSRV() {
        return discordSRV;
    }
}
