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

package com.discordsrv.common.component.renderer;

import com.discordsrv.api.component.GameTextBuilder;
import com.discordsrv.api.discord.entity.DiscordUser;
import com.discordsrv.api.discord.entity.guild.DiscordCustomEmoji;
import com.discordsrv.api.discord.entity.guild.DiscordGuild;
import com.discordsrv.api.discord.entity.guild.DiscordGuildMember;
import com.discordsrv.api.discord.entity.guild.DiscordRole;
import com.discordsrv.api.event.events.message.process.discord.DiscordChatMessageCustomEmojiRenderEvent;
import com.discordsrv.common.DiscordSRV;
import com.discordsrv.common.component.util.ComponentUtil;
import com.discordsrv.common.config.main.channels.DiscordToMinecraftChatConfig;
import dev.vankka.mcdiscordreserializer.renderer.implementation.DefaultMinecraftRenderer;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.utils.MiscUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DiscordSRVMinecraftRenderer extends DefaultMinecraftRenderer {

    private static final Pattern MESSAGE_URL_PATTERN = Pattern.compile("https://(?:(?:ptb|canary)\\.)?discord\\.com/channels/[0-9]{16,20}/([0-9]{16,20})/[0-9]{16,20}");
    private static final ThreadLocal<Context> CONTEXT = new ThreadLocal<>();
    private final DiscordSRV discordSRV;

    public DiscordSRVMinecraftRenderer(DiscordSRV discordSRV) {
        this.discordSRV = discordSRV;
    }

    public static void runInContext(
            DiscordGuild guild,
            DiscordToMinecraftChatConfig config,
            Runnable runnable
    ) {
        getWithContext(guild, config, () -> {
            runnable.run();
            return null;
        });
    }

    public static <T> T getWithContext(
            DiscordGuild guild,
            DiscordToMinecraftChatConfig config,
            Supplier<T> supplier
    ) {
        Context oldValue = CONTEXT.get();
        CONTEXT.set(new Context(guild, config));
        T output = supplier.get();
        CONTEXT.set(oldValue);
        return output;
    }

    @Override
    public Component appendLink(@NotNull Component part, String link) {
        JDA jda = discordSRV.jda();

        if (jda != null) {
            Matcher matcher = MESSAGE_URL_PATTERN.matcher(link);
            if (matcher.matches()) {
                String channel = matcher.group(1);
                GuildChannel guildChannel = jda.getGuildChannelById(channel);

                Context context = CONTEXT.get();
                String format = context != null ? context.config.mentions.messageUrl : null;
                if (format == null || guildChannel == null) {
                    return super.appendLink(part, link);
                }

                return Component.text()
                        .clickEvent(ClickEvent.openUrl(link))
                        .append(
                                ComponentUtil.fromAPI(
                                        discordSRV.componentFactory()
                                                .textBuilder(format)
                                                .addContext(guildChannel)
                                                .addPlaceholder("jump_url", link)
                                                .applyPlaceholderService()
                                                .build()
                                )
                        )
                        .build();
            }
        }

        return super.appendLink(part, link);
    }

    @Override
    public @NotNull Component appendChannelMention(@NotNull Component component, @NotNull String id) {
        Context context = CONTEXT.get();
        DiscordToMinecraftChatConfig.Mentions.Format format = context != null ? context.config.mentions.channel : null;
        if (format == null) {
            return component.append(Component.text("<#" + id + ">"));
        }

        JDA jda = discordSRV.jda();
        if (jda == null) {
            return Component.empty();
        }

        GuildChannel guildChannel = jda.getGuildChannelById(id);

        return component.append(ComponentUtil.fromAPI(
                discordSRV.componentFactory()
                        .textBuilder(guildChannel != null ? format.format : format.unknownFormat)
                        .addContext(guildChannel)
                        .applyPlaceholderService()
                        .build()
        ));
    }

    @Override
    public @NotNull Component appendUserMention(@NotNull Component component, @NotNull String id) {
        Context context = CONTEXT.get();
        DiscordToMinecraftChatConfig.Mentions.Format format = context != null ? context.config.mentions.user : null;
        DiscordGuild guild = context != null ? context.guild : null;
        if (format == null || guild == null) {
            return component.append(Component.text("<@" + id + ">"));
        }

        long userId = MiscUtil.parseLong(id);
        DiscordUser user = discordSRV.discordAPI().getUserById(userId);
        DiscordGuildMember member = guild.getMemberById(userId);

        GameTextBuilder builder = discordSRV.componentFactory()
                .textBuilder(user != null ? format.format : format.unknownFormat);

        if (user != null) {
            builder.addContext(user);
        }
        if (member != null) {
            builder.addContext(member);
        }

        return component.append(ComponentUtil.fromAPI(
                builder.applyPlaceholderService().build()
        ));
    }

    @Override
    public @NotNull Component appendRoleMention(@NotNull Component component, @NotNull String id) {
        Context context = CONTEXT.get();
        DiscordToMinecraftChatConfig.Mentions.Format format = context != null ? context.config.mentions.role : null;
        if (format == null) {
            return component.append(Component.text("<#" + id + ">"));
        }

        long roleId = MiscUtil.parseLong(id);
        DiscordRole role = discordSRV.discordAPI().getRoleById(roleId);

        GameTextBuilder builder = discordSRV.componentFactory()
                .textBuilder(role != null ? format.format : format.unknownFormat);

        if (role != null) {
            builder.addContext(role);
        }

        return component.append(ComponentUtil.fromAPI(
                builder.applyPlaceholderService().build()
        ));
    }

    @Override
    public @NotNull Component appendEmoteMention(
            @NotNull Component component,
            @NotNull String name,
            @NotNull String id
    ) {
        Context context = CONTEXT.get();
        DiscordToMinecraftChatConfig.EmoteBehaviour behaviour = context != null ? context.config.customEmojiBehaviour : null;
        if (behaviour == null || behaviour == DiscordToMinecraftChatConfig.EmoteBehaviour.HIDE) {
            return component;
        }

        System.out.println(name);
        long emojiId = MiscUtil.parseLong(id);
        DiscordCustomEmoji emoji = discordSRV.discordAPI().getEmojiById(emojiId);
        if (emoji == null) {
            return component;
        }

        DiscordChatMessageCustomEmojiRenderEvent event = new DiscordChatMessageCustomEmojiRenderEvent(emoji);
        discordSRV.eventBus().publish(event);

        if (event.isProcessed()) {
            Component rendered = ComponentUtil.fromAPI(event.getRenderedEmojiFromProcessing());
            return component.append(rendered);
        }

        switch (behaviour) {
            case NAME:
                return component.append(Component.text(":" + emoji.getName() + ":"));
            case BLANK:
            default:
                return component;
        }
    }

    private static class Context {

        private final DiscordGuild guild;
        private final DiscordToMinecraftChatConfig config;

        public Context(DiscordGuild guild, DiscordToMinecraftChatConfig config) {
            this.guild = guild;
            this.config = config;
        }
    }
}
