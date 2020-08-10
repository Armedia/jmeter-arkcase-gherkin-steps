/*******************************************************************************
 * #%L
 * Armedia ArkCase
 * %%
 * Copyright (C) 2020 Armedia, LLC
 * %%
 * This file is part of the ArkCase software.
 *
 * If the software was purchased under a paid ArkCase license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 *
 * ArkCase is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ArkCase is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with ArkCase. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 *******************************************************************************/
package com.arkcase.sim.tools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.function.ToIntBiFunction;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;

public class ByTools {

	public static By cssMatching(String cssSelector, Predicate<WebElement> predicate) {
		return ByTools.withPredicate(By.cssSelector(cssSelector), predicate);
	}

	public static By withPredicate(By selector, Predicate<WebElement> predicate) {
		Objects.requireNonNull(selector, "Must provide a By selector");
		Objects.requireNonNull(predicate, "Must provide a Predicate");
		return new By() {
			@Override
			public List<WebElement> findElements(SearchContext context) {
				List<WebElement> matches = new LinkedList<>();
				context.findElements(selector).stream() //
					.filter(predicate) //
					.forEach(matches::add) //
				;
				return matches;
			}
		};
	}

	public static By withPredicate(Predicate<WebElement> predicate) {
		return ByTools.withPredicate(By.cssSelector("*"), predicate);
	}

	public static By textMatches(Predicate<String> matcher) {
		Objects.requireNonNull(matcher, "Must provide a non-null predicate to apply");
		return ByTools.withPredicate((webElement) -> matcher.test(webElement.getText()));
	}

	public static By containsText(final String text) {
		return ByTools.containsText(text, false);
	}

	public static By containsText(final String text, boolean ignoreCase) {
		if (StringUtils.isEmpty(text)) { throw new IllegalArgumentException("Must provide a non-empty string"); }
		final ToIntBiFunction<String, String> indexOf = (ignoreCase ? StringUtils::indexOfIgnoreCase
			: StringUtils::indexOf);
		return ByTools.textMatches((elementText) -> indexOf.applyAsInt(elementText, text) >= 0);
	}

	public static By isText(final String text) {
		return ByTools.isText(text, false);
	}

	public static By isText(final String text, boolean ignoreCase) {
		if (text == null) { throw new IllegalArgumentException("Must provide a non-null string"); }
		final String expected = (StringUtils.isNotEmpty(text) ? text : StringUtils.EMPTY);
		final BiPredicate<String, String> equals = (ignoreCase ? StringUtils::equalsIgnoreCase : StringUtils::equals);
		return ByTools.textMatches((elementText) -> equals.test(elementText, expected));
	}

	public static By startsWith(final String text) {
		return ByTools.startsWith(text, false);
	}

	public static By startsWith(final String text, boolean ignoreCase) {
		if (StringUtils.isEmpty(text)) { throw new IllegalArgumentException("Must provide a non-empty string"); }
		final BiPredicate<String, String> startsWith = (ignoreCase ? StringUtils::startsWithIgnoreCase
			: StringUtils::startsWith);
		return ByTools.textMatches((elementText) -> startsWith.test(elementText, text));
	}

	public static By endsWith(final String text) {
		return ByTools.endsWith(text, false);
	}

	public static By endsWith(final String text, boolean ignoreCase) {
		if (StringUtils.isEmpty(text)) { throw new IllegalArgumentException("Must provide a non-empty string"); }
		final BiPredicate<String, String> endsWith = (ignoreCase ? StringUtils::endsWithIgnoreCase
			: StringUtils::endsWith);
		return ByTools.textMatches((elementText) -> endsWith.test(elementText, text));
	}

	public static By matchesRegex(final String regEx) {
		Objects.requireNonNull(regEx, "Must provide a valid regular expression");
		final Pattern p = Pattern.compile(regEx);
		return ByTools.textMatches((elementText) -> p.matcher(elementText).matches());
	}

	private static final String[] NG_PREFIXES = {
		"ng-", "ng_", "data-ng-", "x-ng-", "ng\\:"
	};

	private static final String NG_MODEL_CSS_TEMPLATE = "[${0}model=\"${1}\"]";

	public static By ngModel(final String model) {
		if (StringUtils.isEmpty(model)) {
			throw new IllegalArgumentException("Must provide a non-empty model name to search for");
		}
		return new By() {
			@Override
			public List<WebElement> findElements(SearchContext context) {
				final String template = TextTools.interpolate(ByTools.NG_MODEL_CSS_TEMPLATE, null, model);
				for (String prefix : ByTools.NG_PREFIXES) {
					By by = By.cssSelector(TextTools.interpolate(template, prefix));
					List<WebElement> matches = by.findElements(context);
					if (!matches.isEmpty()) { return matches; }
				}
				return Collections.emptyList();
			}
		};
	}

	/**
	 * <p>
	 * Will succeed when at least one element is found for each of the search conditions given.
	 * </p>
	 */
	public static By byOneEach(By... locators) {
		Objects.requireNonNull(locators, "Must provide a non-null array of By instances");
		final List<By> finalLocators = Arrays.asList(locators);
		finalLocators.removeIf(Objects::isNull);
		if (finalLocators.isEmpty()) {
			throw new IllegalArgumentException("Must provide at least one non-null By instance");
		}
		return new By() {
			@Override
			public List<WebElement> findElements(SearchContext context) {
				List<WebElement> ret = null;
				int matches = 0;
				for (By by : finalLocators) {
					List<WebElement> l = by.findElements(context);
					if (!l.isEmpty()) {
						if (ret == null) {
							ret = new ArrayList<>();
						}
						ret.addAll(l);
						matches++;
					}
				}
				return (matches == finalLocators.size() ? ret : Collections.emptyList());
			}
		};
	}

	/**
	 * <p>
	 * Will succeed when any of the given locators returns at least one element.
	 * </p>
	 */
	public static By firstOf(By... locators) {
		Objects.requireNonNull(locators, "Must provide a non-null array of By instances");
		final List<By> finalLocators = Arrays.asList(locators);
		finalLocators.removeIf(Objects::isNull);
		if (finalLocators.isEmpty()) {
			throw new IllegalArgumentException("Must provide at least one non-null By instance");
		}
		return new By() {
			@Override
			public List<WebElement> findElements(SearchContext context) {
				for (By by : finalLocators) {
					List<WebElement> l = by.findElements(context);
					if (!l.isEmpty()) { return l; }
				}
				return Collections.emptyList();
			}
		};
	}
}