package com.mifmif.common.regex;

import org.assertj.core.api.Assertions;
import org.junit.Test;

public class JavaRegexToGeneRExTest {

    /**
     * result should internally be (?=someregex)someotherregex -> (someotherregex)&(someregex)
     */
    @Test
    public void testPositiveLookahead() {
        // given
        String javaRegex = "(?=[a-c]{2,4})[b]{3}";
        // when
        GeneREx generex = new GeneREx(javaRegex);
        String generatedValue = generex.random();
        // then
        Assertions.assertThat(generatedValue).as("generated value").matches(javaRegex);
        Assertions.assertThat(generatedValue).isEqualTo("bbb");
    }

    @Test
    public void testPositiveLookaheadWithPrefixBeforeLookahead() {
        // given
        String javaRegex = "[z](?=[a-c]{2,4})[b]{3}";
        String expectedValue = "zbbb";
        // when
        GeneREx generex = new GeneREx(javaRegex);
        String generatedValue = generex.random();
        // then
        Assertions.assertThat(expectedValue).as("expected value").matches(javaRegex);
        Assertions.assertThat(generatedValue).as("generated value").matches(javaRegex);
        Assertions.assertThat(generatedValue).isEqualTo(expectedValue);
    }

    /**
     * result should internally be (?!someregex)someotherregex -> (someotherregex)&~(someregex)
     */
    @Test
    public void testNegativeLookahead() {
        // given
        String javaRegex = "(?![a]{2})[a]{1,2}";
        // when
        GeneREx generex = new GeneREx(javaRegex);
        String generatedValue = generex.random();
        // then
        Assertions.assertThat(generatedValue).as("generated value").matches(javaRegex);
        Assertions.assertThat(generatedValue).isEqualTo("a");
    }

    @Test
    public void testNegativeLookaheadWithPrefixBeforeLookahead() {
        // given
        String javaRegex = "[z](?![a]{2})[a]{1,2}";
        String expectedValue = "za";
        // when
        GeneREx generex = new GeneREx(javaRegex);
        String generatedValue = generex.random();
        // then
        Assertions.assertThat(expectedValue).as("expected value").matches(javaRegex);
        Assertions.assertThat(generatedValue).as("generated value").matches(javaRegex);
        Assertions.assertThat(generatedValue).isEqualTo(expectedValue);
    }

    @Test
    public void testPositiveLookbehind() {
        // given
        String javaRegex = "[z][a]{1,2}(?<=za)";
        String expectedValue = "za";
        // when
        GeneREx geneREx = new GeneREx(javaRegex);
        String generatedValue = geneREx.random();
        // then
        Assertions.assertThat(expectedValue).as("expected value").matches(javaRegex);
        Assertions.assertThat(generatedValue).as("generated value").matches(javaRegex);
        Assertions.assertThat(generatedValue).isEqualTo(expectedValue);
    }

    @Test
    public void testNegativeLookbehind() {
        // given
        String javaRegex = "[z][a]{1,2}(?<!za)";
        String expectedValue = "zaa";
        // when
        GeneREx geneREx = new GeneREx(javaRegex);
        String generatedValue = geneREx.random();
        // then
        Assertions.assertThat(expectedValue).as("expected value").matches(javaRegex);
        Assertions.assertThat(generatedValue).as("generated value").matches(javaRegex);
        Assertions.assertThat(generatedValue).isEqualTo(expectedValue);
    }
}
