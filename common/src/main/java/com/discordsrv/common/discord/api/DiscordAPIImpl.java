/*
 * This file is part of DiscordSRV, licensed under the GPLv3 License
 * Copyright (c) 2016-2021 Austin "Scarsz" Shapiro, Henri "Vankka" Schubin and DiscordSRV contributors
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

package com.discordsrv.common.discord.api;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.WebhookClientBuilder;
import com.discordsrv.api.discord.api.DiscordAPI;
import com.discordsrv.api.discord.api.entity.channel.DiscordTextChannel;
import com.discordsrv.api.discord.api.entity.guild.DiscordGuild;
import com.discordsrv.api.discord.api.entity.user.DiscordUser;
import com.discordsrv.api.discord.api.exception.NotReadyException;
import com.discordsrv.api.discord.api.exception.UnknownChannelException;
import com.discordsrv.common.DiscordSRV;
import com.discordsrv.common.config.main.channels.BaseChannelConfig;
import com.discordsrv.common.config.main.channels.ChannelConfig;
import com.discordsrv.common.config.main.channels.ChannelConfigHolder;
import com.discordsrv.common.discord.api.channel.DiscordTextChannelImpl;
import com.discordsrv.common.discord.api.guild.DiscordGuildImpl;
import com.discordsrv.common.discord.api.user.DiscordUserImpl;
import com.github.benmanes.caffeine.cache.*;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.Webhook;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

public class DiscordAPIImpl implements DiscordAPI {

    private final DiscordSRV discordSRV;

    private final AsyncLoadingCache<String, WebhookClient> cachedClients = Caffeine.newBuilder()
            .removalListener((RemovalListener<String, WebhookClient>) (id, client, cause) -> {
                if (client != null) {
                    client.close();
                }
            })
            .expireAfter(new CacheExpiry())
            .buildAsync(new CacheLoader());

    public DiscordAPIImpl(DiscordSRV discordSRV) {
        this.discordSRV = discordSRV;
    }

    public WebhookClient getWebhookClient(String channelId) {
        CompletableFuture<WebhookClient> clientFuture = cachedClients.getIfPresent(channelId);
        if (clientFuture == null) {
            return null;
        }

        return clientFuture.join();
    }

    public CompletableFuture<WebhookClient> queryWebhookClient(String channelId) {
        return cachedClients.get(channelId);
    }

    @Override
    public Optional<DiscordTextChannel> getTextChannelById(@NotNull String id) {
        JDA jda = discordSRV.jda();
        if (jda == null) {
            return Optional.empty();
        }

        TextChannel textChannel = jda.getTextChannelById(id);
        return textChannel != null
                ? Optional.of(new DiscordTextChannelImpl(discordSRV, textChannel))
                : Optional.empty();
    }

    @Override
    public Optional<DiscordGuild> getGuildById(@NotNull String id) {
        JDA jda = discordSRV.jda();
        if (jda == null) {
            return Optional.empty();
        }

        Guild guild = jda.getGuildById(id);
        return guild != null
                ? Optional.of(new DiscordGuildImpl(discordSRV, guild))
                : Optional.empty();
    }

    @Override
    public Optional<DiscordUser> getUserById(@NotNull String id) {
        JDA jda = discordSRV.jda();
        if (jda == null) {
            return Optional.empty();
        }

        User user = jda.getUserById(id);
        return user != null
                ? Optional.of(new DiscordUserImpl(user))
                : Optional.empty();
    }

    private class CacheLoader implements AsyncCacheLoader<String, WebhookClient> {

        @Override
        public @NonNull CompletableFuture<WebhookClient> asyncLoad(@NonNull String channelId, @NonNull Executor executor) {
            CompletableFuture<WebhookClient> future = new CompletableFuture<>();

            JDA jda = discordSRV.jda();
            if (jda == null) {
                future.completeExceptionally(new NotReadyException());
                return future;
            }

            TextChannel textChannel = jda.getTextChannelById(channelId);
            if (textChannel == null) {
                future.completeExceptionally(new UnknownChannelException(null));
                return future;
            }

            return textChannel.retrieveWebhooks().submit().thenApply(webhooks -> {
                Webhook hook = null;
                for (Webhook webhook : webhooks) {
                    User user = webhook.getOwnerAsUser();
                    if (user == null
                            || !user.getId().equals(jda.getSelfUser().getId())
                            || !webhook.getName().equals("DiscordSRV")) {
                        continue;
                    }

                    hook = webhook;
                    break;
                }

                return hook;
            }).thenCompose(webhook -> {
                if (webhook != null) {
                    CompletableFuture<Webhook> completableFuture = new CompletableFuture<>();
                    completableFuture.complete(webhook);
                    return completableFuture;
                }

                return textChannel.createWebhook("DiscordSRV").submit();
            }).thenApply(webhook ->
                    WebhookClientBuilder.fromJDA(webhook)
                            .setHttpClient(jda.getHttpClient())
                            .setExecutorService(discordSRV.scheduler().executor())
                            .build()
            );
        }
    }

    private class CacheExpiry implements Expiry<String, WebhookClient> {

        private boolean isConfiguredChannel(String channelId) {
            for (ChannelConfigHolder value : discordSRV.config().channels.values()) {
                BaseChannelConfig config = value.get();
                if (config instanceof ChannelConfig
                        && ((ChannelConfig) config).channelIds.contains(channelId)) {
                    return true;
                }
            }
            return false;
        }

        private long expireAfterWrite(String channelId) {
            return isConfiguredChannel(channelId) ? Long.MAX_VALUE : TimeUnit.MINUTES.toNanos(15);
        }

        @Override
        public long expireAfterCreate(@NonNull String channelId, @NonNull WebhookClient webhookClient, long currentTime) {
            return expireAfterWrite(channelId);
        }

        @Override
        public long expireAfterUpdate(@NonNull String channelId, @NonNull WebhookClient webhookClient, long currentTime, @NonNegative long currentDuration) {
            return expireAfterWrite(channelId);
        }

        @Override
        public long expireAfterRead(@NonNull String channelId, @NonNull WebhookClient webhookClient, long currentTime, @NonNegative long currentDuration) {
            return isConfiguredChannel(channelId) ? Long.MAX_VALUE : TimeUnit.MINUTES.toNanos(10);
        }
    }
}
