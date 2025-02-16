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

package com.discordsrv.fabric.core.component;

import com.discordsrv.api.component.MinecraftComponent;
import com.discordsrv.common.core.component.ComponentFactory;
import com.discordsrv.common.util.ComponentUtil;
import com.discordsrv.fabric.FabricDiscordSRV;
import net.kyori.adventure.text.Component;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;

public class FabricComponentFactory extends ComponentFactory {

    public FabricComponentFactory(FabricDiscordSRV discordSRV) {
        super(discordSRV);
    }

    public MinecraftComponent fromNative(Text text) {
        return fromJson(Text.Serialization.toJsonString(text, DynamicRegistryManager.of(Registries.REGISTRIES)));
    }

    public Text toNative(Component component) {
        return toNative(ComponentUtil.toAPI(component));
    }

    public Text toNative(MinecraftComponent component) {
        return Text.Serialization.fromJson(component.asJson(), DynamicRegistryManager.of(Registries.REGISTRIES));
    }
}
