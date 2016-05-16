package com.mifmif.common.regex;

import org.assertj.core.api.Assertions;
import org.junit.Test;

public class JavaRegexToGeneRExTest {

    /**
     * result should internally be (?=someregex)someotherregex -> (someotherregex)&(someregex)
     */
    @Test
    public void testPositiveLookahead() {
        String javaRegex = "(?=[a-c]{2,4})[b]{3}";
        GeneREx generex = new GeneREx(javaRegex);
        String generatedValue = generex.random();
        Assertions.assertThat(generatedValue).as("generated value").matches(javaRegex);
        Assertions.assertThat(generatedValue).isEqualTo("bbb");
    }

    @Test
    public void testPositiveLookaheadWithPrefixBeforeLookahead() {
        String javaRegex = "[z](?=[a-c]{2,4})[b]{3}";
        GeneREx generex = new GeneREx(javaRegex);
        String generatedValue = generex.random();
        String expectedValue = "zbbb";
        Assertions.assertThat(expectedValue).as("expected value").matches(javaRegex);
        Assertions.assertThat(generatedValue).as("generated value").matches(javaRegex);
        Assertions.assertThat(generatedValue).isEqualTo(expectedValue);
    }

    /**
     * result should internally be (?!someregex)someotherregex -> (someotherregex)&~(someregex)
     */
    @Test
    public void testNegativeLookahead() {
        String javaRegex = "(?![a]{2})[a]{1,2}";
        GeneREx generex = new GeneREx(javaRegex);
        String generatedValue = generex.random();
        Assertions.assertThat(generatedValue).as("generated value").matches(javaRegex);
        Assertions.assertThat(generatedValue).isEqualTo("a");
    }

    @Test
    public void testNegativeLookaheadWithPrefixBeforeLookahead() {
        String javaRegex = "[z](?![a]{2})[a]{1,2}";
        GeneREx generex = new GeneREx(javaRegex);
        String generatedValue = generex.random();
        String expectedValue = "za";
        Assertions.assertThat(expectedValue).as("expected value").matches(javaRegex);
        Assertions.assertThat(generatedValue).as("generated value").matches(javaRegex);
        Assertions.assertThat(generatedValue).isEqualTo(expectedValue);
    }
}
