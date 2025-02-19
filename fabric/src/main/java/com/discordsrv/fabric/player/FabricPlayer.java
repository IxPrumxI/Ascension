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

package com.discordsrv.fabric.player;

import com.discordsrv.common.DiscordSRV;
import com.discordsrv.common.abstraction.player.IPlayer;
import com.discordsrv.common.abstraction.player.provider.model.SkinInfo;
import com.discordsrv.common.abstraction.player.provider.model.Textures;
import com.discordsrv.fabric.FabricDiscordSRV;
import com.discordsrv.fabric.command.game.sender.FabricCommandSender;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

//? if minecraft: >1.19 {
import net.minecraft.network.packet.s2c.play.ChatSuggestionsS2CPacket;
//?}

public class FabricPlayer extends FabricCommandSender implements IPlayer {

    private final ServerPlayerEntity player;

    public FabricPlayer(FabricDiscordSRV discordSRV, ServerPlayerEntity player) {
        super(discordSRV, player.getCommandSource());
        this.player = player;
    }

    @Override
    public DiscordSRV discordSRV() {
        return discordSRV;
    }

    @Override
    public @NotNull String username() {
        return player.getName().getString();
    }

    @Override
    public @Nullable Locale locale() {
        // if java lower than 19
        //? if java: >19 || minecraft: <1.20.2 {
        return Locale.getDefault();
        //?} else {
        /*return Locale.of(player.getClientOptions().language());
        *///?}
    }

    @Override
    public CompletableFuture<Void> kick(Component component) {
        player.networkHandler.disconnect(Text.of(component.toString()));
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void addChatSuggestions(Collection<String> suggestions) {
        //? if minecraft: >1.19 {
        ChatSuggestionsS2CPacket packet = new ChatSuggestionsS2CPacket(ChatSuggestionsS2CPacket.Action.ADD, new ArrayList<>(suggestions));
        player.networkHandler.sendPacket(packet);
        //?}
    }

    @Override
    public void removeChatSuggestions(Collection<String> suggestions) {
        //? if minecraft: >1.19 {
        ChatSuggestionsS2CPacket packet = new ChatSuggestionsS2CPacket(ChatSuggestionsS2CPacket.Action.REMOVE, new ArrayList<>(suggestions));
        player.networkHandler.sendPacket(packet);
        //?}
    }

    @Override
    public @Nullable SkinInfo skinInfo() {
        //? if minecraft: >1.20.1 {
        return Textures.getFromBase64(discordSRV, player.getGameProfile().getProperties().get(Textures.KEY).iterator().next().value()).getSkinInfo();
        //?} else {
        /*return Textures.getFromBase64(discordSRV, player.getGameProfile().getProperties().get(Textures.KEY).iterator().next().getValue()).getSkinInfo();
        *///?}
    }

    @Override
    public @NotNull Identity identity() {
        //? if adventure: >=5.11.0 {
        return player.identity();
        //?} else {
        /*return Identity.identity(player.getUuid());
        *///?}
    }

    @Override
    public @NotNull Component displayName() {
        //? if adventure: >=5.3.0 {
        return player.getOrDefaultFrom(
                Identity.DISPLAY_NAME,
                () -> Component.text(player.getName().getString())
        );
        //?} else {
        /*return Component.text(player.getName().getString());
        *///?}

    }

    @Override
    public String toString() {
        return "FabricPlayer{" + username() + "}";
    }
}
