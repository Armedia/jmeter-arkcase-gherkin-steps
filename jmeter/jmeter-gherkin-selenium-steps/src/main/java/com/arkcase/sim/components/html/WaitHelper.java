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
package com.arkcase.sim.components.html;

import java.time.Duration;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;

import com.arkcase.sim.components.AngularHelper;

public class WaitHelper extends AngularHelper {

	private static final String DEFAULT_WAIT_FOR_ELEMENT_HAVE_TEXT_MESSAGE = "Element should have text";

	public WaitHelper(WebDriver browser) {
		super(browser);
	}

	public final WebElement waitForElementToHaveText(By by) {
		return waitForElementToHaveText(by, null, null, null);
	}

	public final WebElement waitForElementToHaveText(By by, Duration wait) {
		return waitForElementToHaveText(by, wait, null, null);
	}

	public final WebElement waitForElementToHaveText(By by, Duration wait, Duration pollTime) {
		return waitForElementToHaveText(by, wait, pollTime, null);
	}

	public final WebElement waitForElementToHaveText(By by, Duration wait, String message) {
		return waitForElementToHaveText(by, wait, null, message);
	}

	public final WebElement waitForElementToHaveText(By by, Duration wait, Duration pollTime, String message) {
		Objects.requireNonNull(by, "Must provide a selector for the element to wait for");
		final ExpectedCondition<WebElement> presence = ExpectedConditions.presenceOfElementLocated(by);
		final ExpectedCondition<WebElement> c = (d) -> {
			WebElement e = presence.apply(d);
			if (e == null) { return null; }
			if (StringUtils.isEmpty(e.getText())) { return null; }
			return e;
		};
		return waitUntil(c, wait, pollTime,
			getOrDefaultSupplier(message, WaitHelper.DEFAULT_WAIT_FOR_ELEMENT_HAVE_TEXT_MESSAGE));
	}

	public final Boolean waitForElementToBeOptionallyPresent(By by) {
		return waitForElementToBeOptionallyPresent(by, null, null, null);
	}

	public final Boolean waitForElementToBeOptionallyPresent(By by, Duration wait) {
		return waitForElementToBeOptionallyPresent(by, wait, null, null);
	}

	public final Boolean waitForElementToBeOptionallyPresent(By by, Duration wait, Duration pollTime) {
		return waitForElementToBeOptionallyPresent(by, wait, pollTime, null);
	}

	public final Boolean waitForElementToBeOptionallyPresent(By by, Duration wait, String message) {
		return waitForElementToBeOptionallyPresent(by, wait, null, message);
	}

	public final Boolean waitForElementToBeOptionallyPresent(By by, Duration wait, Duration pollTime, String message) {
		Objects.requireNonNull(by, "Must provide a selector for the element to wait for");
		WebElement element = null;
		try {
			element = waitUntil(ExpectedConditions.presenceOfElementLocated(by), wait, pollTime,
				nullableSupplier(message));
		} catch (TimeoutException e) {
			// Ignore it
		}
		return (element != null);
	}

	protected final Actions newActions() {
		return new Actions(this.browser);
	}
}
