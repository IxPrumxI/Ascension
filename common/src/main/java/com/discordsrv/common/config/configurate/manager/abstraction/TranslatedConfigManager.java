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

package com.discordsrv.common.config.configurate.manager.abstraction;

import com.discordsrv.common.DiscordSRV;
import com.discordsrv.common.config.Config;
import com.discordsrv.common.exception.ConfigException;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.loader.AbstractConfigurationLoader;
import org.spongepowered.configurate.objectmapping.ObjectMapper;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class TranslatedConfigManager<T extends Config, LT extends AbstractConfigurationLoader<CommentedConfigurationNode>>
        extends ConfigurateConfigManager<T, LT> {

    private String header;

    public TranslatedConfigManager(DiscordSRV discordSRV) {
        super(discordSRV);
    }

    @Override
    public void load() throws ConfigException {
        super.reload();
        translate();
        super.save();
    }

    @Override
    public ConfigurationOptions configurationOptions(ObjectMapper.Factory objectMapper) {
        ConfigurationOptions options = super.configurationOptions(objectMapper);
        if (header != null) {
            options = options.header(header);
        }
        return options;
    }

    @Override
    protected @Nullable ConfigurationNode getTranslation() throws ConfigurateException {
        ConfigurationNode translation = getTranslationRoot();
        if (translation == null) {
            return null;
        }
        translation = translation.copy();
        translation.node("_comments").set(null);
        return translation;
    }

    @SuppressWarnings("unchecked")
    public void translate() throws ConfigException {
        T config = config();
        if (config == null) {
            return;
        }

        try {
            ConfigurationNode translationRoot = getTranslationRoot();
            if (translationRoot == null) {
                return;
            }

            String fileIdentifier = config.getFileName();
            ConfigurationNode translation = translationRoot.node(fileIdentifier);
            ConfigurationNode comments = translationRoot.node(fileIdentifier + "_comments");

            CommentedConfigurationNode node = loader().createNode();
            this.header = comments.node("$header").getString();

            save(config, (Class<T>) config.getClass(), node);
            translateNode(node, translation, comments);
        } catch (ConfigurateException e) {
            throw new ConfigException(e);
        }
    }

    private ConfigurationNode getTranslationRoot() throws ConfigurateException {
        String languageCode = discordSRV.locale().getISO3Language();
        URL resourceURL = discordSRV.getClass().getClassLoader()
                .getResource("translations/" + languageCode + ".yml");
        if (resourceURL == null) {
            return null;
        }

        return YamlConfigurationLoader.builder().url(resourceURL).build().load();
    }

    private void translateNode(
            CommentedConfigurationNode node,
            ConfigurationNode translations,
            ConfigurationNode commentTranslations
    ) throws SerializationException {
        List<Object> path = new ArrayList<>(Arrays.asList(node.path().array()));

        String translation = translations.node(path).getString();
        if (translation != null) {
            node.set(translation);
        }

        path.add("_comment");
        String commentTranslation = commentTranslations.node(path).getString();
        if (commentTranslation != null) {
            node.comment(commentTranslation);
        }

        for (CommentedConfigurationNode child : node.childrenMap().values()) {
            translateNode(child, translations, commentTranslations);
        }
    }

}