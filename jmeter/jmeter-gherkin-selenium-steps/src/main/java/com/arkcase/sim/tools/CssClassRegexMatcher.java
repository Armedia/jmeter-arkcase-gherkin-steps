package com.arkcase.sim.tools;

import java.util.Objects;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.WebElement;

/**
 * <p>
 * This class helps match CSS Classes against a regular expression. This matcher will
 * {@link #test(String) test against a String} or {@link #test(WebElement) against a WebElement}. In
 * either case, the string to be compared is expected to be in the syntax of the HTML {@code class}
 * attribute (i.e. whitespace-separated words).
 * </p>
 * <p>
 * The matcher splits the string into words, discarding any and all whitespace, and then compares
 * each word against the {@link Pattern regular expression} with which the object was constructed,
 * returning the result.
 * </p>
 *
 * @author diego
 *
 */
public class CssClassRegexMatcher {

	private final Pattern pattern;

	/**
	 * <p>
	 * Construct a new instance with the given regular expression. Must be a non-blank regular
	 * expression. The regular expression may contain whitespaces or whitespace matchers, but these
	 * will be useless since for testing the classes string will be split by whitespace and each
	 * resulting term will be checked against the pattern, therefore none of the terms will contain
	 * whitespace.
	 * </p>
	 *
	 * @param regex
	 *            the regular expression to search for
	 */
	public CssClassRegexMatcher(String regex) {
		if (StringUtils.isBlank(regex)) {
			throw new IllegalArgumentException("Must use a non-blank regular expression");
		}
		this.pattern = Pattern.compile(regex);
	}

	/**
	 * <p>
	 * Returns the pattern this instance is using to match classes.
	 * </p>
	 * 
	 * @return the pattern this instance is using to match classes
	 */
	public Pattern getPattern() {
		return this.pattern;
	}

	/**
	 * <p>
	 * Compare each of the CSS classes within the given string (expected to be in the syntax of
	 * HTML's {@code class} attribute) against the pattern, returning {@code true} on the first
	 * match, or {@code false} if there are no matches.
	 *
	 * @param classes
	 *            the whitespace-separated list of classes to check
	 * @return {@code true} if any of the given classes matches the given regex, {@code false}
	 *         otherwise.
	 */
	public boolean test(String classes) {
		if (StringUtils.isBlank(classes)) { return false; }
		for (String c : classes.split("\\s+")) {
			if (this.pattern.matcher(c).matches()) { return true; }
		}
		return false;
	}

	/**
	 * <p>
	 * Will invoke {@link WebElement#getAttribute(String) element.getAttribute("class")} and submit
	 * the returned string to the alternative {@link #test(String)} method, returning its value.
	 * </p>
	 *
	 * @param element
	 *            the {@link WebElement} whose {@code class} attribute should be checked
	 * @return as per {@link #test(String)}
	 */
	public boolean test(WebElement element) {
		return test(Objects.requireNonNull(element, "Must provide a WebElement to test").getAttribute("class"));
	}
}