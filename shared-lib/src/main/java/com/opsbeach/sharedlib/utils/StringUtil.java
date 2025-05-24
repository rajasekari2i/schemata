package com.opsbeach.sharedlib.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Objects;
import java.util.StringJoiner;

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * <p>
 * String operation reusable functions
 * </p>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class StringUtil {

    /**
     * <p>
     * Common method to append string using the ${@code StringJoiner} and return as combine string.
     * </p>
     *
     * @param args - Get list of string.
     * @return Combine the string using String Joiner and return as string.
     */
    public static String constructStringEmptySeparator(String... args) {
        var buildString = new StringJoiner(Constants.EMPTY);
        for (String arg : args) {
            buildString.add(Objects.nonNull(arg) ? arg : Constants.EMPTY);
        }
        return buildString.toString();
    }

    /**
     * Returns a String with capitalizing every word starting letter.
     *
     * @param value - Target String in which every word needs to be capitalized.
     * @return String - Final String with capitalizing every word starting letter.
     */
    public static String capitalizeWord(String value) {
        if (StringUtil.isEmpty(value).equals(Boolean.TRUE)) {
            return Constants.EMPTY;
        }
        var words = value.split("\\s");
        var capitalizeWord = new StringBuilder();
        for (String word : words) {
            var firstLetter = word.substring(0, 1);
            var wordAfterFirstLetter = word.substring(1);
            capitalizeWord.append(firstLetter.toUpperCase()).append(wordAfterFirstLetter).append(" ");
        }
        return capitalizeWord.toString().trim();
    }

    public static Boolean isBlank(final CharSequence cs) {
        final int strLen = length(cs);
        if (strLen == 0) {
            return true;
        }
        for (var i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(cs.charAt(i))) {
                return Boolean.FALSE;
            }
        }
        return Boolean.TRUE;
    }

    private static int length(final CharSequence cs) {
        return cs == null ? 0 : cs.length();
    }

    public static Boolean isEmpty(final CharSequence cs) {
        return cs == null || cs.length() == 0;
    }

    public static void addToJsonNode(ObjectNode node, String key, String value) {
        if (value == null) {
            node.putNull(key);
        } else {
            node.put(key, value);
        }
    }

    public static void addArrayToJsonNode(ObjectNode node, String key, String[] value) {
        if (value == null) {
            node.putNull(key);
        } else {
            var arrayNode = node.putArray(key);
            for (String val : value) arrayNode.add(val);
        }
    }
}