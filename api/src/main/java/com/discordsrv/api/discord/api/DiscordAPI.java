/*
 * This file is part of the DiscordSRV API, licensed under the MIT License
 * Copyright (c) 2016-2021 Austin "Scarsz" Shapiro, Henri "Vankka" Schubin and DiscordSRV contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.discordsrv.api.discord.api;

import com.discordsrv.api.discord.api.entity.channel.DiscordTextChannel;
import com.discordsrv.api.discord.api.entity.guild.DiscordGuild;
import com.discordsrv.api.discord.api.entity.user.DiscordUser;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * A basic Discord API wrapper for a limited amount of functions, with a minimal amount of breaking changes.
 */
public interface DiscordAPI {

    /**
     * Gets a Discord text channel by id.
     * @param id the id for the text channel
     * @return the text channel
     */
    Optional<DiscordTextChannel> getTextChannelById(@NotNull String id);

    /**
     * Gets a Discord server by id.
     * @param id the id for the Discord server
     * @return the Discord server
     */
    Optional<DiscordGuild> getGuildById(@NotNull String id);

    /**
     * Gets a Discord user by id.
     * @param id the id for the Discord user
     * @return the Discord user
     */
    Optional<DiscordUser> getUserById(@NotNull String id);
}
