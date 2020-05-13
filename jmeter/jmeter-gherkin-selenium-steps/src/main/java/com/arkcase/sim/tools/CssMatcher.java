package com.arkcase.sim.tools;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.WebElement;

public interface CssMatcher {

	public boolean test(String cssClass);

	public boolean test(WebElement element);

	/**
	 * <p>
	 * This class helps identify which CSS classes are present in a given HTML class string. This
	 * matcher will {@link #test(String) test against a String} or {@link #test(WebElement) against
	 * a WebElement}. In either case, the string to be compared is expected to be in the syntax of
	 * the HTML {@code class} attribute (i.e. whitespace-separated words).
	 * </p>
	 * <p>
	 * The matcher will join all the class names it was created with into a flexible regular
	 * expression which will then be used to match against the entire classes string. Each class
	 * name given will be searched for literally (i.e. it will be quoted in the pattern using
	 * {@link Pattern#quote(String)}). Empty or blank classes, or classes that contain whitespace
	 * will be ignored.
	 * </p>
	 *
	 * @author diego
	 *
	 */
	public class ClassName implements CssMatcher {

		private final Pattern pattern;
		private final Set<String> classes;

		/**
		 * <p>
		 * Construct a new instance that will seek to match the presence of any of the given
		 * classes. Classes will be searched for verbatim, quoted using
		 * {@link Pattern#quote(String)} (i.e. no regex sneakery allowed :D). Classes which are
		 * blank (i.e. either null or only whitespace) or contain any whitespace will be ignored,
		 * since these would never be matched in any meaningful way.
		 * </p>
		 *
		 * @param klasses
		 */
		public ClassName(String... klasses) {
			if (klasses.length < 1) {
				throw new IllegalArgumentException("Must provide at least one class to search for");
			}
			StringBuilder b = new StringBuilder();
			boolean first = true;
			Set<String> classes = new LinkedHashSet<>();
			for (String s : klasses) {
				if (StringUtils.isNotBlank(s) && !StringUtils.containsWhitespace(s)) {
					if (classes.add(s)) {
						if (!first) {
							b.append('|');
						}
						b.append(Pattern.quote(s));
					}
				}
				first = false;
			}
			if (classes.isEmpty()) {
				throw new IllegalArgumentException("Must include at least one non-blank CSS class to search for");
			}
			this.classes = Collections.unmodifiableSet(classes);
			this.pattern = Pattern.compile("\\b(" + b + ")\\b");
		}

		/**
		 * <p>
		 * Returns the read-only set of classes this instance will search for.
		 * </p>
		 *
		 * @return the read-only set of classes this instance will search for
		 */
		public Set<String> getClasses() {
			return this.classes;
		}

		/**
		 * <p>
		 * Check to see if the given string (expected to be in the syntax of HTML's {@code class}
		 * attribute) contains any of the classes which this object was constructed to match. Will
		 * return {@code true} if a match is found, or {@code false} otherwise.
		 *
		 * @param classes
		 *            the whitespace-separated list of classes to check
		 * @return {@code true} the given string contains any of the classes this instance has been
		 *         created to search for, {@code false} otherwise.
		 */
		@Override
		public boolean test(String classes) {
			return (!StringUtils.isBlank(classes) && this.pattern.matcher(classes).find());
		}

		/**
		 * <p>
		 * Will invoke {@link WebElement#getAttribute(String) element.getAttribute("class")} and
		 * submit the returned string to the alternative {@link #test(String)} method, returning its
		 * value.
		 * </p>
		 *
		 * @param element
		 *            the {@link WebElement} whose {@code class} attribute should be checked
		 * @return as per {@link #test(String)}
		 */
		@Override
		public boolean test(WebElement element) {
			return test(Objects.requireNonNull(element, "Must provide a WebElement to test").getAttribute("class"));
		}
	}

	/**
	 * <p>
	 * This class helps match CSS Classes against a regular expression. This matcher will
	 * {@link #test(String) test against a String} or {@link #test(WebElement) against a
	 * WebElement}. In either case, the string to be compared is expected to be in the syntax of the
	 * HTML {@code class} attribute (i.e. whitespace-separated words).
	 * </p>
	 * <p>
	 * The matcher splits the string into words, discarding any and all whitespace, and then
	 * compares each word against the {@link Pattern regular expression} with which the object was
	 * constructed, returning the result.
	 * </p>
	 *
	 * @author diego
	 *
	 */
	public static class Regex implements CssMatcher {

		private final Pattern pattern;

		/**
		 * <p>
		 * Construct a new instance with the given regular expression. Must be a non-blank regular
		 * expression. The regular expression may contain whitespaces or whitespace matchers, but
		 * these will be useless since for testing the classes string will be split by whitespace
		 * and each resulting term will be checked against the pattern, therefore none of the terms
		 * will contain whitespace.
		 * </p>
		 *
		 * @param regex
		 *            the regular expression to search for
		 */
		public Regex(String regex) {
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
		@Override
		public boolean test(String classes) {
			if (StringUtils.isBlank(classes)) { return false; }
			for (String c : classes.split("\\s+")) {
				if (this.pattern.matcher(c).matches()) { return true; }
			}
			return false;
		}

		/**
		 * <p>
		 * Will invoke {@link WebElement#getAttribute(String) element.getAttribute("class")} and
		 * submit the returned string to the alternative {@link #test(String)} method, returning its
		 * value.
		 * </p>
		 *
		 * @param element
		 *            the {@link WebElement} whose {@code class} attribute should be checked
		 * @return as per {@link #test(String)}
		 */
		@Override
		public boolean test(WebElement element) {
			return test(Objects.requireNonNull(element, "Must provide a WebElement to test").getAttribute("class"));
		}
	}
}