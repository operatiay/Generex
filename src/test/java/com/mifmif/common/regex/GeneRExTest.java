package com.mifmif.common.regex;

import java.util.Arrays;
import java.util.Collection;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * @author Myk Kolisnyk
 *
 */
@RunWith(Parameterized.class)
public class GeneRExTest {

	private String pattern;
	private GeneREx generex;
	private int expectedMatchedStringsSize;

	@Parameters(name = "Test get match: {0}")
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] { { "Sample multicharacter expression", "[A-B]{5,9}", 992 }, { "Sample expression", "[0-3]([a-c]|[e-g]{1,2})", 60 },
				{ "Number format", "\\d{3,4}", 11000 },
				// {"Any non-number","\\D{3,4}"},
				{ "Any word", "\\w{1,2}", 4032 }, { "Empty string", "", 1 },
		// {"Any non-word","\\W{1,2}"}
				});
	}

	public GeneRExTest(String description, String patternValue, int numberOfStrings) {
		this.pattern = patternValue;
		this.expectedMatchedStringsSize = numberOfStrings;
	}

	@Before
	public void setUp() throws Exception {
		generex = new GeneREx(pattern);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testMatchedStringsSizeShouldReturnExpectedValues() {
		long size = generex.matchedStringsSize();
		Assert.assertTrue(
				String.format("The matched strings size '%s' doesn't match the value '%s'", size, expectedMatchedStringsSize),
				expectedMatchedStringsSize == size);
	}

	@Test
	public void testGetMatchedFirstMatchShouldBeTheSameAsMatchWithZeroIndex() {
		String firstMatch = generex.getFirstMatch();
		String matchStringZeroIndex = generex.getMatchedString(0);
		Assert.assertTrue(String.format("The generated string '%s' doesn't match the pattern '%s'", firstMatch, pattern), firstMatch.matches(pattern));
		Assert.assertTrue(String.format("The generated string '%s' doesn't match the pattern '%s'", matchStringZeroIndex, pattern),
				matchStringZeroIndex.matches(pattern));
		Assert.assertEquals(firstMatch, matchStringZeroIndex);
	}

	@Test
	public void testIterateThroughAllMatchesShouldReturnConsistentResults() {
		long total = generex.matchedStringsSize();
		for (int count = 1; count < total; count++) {
			String matchStringZeroIndex = generex.getMatchedString(count);
			Assert.assertTrue(String.format("The generated string '%s' doesn't match the pattern '%s' at iteration #%d", matchStringZeroIndex, pattern, count),
					matchStringZeroIndex.matches(pattern));
		}
	}
}
