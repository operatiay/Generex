package com.mifmif.common.regex;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.util.List;

public class GeneRExAllValuesTest {
    private static final GeneREx generex = new GeneREx("[0-3]([a-c]|[e-g]{1,2})");
    private static final String[] allValues = {
            "0a", "0b", "0c", "0e", "0ee", "0ef", "0eg",
            "0f", "0fe", "0ff", "0fg",
            "0g", "0ge", "0gf", "0gg",

            "1a", "1b", "1c", "1e", "1ee", "1ef", "1eg",
            "1f", "1fe", "1ff", "1fg",
            "1g", "1ge", "1gf", "1gg",

            "2a", "2b", "2c", "2e", "2ee", "2ef", "2eg",
            "2f", "2fe", "2ff", "2fg",
            "2g", "2ge", "2gf", "2gg",

            "3a", "3b", "3c", "3e", "3ee", "3ef", "3eg",
            "3f", "3fe", "3ff", "3fg",
            "3g", "3ge", "3gf", "3gg"};

    /**
     * generate the second String in lexicographical order that match the given Regex.
     */
    @Test
    public void testSpecificValue() {
        String secondString = generex.getMatchedString(2);
        String expectedResult = "0b";
        Assertions.assertThat(secondString).isEqualTo(expectedResult);
    }

    @Test
    public void testAllValues() {
        List<String> matchedStrs = generex.getAllMatchedStrings();
        Assertions.assertThat(matchedStrs).containsOnly(allValues);
    }

    @Test
    public void testRandom() {
        String random = generex.random();
        Assertions.assertThat(random).isIn((Object[]) allValues);
    }

}
