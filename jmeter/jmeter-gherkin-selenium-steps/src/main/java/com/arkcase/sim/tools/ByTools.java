/*******************************************************************************
 * #%L
 * Armedia JMeter Gherkin Plugin
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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.pagefactory.ByChained;

public class ByTools {

	// TODO: The method in TypeScript supports both a string and a regex...
	public static By cssContainingText(String cssMatcher, String text) {
		return new ByChained(By.cssSelector(cssMatcher), ByTools.containsText(text));
	}

	public static By containsText(final String text) {
		if (StringUtils.isEmpty(text)) {
			throw new IllegalArgumentException("Must provide a non-empty string to search for");
		}
		return new By() {
			@Override
			public List<WebElement> findElements(SearchContext context) {
				List<WebElement> matches = new LinkedList<>();
				context.findElements(By.cssSelector("*")).stream() //
					.filter((e) -> (e.getText().indexOf(text) >= 0)) //
					.forEach(matches::add) //
				;
				return matches;
			}

		};
	}

	public static By matchesRegex(final String text) {
		Objects.requireNonNull(text, "Must provide a valid regular expression");
		final Pattern p = Pattern.compile(text);
		return new By() {

			@Override
			public List<WebElement> findElements(SearchContext context) {
				List<WebElement> matches = new LinkedList<>();
				context.findElements(By.cssSelector("*")).stream()//
					.filter((e) -> p.matcher(e.getText()).matches()) //
					.forEach(matches::add) //
				;
				return matches;
			}

		};
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

}
