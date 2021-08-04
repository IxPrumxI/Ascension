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

package com.discordsrv.common.placeholder.provider;

import com.discordsrv.api.placeholder.Placeholder;
import com.discordsrv.api.placeholder.PlaceholderLookupResult;
import com.discordsrv.common.placeholder.provider.util.PlaceholderMethodUtil;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;

public class AnnotationPlaceholderProvider implements PlaceholderProvider {

    private final Placeholder annotation;

    private final Class<?> type;
    private final Method method;
    private final Field field;

    public AnnotationPlaceholderProvider(Placeholder annotation, Class<?> type, Method method) {
        this.annotation = annotation;
        this.type = type;
        this.method = method;
        this.field = null;
    }

    public AnnotationPlaceholderProvider(Placeholder annotation, Class<?> type, Field field) {
        this.annotation = annotation;
        this.type = type;
        this.method = null;
        this.field = field;
    }

    @Override
    public @NotNull PlaceholderLookupResult lookup(@NotNull String placeholder, @NotNull Set<Object> context) {
        if (!annotation.value().equals(placeholder) || (type != null && context.isEmpty())) {
            return PlaceholderLookupResult.UNKNOWN_PLACEHOLDER;
        }

        Object instance = null;
        if (type != null) {
            for (Object o : context) {
                if (type.isAssignableFrom(o.getClass())) {
                    instance = o;
                }
            }
            if (instance == null) {
                return PlaceholderLookupResult.UNKNOWN_PLACEHOLDER;
            }
        }

        if (field != null) {
            try {
                return PlaceholderLookupResult.success(field.get(instance));
            } catch (IllegalAccessException e) {
                e.printStackTrace(); // TODO
                return PlaceholderLookupResult.LOOKUP_FAILED;
            }
        } else {
            try {
                assert method != null;
                return PlaceholderMethodUtil.lookup(method, instance, context);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace(); // TODO
                return PlaceholderLookupResult.LOOKUP_FAILED;
            }
        }
    }
}
