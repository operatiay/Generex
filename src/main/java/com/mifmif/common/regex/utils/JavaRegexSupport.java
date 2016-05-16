package com.mifmif.common.regex.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JavaRegexSupport {
    /**
     * lookahead needs to become a separate regex, which is to be combined with the rest
     * @param javaRegex
     * @return
     */
    public static String replacePositiveLookaheadFromRegex(final String javaRegex) {
        return replaceLookaheadFromRegex(javaRegex, "?=", "&");
    }

    public static String replaceNegativeLookaheadFromRegex(final String javaRegex) {
        return replaceLookaheadFromRegex(javaRegex, "?!", "&~");
    }

    public static String replacePositiveLookbehindFromRegex(final String javaRegex) {
        return replaceLookbehindFromRegex(javaRegex, "?<=", "&");
    }

    public static String replaceNegativeLookbehindFromRegex(final String javaRegex) {
        return replaceLookbehindFromRegex(javaRegex, "?<!", "&~");
    }

    private static String replaceLookaheadFromRegex(final String javaRegex, final String lookaheadKey, final String translationKey) {
        String result = "";
        if (!javaRegex.contains(lookaheadKey)) {
            result = javaRegex;
        } else {
            LookaroundMatch matchData = matchLookaround(javaRegex, lookaheadKey);
            if (matchData.pre.length() > 0 || matchData.post.length() > 0) {
                result =  matchData.pre + "((" + matchData.post + ")" +
                        translationKey + "(" + matchData.look + "))";
            }
        }
        return result;
    }


    private static class LookaroundMatch {
        String pre;
        String post;
        String look;
        LookaroundMatch(String pre, String look, String post) {
            this.pre = pre;
            this.look = look;
            this.post = post;
        }
    }

    private static LookaroundMatch matchLookaround(final String javaRegex, final String lookKey) {
        final String GROUP_PRE_LOOK = "preLook";
        final String GROUP_LOOK = "look";
        final String GROUP_POST_LOOK = "postLook";
        String regexForLook = "(?<" + GROUP_PRE_LOOK + ">.*?)" +
                "(\\(" + Pattern.quote(lookKey) + "(?<" + GROUP_LOOK + ">.*)\\))" +
                "(?<" + GROUP_POST_LOOK + ">.*?)";
        Pattern lookPattern = Pattern.compile(regexForLook);
        Matcher matcher = lookPattern.matcher(javaRegex);
        boolean matches = matcher.matches();
        if (! matches) {
            throw new IllegalStateException("This regex should have had a lookaround pattern");
        }
        return new LookaroundMatch(matcher.group(GROUP_PRE_LOOK), matcher.group(GROUP_LOOK), matcher.group(GROUP_POST_LOOK));
    }

    private static String replaceLookbehindFromRegex(final String javaRegex, final String lookbehindKey, final String translationKey) {

        String result = "";
        if (!javaRegex.contains(lookbehindKey)) {
            result = javaRegex;
        } else {
            LookaroundMatch matchData = matchLookaround(javaRegex, lookbehindKey);

            if (matchData.pre.length() > 0 || matchData.look.length() > 0) {
                result =  "((" + matchData.pre + ")" +
                        translationKey + "(.*" + matchData.look + "))" + matchData.post;
            }
        }
        return result;
    }
}
