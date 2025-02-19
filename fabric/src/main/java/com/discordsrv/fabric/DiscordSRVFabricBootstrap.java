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

import com.discordsrv.common.abstraction.bootstrap.IBootstrap;
import com.discordsrv.common.abstraction.bootstrap.LifecycleManager;
import com.discordsrv.common.core.logging.Logger;
import com.discordsrv.common.core.logging.backend.impl.Log4JLoggerImpl;
import dev.vankka.dependencydownload.classpath.ClasspathAppender;
import dev.vankka.mcdependencydownload.fabric.classpath.FabricClasspathAppender;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.MinecraftVersion;
import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.LogManager;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;

//? if adventure: <6 {
/*import net.kyori.adventure.platform.fabric.FabricServerAudiences;
*///?} else {
 import net.kyori.adventure.platform.modcommon.MinecraftServerAudiences;
 //?}


public class DiscordSRVFabricBootstrap implements DedicatedServerModInitializer, IBootstrap {
    private final static String DEPENDENCIES_RUNTIME = /*$ dependencies_file*/"dependencies/runtimeDownload-1.21.4.txt";

    private final Logger logger;
    private final ClasspathAppender classpathAppender;
    private final LifecycleManager lifecycleManager;
    private final Path dataDirectory;
    private MinecraftServer minecraftServer;
    private FabricDiscordSRV discordSRV;
    //? if adventure: <6 {
    /*private FabricServerAudiences adventure;
    *///?} else {
    private MinecraftServerAudiences adventure;
     //?}

    public DiscordSRVFabricBootstrap() {
        this.logger = new Log4JLoggerImpl(LogManager.getLogger("DiscordSRV"));
        this.classpathAppender = new FabricClasspathAppender();
        this.dataDirectory = FabricLoader.getInstance().getConfigDir().resolve("DiscordSRV");
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
        this.adventure = null;
    }

    @Override
    public void onInitializeServer() {
        ServerLifecycleEvents.SERVER_STARTING.register(minecraftServer -> {
            this.minecraftServer = minecraftServer;
            //? if adventure: <6 {
            /*this.adventure = FabricServerAudiences.of(minecraftServer);
            *///?} else {
            this.adventure = MinecraftServerAudiences.of(minecraftServer);
             //?}
            lifecycleManager.loadAndEnable(() -> this.discordSRV = new FabricDiscordSRV(this));
        });

        ServerLifecycleEvents.SERVER_STARTED.register(minecraftServer -> this.discordSRV.runServerStarted());

        ServerLifecycleEvents.SERVER_STOPPING.register(minecraftServer -> {
            if (this.discordSRV != null) this.discordSRV.runDisable();
        });
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
        //? if minecraft: <1.18 {
        /*MinecraftVersion version = (MinecraftVersion) MinecraftVersion.GAME_VERSION;
        *///?} else {
        MinecraftVersion version = (MinecraftVersion) MinecraftVersion.CURRENT;
         //?}
        return version.getName() + " (from Fabric)"; //TODO: get current build version for Fabric
    }

    public MinecraftServer getServer() {
        return minecraftServer;
    }

    public FabricDiscordSRV getDiscordSRV() {
        return discordSRV;
    }

    //? if adventure: <6 {
    /*public FabricServerAudiences getAdventure() {
        return adventure;
    }
    *///?} else {
     public MinecraftServerAudiences getAdventure() {
        return adventure;
    }//?}
}
