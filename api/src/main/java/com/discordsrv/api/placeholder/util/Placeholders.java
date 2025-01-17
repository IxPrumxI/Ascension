/*
 * This file is part of the DiscordSRV API, licensed under the MIT License
 * Copyright (c) 2016-2025 Austin "Scarsz" Shapiro, Henri "Vankka" Schubin and DiscordSRV contributors
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

package com.discordsrv.api.placeholder.util;

import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Placeholders {

    private final String inputText;
    private final Map<Pattern, Function<Matcher, Object>> replacements = new LinkedHashMap<>();

    public Placeholders(String inputText) {
        this.inputText = inputText;
    }

    @NotNull
    public Placeholders addAll(Map<Pattern, Function<Matcher, Object>> replacements) {
        this.replacements.putAll(replacements);
        return this;
    }

    @NotNull
    public Placeholders replace(String target, Object replacement) {
        return replace(target, matcher -> replacement);
    }

    @NotNull
    public Placeholders replaceAll(Pattern pattern, Object replacement) {
        return replaceAll(pattern, matcher -> replacement);
    }

    @NotNull
    public Placeholders replace(String target, Supplier<Object> replacement) {
        return replaceAll(Pattern.compile(target, Pattern.LITERAL), matcher -> replacement);
    }

    @NotNull
    public Placeholders replaceAll(Pattern pattern, Supplier<Object> replacement) {
        return replaceAll(pattern, matcher -> replacement);
    }

    @NotNull
    public Placeholders replace(String target, Function<Matcher, Object> replacement) {
        return replaceAll(Pattern.compile(target, Pattern.LITERAL), replacement);
    }

    @NotNull
    public Placeholders replaceAll(Pattern pattern, Function<Matcher, Object> replacement) {
        this.replacements.put(pattern, replacement);
        return this;
    }

    @Override
    @NotNull
    public String toString() {
        String input = inputText;
        for (Map.Entry<Pattern, Function<Matcher, Object>> entry : replacements.entrySet()) {
            Pattern pattern = entry.getKey();
            Matcher matcher = pattern.matcher(input);
            StringBuffer buffer = new StringBuffer();
            int lastEnd = -1;
            while (matcher.find()) {
                lastEnd = matcher.end();
                Function<Matcher, Object> replacement = entry.getValue();
                Object value = replacement.apply(matcher);

                matcher.appendReplacement(buffer, Matcher.quoteReplacement(String.valueOf(value)));
            }
            if (lastEnd == -1) {
                continue;
            }
            buffer.append(input.substring(lastEnd));
            input = buffer.toString();
        }
        return input;
    }
}
