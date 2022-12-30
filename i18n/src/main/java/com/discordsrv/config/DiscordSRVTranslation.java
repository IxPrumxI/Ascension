/*
 * This file is part of DiscordSRV, licensed under the GPLv3 License
 * Copyright (c) 2016-2022 Austin "Scarsz" Shapiro, Henri "Vankka" Schubin and DiscordSRV contributors
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

package com.discordsrv.config;

import com.discordsrv.bukkit.config.manager.BukkitConfigManager;
import com.discordsrv.bukkit.config.manager.BukkitConnectionConfigManager;
import com.discordsrv.common.DiscordSRV;
import com.discordsrv.common.config.Config;
import com.discordsrv.common.config.annotation.Untranslated;
import com.discordsrv.common.config.manager.manager.ConfigurateConfigManager;
import com.discordsrv.common.config.manager.manager.TranslatedConfigManager;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.objectmapping.ObjectMapper;
import org.spongepowered.configurate.objectmapping.meta.Processor;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * A java application to generate a translation file that has comments as options.
 */
public final class DiscordSRVTranslation {

    private static final DiscordSRV discordSRV = new MockDiscordSRV();
    private static final List<TranslatedConfigManager<? extends Config, ?>> CONFIGS = Arrays.asList(
            new BukkitConfigManager(discordSRV),
            new BukkitConnectionConfigManager(discordSRV)
    );

    public static void main(String[] args) throws ConfigurateException {
        new DiscordSRVTranslation().run();
    }

    private DiscordSRVTranslation() {}

    public void run() throws ConfigurateException {
        Processor.Factory<Untranslated, Object> untranslatedProcessorFactory = (data, v1) -> (v2, destination) -> {
            try {
                Untranslated.Type type = data.value();
                if (type.isValue()) {
                    Object value = destination.get(Object.class);
                    if (type.isComment()/* || !(value instanceof String)*/) {
                        destination.set(null);
                    } else {
                        destination.set("");
                    }
                } else if (type.isComment() && destination instanceof CommentedConfigurationNode) {
                    ((CommentedConfigurationNode) destination).comment(null);
                }
            } catch (SerializationException e) {
                e.printStackTrace();
                System.exit(1);
            }
        };

        CommentedConfigurationNode node = CommentedConfigurationNode.root();
        for (ConfigurateConfigManager<?, ?> configManager : CONFIGS) {
            Config config = (Config) configManager.createConfiguration();
            String fileIdentifier = config.getFileName();
            ConfigurationNode commentSection = node.node(fileIdentifier + "_comments");
            
            String header = configManager.configNodeOptions().header();
            if (header != null) {
                commentSection.node("$header").set(header);
            }

            ObjectMapper.Factory mapperFactory = configManager.configObjectMapperBuilder()
                    .addProcessor(Untranslated.class, untranslatedProcessorFactory)
                    .build();

            TranslationConfigManagerProxy<?> configManagerProxy = new TranslationConfigManagerProxy<>(discordSRV, mapperFactory, configManager);
            CommentedConfigurationNode configurationNode = configManagerProxy.getDefaultNode(mapperFactory);

            convertCommentsToOptions(configurationNode, commentSection);

            processUnwantedValues(configurationNode);
            ConfigurationNode section = node.node(fileIdentifier);
            ConfigurationNode configSection = section.set(configurationNode);
            section.set(configSection);
        }

        YamlConfigurationLoader.builder()
                .file(new File("i18n", "source.yaml"))
                .build()
                .save(node);
    }

    public void processUnwantedValues(ConfigurationNode node) throws SerializationException {
        Map<Object, ? extends ConfigurationNode> children = node.childrenMap();
        Object value;
        if (children.isEmpty() && (!((value = node.get(Object.class)) instanceof String) || ((String) value).isEmpty())) {
            node.set(null);
            return;
        }

        boolean allChildrenEmpty = true;
        for (ConfigurationNode child : children.values()) {
            processUnwantedValues(child);

            if (child.virtual() || child.isNull() || child.empty()) {
                allChildrenEmpty = false;
                break;
            }
        }
        if (!allChildrenEmpty) {
            node.set(null);
        }
    }

    public void convertCommentsToOptions(ConfigurationNode node, ConfigurationNode commentParent) throws SerializationException {
        if (node instanceof CommentedConfigurationNode) {
            CommentedConfigurationNode commentedNode = (CommentedConfigurationNode) node;
            String comment = commentedNode.comment();
            if (comment != null) {
                List<Object> path = new ArrayList<>(Arrays.asList(commentedNode.path().array()));
                path.add("_comment");
                commentParent.node(path).set(comment);
            }
        }
        if (node.empty()) {
            node.set(null);
            return;
        }
        for (ConfigurationNode value : node.childrenMap().values()) {
            convertCommentsToOptions(value, commentParent);
        }
    }
}
