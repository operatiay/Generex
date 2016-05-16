/*
  * Copyright 2014 y.mifrah
 *

 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mifmif.common.regex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import com.mifmif.common.regex.utils.yield.Yielderable;
import dk.brics.automaton.Automaton;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.State;
import dk.brics.automaton.Transition;

/**
 * A Java utility class that helps generating string values that match a given
 * regular expression. It generates all values that are matched by the Regex, a
 * random value, or you can generate only a specific string based on its
 * lexicographical order.
 *
 * @author y.mifrah
 */
public class GeneREx implements Iterable<String> {

    /**
     * The predefined character classes supported by {@code GeneREx}.
     * <p>
     * An immutable map containing as keys the character classes and values the equivalent regular expression syntax.
     *
     * @see #createRegExp(String)
     */
    private static final Map<String, String> PREDEFINED_CHARACTER_CLASSES;

    static {
        Map<String, String> characterClasses = new HashMap<>();
        characterClasses.put("\\\\d", "[0-9]");
        characterClasses.put("\\\\D", "[^0-9]");
        characterClasses.put("\\\\s", "[ \t\n\f\r]");
        characterClasses.put("\\\\S", "[^ \t\n\f\r]");
        characterClasses.put("\\\\w", "[a-zA-Z_0-9]");
        characterClasses.put("\\\\W", "[^a-zA-Z_0-9]");
        PREDEFINED_CHARACTER_CLASSES = Collections.unmodifiableMap(characterClasses);
    }

    private Automaton automaton;
    private List<String> matchedStrings = new ArrayList<>();
    private Node rootNode;
    private boolean isTransactionNodeBuilt;
    private int matchedStringCounter = 0;


    public GeneREx(String regex) {
        RegExp regExp = createRegExp(regex);
        automaton = regExp.toAutomaton();
    }

    public GeneREx(Automaton automaton) {
        this.automaton = automaton;
    }

    /**
     * Creates a {@code RegExp} instance from the given regular expression.
     * <p>
     * Predefined character classes are replaced with equivalent regular expression syntax prior creating the instance.
     *
     * @param regex the regular expression used to build the {@code RegExp} instance
     * @return a {@code RegExp} instance for the given regular expression
     * @throws NullPointerException     if the given regular expression is {@code null}
     * @throws IllegalArgumentException if an error occurred while parsing the given regular expression
     * @throws StackOverflowError       if the regular expression has to many transitions
     * @see #PREDEFINED_CHARACTER_CLASSES
     * @see #isValidPattern(String)
     */
    private static RegExp createRegExp(String regex) {
        String finalRegex = regex;
        finalRegex = replaceCharacterClasses(finalRegex);
        //TODO handle other java specific regex features
        //TODO replace lookarounds with union (a & b)
        finalRegex = replacePositiveLookaheadFromRegex(finalRegex);
        finalRegex = replaceNegativeLookaheadFromRegex(finalRegex);
        return new RegExp(finalRegex, RegExp.ALL);
    }

    private static String replaceCharacterClasses(String finalRegex) {
        for (Entry<String, String> charClass : PREDEFINED_CHARACTER_CLASSES.entrySet()) {
            finalRegex = finalRegex.replaceAll(charClass.getKey(), charClass.getValue());
        }

        return finalRegex;
    }

    /**
     * lookahead needs to become a separate regex, which is to be combined with the rest
     * @param javaRegex
     * @return
     */
    private static String replacePositiveLookaheadFromRegex(final String javaRegex) {
        return replaceLookaheadFromRegex(javaRegex, "?=", "&");
    }

    private static String replaceNegativeLookaheadFromRegex(final String javaRegex) {
        return replaceLookaheadFromRegex(javaRegex, "?!", "&~");
    }

    private static String replaceLookaheadFromRegex(final String javaRegex, final String lookaheadKey, final String translationKey) {
        final String GROUP_PRE_LOOKAHEAD = "preLookahead";
        final String GROUP_LOOKAHEAD = "lookahead";
        final String GROUP_POST_LOOKAHEAD = "postLookahead";
        String result = "";
        String regexForLookahead = "(?<" + GROUP_PRE_LOOKAHEAD + ">.*?)" +
                "(\\(" + Pattern.quote(lookaheadKey) + "(?<" + GROUP_LOOKAHEAD + ">.*)\\))" +
                "(?<" + GROUP_POST_LOOKAHEAD + ">.*?)";
        if (!javaRegex.contains(lookaheadKey)) {
            result = javaRegex;
        } else {
            Pattern lookaheadPattern = Pattern.compile(regexForLookahead);
            Matcher matcher = lookaheadPattern.matcher(javaRegex);
            boolean matches = matcher.matches();
            if (! matches) {
                throw new IllegalStateException("This regex should have had a lookahead pattern");
            }
            String preLookaheadCapture = matcher.group(GROUP_PRE_LOOKAHEAD);
            String lookaheadCapture = matcher.group(GROUP_LOOKAHEAD);
            String postLookaheadCapture = matcher.group(GROUP_POST_LOOKAHEAD);

            String regexWithoutLookahead = preLookaheadCapture + postLookaheadCapture;
            if (regexWithoutLookahead.length() > 0) {
                result =  preLookaheadCapture + "((" + postLookaheadCapture + ")" +
                        translationKey + "(" + lookaheadCapture + "))";
            }
        }
        return result;
    }

    /**
     * @param indexOrder {@code (1<= indexOrder <=n)}
     * @return The matched string by the given pattern in the given it's order
     * in the sorted list of matched String.<br>
     * <code>indexOrder</code> between 1 and <code>n</code> where
     * <code>n</code> is the number of matched String.<br>
     * If {@code indexOrder >= n}, return an empty string. if there is an
     * infinite number of String that matches the given Regex, the
     * method throws {@code StackOverflowError}
     */
    public String getMatchedString(int indexOrder) {
        buildRootNode();
        if (indexOrder == 0)
            indexOrder = 1;
        String result = buildStringFromNode(rootNode, indexOrder);
        result = result.substring(1, result.length() - 1);
        return result;
    }

    private String buildStringFromNode(Node node, int indexOrder) {
        String result = "";
        long passedStringNbr = 0;
        long step = node.getNbrMatchedString() / node.getNbrChar();
        for (char usedChar = node.getMinChar(); usedChar <= node.getMaxChar(); ++usedChar) {
            passedStringNbr += step;
            if (passedStringNbr >= indexOrder) {
                passedStringNbr -= step;
                indexOrder -= passedStringNbr;
                result = result.concat("" + usedChar);
                break;
            }
        }
        long passedStringNbrInChildNode = 0;
        if (result.length() == 0)
            passedStringNbrInChildNode = passedStringNbr;
        for (Node childN : node.getNextNodes()) {
            passedStringNbrInChildNode += childN.getNbrMatchedString();
            if (passedStringNbrInChildNode >= indexOrder) {
                passedStringNbrInChildNode -= childN.getNbrMatchedString();
                indexOrder -= passedStringNbrInChildNode;
                result = result.concat(buildStringFromNode(childN, indexOrder));
                break;
            }
        }
        return result;
    }

    /**
     * Tells whether or not the given pattern (or {@code Automaton}) is infinite, that is, generates an infinite number of
     * strings.
     * <p>
     * For example, the pattern "a+" generates an infinite number of strings whether "a{5}" does not.
     *
     * @return {@code true} if the pattern (or {@code Automaton}) generates an infinite number of strings, {@code false}
     * otherwise
     */
    public boolean isInfinite() {
        return !automaton.isFinite();
    }

    /**
     * @return first string in lexicographical order that is matched by the
     * given pattern.
     */
    public String getFirstMatch() {
        buildRootNode();
        Node node = rootNode;
        String result = "";
        while (node.getNextNodes().size() > 0) {
            result = result.concat("" + node.getMinChar());
            node = node.getNextNodes().get(0);
        }
        result = result.substring(1);
        return result;
    }

    /**
     * @return the number of strings that are matched by the given pattern.
     * @throws StackOverflowError if the given pattern generates a large, possibly infinite, number of strings.
     */
    public long matchedStringsSize() {
        buildRootNode();
        return rootNode.getNbrMatchedString();
    }

    /**
     * Prepare the rootNode and it's child nodes so that we can get
     * matchedString by index
     */
    private void buildRootNode() {
        if (isTransactionNodeBuilt) {
            return;
        }
        isTransactionNodeBuilt = true;
        rootNode = new Node();
        rootNode.setNbrChar(1);
        List<Node> nextNodes = prepareTransactionNodes(automaton.getInitialState());
        rootNode.setNextNodes(nextNodes);
        rootNode.updateNbrMatchedString();
    }


    private void generate(String strMatch, State state, int limit) {
        if (matchedStringCounter == limit) {
            return;
        }
        ++matchedStringCounter;
        List<Transition> transitions = state.getSortedTransitions(true);
        if (transitions.size() == 0) {
            matchedStrings.add(strMatch);
            return;
        }
        if (state.isAccept()) {
            matchedStrings.add(strMatch);
        }
        for (Transition transition : transitions) {
            for (char c = transition.getMin(); c <= transition.getMax(); ++c) {
                generate(strMatch + c, transition.getDest(), limit);
            }
        }
    }

    private Yielderable<String> getIterable(String strMatch, State state, int limit) {
        Yielderable<String> yielderable = yield -> {
            int i = 0;
            while(i <= limit) {
                yield.returning(null); //TODO here comes the actual new value
            }
        };
        return yielderable;
    }

    /**
     * Build list of nodes that present possible transactions from the
     * {@code state}.
     *
     * @param state origin
     * @return list of nodes representing possible transitions from {@code state}
     */
    private List<Node> prepareTransactionNodes(State state) {

        List<Node> transactionNodes = new ArrayList<Node>();
        if (preparedTransactionNode == Integer.MAX_VALUE / 2)
            return transactionNodes;
        ++preparedTransactionNode;

        if (state.isAccept()) {
            Node acceptedNode = new Node();
            acceptedNode.setNbrChar(1);
            transactionNodes.add(acceptedNode);
        }
        List<Transition> transitions = state.getSortedTransitions(true);
        for (Transition transition : transitions) {
            Node trsNode = new Node();
            int nbrChar = transition.getMax() - transition.getMin() + 1;
            trsNode.setNbrChar(nbrChar);
            trsNode.setMaxChar(transition.getMax());
            trsNode.setMinChar(transition.getMin());
            List<Node> nextNodes = prepareTransactionNodes(transition.getDest());
            trsNode.setNextNodes(nextNodes);
            transactionNodes.add(trsNode);
        }
        return transactionNodes;
    }

    private int preparedTransactionNode;

    /**
     * Generate all Strings that matches the given Regex.
     *
     * @return all strings matching the given regex.
     */
    public List<String> getAllMatchedStrings() {
        matchedStrings = new ArrayList<String>();
        generate("", automaton.getInitialState(), Integer.MAX_VALUE);
        return matchedStrings;

    }

    /**
     * Generate subList with a size of <code>limit</code> of Strings that
     * matches the given Regex. the Strings are ordered in lexicographical
     * order.
     *
     * @param limit don't generate more matches
     * @return list of matches, limited by {@code limit}.
     */
    public List<String> getMatchedStrings(int limit) {
        matchedStrings = new ArrayList<String>();
        generate("", automaton.getInitialState(), limit);
        return matchedStrings;

    }

    /**
     * Generate and return a random String that match the pattern used in this
     * GeneREx.
     *
     * @return a match for the pattern (randomly picked).
     */
    public String random() {
        return prepareRandom("", automaton.getInitialState(), 1, Integer.MAX_VALUE);
    }

    /**
     * Generate and return a random String that match the pattern used in this
     * GeneREx, and the string has a {@code length >= minLength}
     *
     * @param minLength minimal length of the string to be generated
     * @return string matching the pattern and having at least the length {@code minLength}.
     */
    public String random(int minLength) {
        return prepareRandom("", automaton.getInitialState(), minLength, Integer.MAX_VALUE);
    }

    /**
     * Generate and return a random String that match the pattern used in this
     * GeneREx, and the string has a {@code length >= minLength <= length <= maxLength}
     *
     * @param minLength minimal length restriction
     * @param maxLength maximal length restriction
     * @return pattern matching string of length between {@code minLength} and {@code maxLength}
     */
    public String random(int minLength, int maxLength) {
        return prepareRandom("", automaton.getInitialState(), minLength, maxLength);
    }

    private String prepareRandom(String strMatch, State state, int minLength, int maxLength) {
        List<Transition> transitions = state.getSortedTransitions(false);

        if (state.isAccept()) {
            if (strMatch.length() == maxLength) {
                return strMatch;
            }
            if (Math.random() > 0.7 && strMatch.length() >= minLength) {
                return strMatch;
            }
        }
        if (transitions.size() == 0) {
            return strMatch;
        }
        Random random = new Random();
        Transition randomTransition = transitions.get(random.nextInt(transitions.size()));
        int diff = randomTransition.getMax() - randomTransition.getMin() + 1;
        int randomOffset = diff;
        if (diff > 0) {
            randomOffset = random.nextInt(diff);
        }
        char randomChar = (char) (randomOffset + randomTransition.getMin());
        return prepareRandom(strMatch + randomChar, randomTransition.getDest(), minLength, maxLength);

    }

    public Iterator<String> iterator() {
        return new GenerexIterator(automaton.getInitialState());
    }

    /**
     * Tells whether or not the given regular expression is a valid pattern (for {@code GeneREx}).
     *
     * @param regex the regular expression that will be validated
     * @return {@code true} if the regular expression is valid, {@code false} otherwise
     * @throws NullPointerException if the given regular expression is {@code null}
     */
    public static boolean isValidPattern(String regex) {
        try {
            createRegExp(regex);
            return true;
        } catch (StackOverflowError | IllegalArgumentException ignore) { // NOPMD - Possibly valid but stack not big enough to handle it.
        }
        return false;
    }
}
