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
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;

public class ExpectedConditionTools {

	@SafeVarargs
	public static ExpectedCondition<WebElement> firstOf(ExpectedCondition<WebElement>... locators) {
		return (d) -> {
			for (ExpectedCondition<WebElement> c : locators) {
				WebElement e = c.apply(d);
				if (e != null) { return e; }
			}
			return null;
		};
	}

	/**
	 * <p>
	 * Returns an {@link ExpectedCondition} instance that can be used to wait until a new window is
	 * opened. It will take a snapshot of the current windows the {@link WebDriver} has open at the
	 * time of creation, and will return the list of any new windows added which are not in that set
	 * at the first moment a difference is detected. Removed windows will not be taken into account
	 * </p>
	 */
	public static ExpectedCondition<Set<String>> newWindowsOpened(Set<String> currentWindows) {
		final Set<String> original = Collections.unmodifiableSet(new HashSet<>(currentWindows));
		return (d) -> {
			Set<String> newWindows = d.getWindowHandles();
			newWindows.removeAll(original);
			if (newWindows.isEmpty()) { return null; }
			return new LinkedHashSet<>(newWindows);
		};
	}

	public static ExpectedCondition<String> newWindowOpened(Set<String> currentWindows) {
		final ExpectedCondition<Set<String>> c = ExpectedConditionTools.newWindowsOpened(currentWindows);
		return (d) -> {
			Set<String> s = c.apply(d);
			if ((s == null) || s.isEmpty()) { return null; }
			return s.iterator().next();
		};
	}

	public static ExpectedCondition<Set<String>> anyWindowIsClosed(Set<String> currentWindows) {
		final Set<String> masterCopy = Collections.unmodifiableSet(new HashSet<>(currentWindows));
		return (d) -> {
			Set<String> original = new LinkedHashSet<>(masterCopy);
			original.removeAll(d.getWindowHandles());
			if (original.isEmpty()) { return null; }
			return original;
		};
	}

	public static ExpectedCondition<Boolean> windowIsClosed(String window) {
		return (d) -> !d.getWindowHandles().contains(window);
	}

}