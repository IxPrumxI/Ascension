/*
 * This file is part of the DiscordSRV API, licensed under the MIT License
 * Copyright (c) 2016-2022 Austin "Scarsz" Shapiro, Henri "Vankka" Schubin and DiscordSRV contributors
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

package com.discordsrv.api.discord.api.util;

import java.util.regex.Matcher;

public final class DiscordFormattingUtil {

    private DiscordFormattingUtil() {}

    public static String escapeContent(String content) {
        content = escapeChars(content, '*', '_', '|', '`', '~', ':', '[');
        content = escapeQuote(content);
        content = escapeMentions(content);
        return content;
    }

    private static String escapeChars(String input, char... characters) {
        for (char character : characters) {
            input = input.replace(
                    String.valueOf(character),
                    "\\" + character);
        }
        return input;
    }

    private static String escapeQuote(String input) {
        return input.replaceAll("^>", Matcher.quoteReplacement("\\>"));
    }

    private static String escapeMentions(String input) {
        return input.replaceAll("<([@#])", Matcher.quoteReplacement("\\<") + "$1");
    }
}
