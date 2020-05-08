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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.LocalFileDetector;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;

import com.arkcase.sim.components.misc.JsHelper;
import com.google.common.collect.Comparators;

public class PageHelper extends WaitHelper {

	public static enum Timeout {
		//
		xxs(Duration.ofSeconds(1)), //
		xs(Duration.ofSeconds(2)), //
		s(Duration.ofSeconds(5)), //
		m(Duration.ofSeconds(10)), //
		l(Duration.ofSeconds(25)), //
		xl(Duration.ofSeconds(50)), //
		xxl(Duration.ofSeconds(75)), //
		xxxl(Duration.ofSeconds(200)), //
		xxxxl(Duration.ofSeconds(500)), //
		//
		;

		public final Duration duration;

		private Timeout(Duration duration) {
			this.duration = Objects.requireNonNull(duration, "Must provide a non-null duration");
			if (this.duration.isNegative() || this.duration.isZero()) {
				throw new IllegalArgumentException("Must provide a positive, non-empty duration");
			}
		}
	}

	protected static final Timeout DEFAULT_TIMEOUT = Timeout.xxl;

	public PageHelper(WebDriver browser) {
		super(browser);
	}

	public final boolean isFullScreen() {
		return Boolean.class.cast(runJavaScript("return (!window.screenTop && !window.screenY)"));
	}

	public final void actionKeyDown(String key) {
		newActions() //
			.keyDown(key) //
			.build() //
			.perform() //
		;
	}

	public final void executeInIframe(WebElement frame, Runnable fn) {
		this.browser.switchTo().frame(frame);
		fn.run();
		this.browser.switchTo().defaultContent();
		waitForAngular();
	}

	public final void executeInIframe(int index, Runnable fn) {
		this.browser.switchTo().frame(index);
		fn.run();
		this.browser.switchTo().defaultContent();
		waitForAngular();
	}

	public final void actionSendKeys(String key) {
		newActions() //
			.sendKeys(key) //
			.build() //
			.perform() //
		;
	}

	public final void sendKeysToInputField(By element, String key) {
		this.browser.findElement(element).sendKeys(key);
	}

	public final void actionKeyUp(String key) {
		newActions() //
			.keyUp(key) //
			.build() //
			.perform() //
		;
	}

	public final void keyPressForBrowser(String key) {
		newActions() //
			.sendKeys(key) //
			.build() //
			.perform() //
		;
	}

	public final void actionMouseUp(WebElement location) {
		newActions() //
			.moveToElement(location) //
			.release() //
			.build() //
			.perform() //
		;
	}

	// Known issue for chrome, direct maximize window doesn't work
	public final void maximizeWindow() {
		resizeWindow();
	}

	public final void resizeHorizontally(int height) {
		resizeWindow(-1, height);
	}

	public final void resizeVertically(int width) {
		resizeWindow(width, -1);
	}

	public final void resizeWindow() {
		resizeWindow(-1, -1);
	}

	public final void resizeWindow(int width) {
		resizeWindow(width, -1);
	}

	public final void resizeWindow(int width, int height) {
		Object maxWidth = runJavaScript("return window.screen.availWidth");
		Object maxHeight = runJavaScript("return window.screen.availHeight");

		int finalWidth = width;
		if (finalWidth < 0) {
			finalWidth = Number.class.cast(maxWidth).intValue();
		}

		int finalHeight = height;
		if (finalHeight < 0) {
			finalHeight = Number.class.cast(maxHeight).intValue();
		}

		this.browser.manage().window().setSize(new Dimension(finalWidth, finalHeight));
	}

	public final void setWindowSize(int width, int height) {
		resizeWindow(width, height);
	}

	public final Object executeScript(String script, Object... args) {
		return runJavaScript(script, args);
	}

	public final void switchToNewTabIfAvailable() {
		switchToNewTabIfAvailable(1);
	}

	public final void switchToNewTabIfAvailable(int windowNumber) {
		// TODO: WARNING - the iteration order may be undefined!!
		List<String> handles = new ArrayList<>(this.browser.getWindowHandles());
		String target = null;
		if (windowNumber < handles.size()) {
			target = handles.get(windowNumber);
		}

		if (target != null) {
			this.browser.switchTo().window(target);
		}

		// Avoiding bootstraping issue, Known issue
		// Error: Error while waiting for Protractor to sync with the page:
		// "window.angular is undefined. This could be either because this is a non-angular page or
		// because your test involves client-side navigation, which can interfere with Protractor's
		// bootstrapping.
		// See http://git.io/v4gXM for details

		// TODO: Uncomment this if the below fix doesn't work
		// browser.get(browser.getCurrentUrl());

		// TODO: Does this next line fix the above error?
		waitForAngular();
	}

	public final void switchToFirstTab() {
		// TODO: WARNING - the iteration order may be undefined!!
		List<String> handles = new ArrayList<>(this.browser.getWindowHandles());
		if (handles.size() > 1) {
			this.browser.close();
		}
		this.browser.switchTo().window(handles.get(0));
	}

	public final String getAttributeValue(By element, String attribute) {
		return StringUtils.trim(waitForElement(element, WaitType.VISIBLE).getAttribute(attribute));
	}

	public final void click(By element) {
		waitForElement(element, WaitType.CLICKABLE).click();
	}

	public final void clickIfPresent(By element) {
		try {
			this.browser.findElement(element).click();
		} catch (NoSuchElementException e) {
			// Do nothing
		}
	}

	public final void clickAndWaitForElementToHide(By element) {
		WebElement e = waitForElement(element, WaitType.CLICKABLE);
		e.click();
		waitForElement(element, WaitType.HIDDEN);
	}

	public final void clickAndWaitForElementToHide(WebElement element) {
		waitForElement(element, WaitType.CLICKABLE);
		element.click();
		waitForElement(element, WaitType.HIDDEN);
	}

	public final String getTextWithNoWait(By element) {
		return this.browser.findElement(element).getText();
	}

	public final String currentUrl() {
		return this.browser.getCurrentUrl();
	}

	public final String getPageTitle() {
		return this.browser.getTitle();
	}

	public final void refreshPage() {
		this.browser.navigate().refresh();
	}

	public final void switchToFrame(By element) {
		switchToFrame(this.browser.findElement(element));
	}

	public final void switchToFrame(WebElement element) {
		this.browser.switchTo().frame(element);
	}

	public final boolean isElementHidden(WebElement element) {
		return isElementHidden(element, true);
	}

	public final boolean isElementHidden(WebElement element, boolean wait) {
		if (wait) {
			try {
				return waitUntil(ExpectedConditions.visibilityOf(element)).isDisplayed();
			} catch (TimeoutException e) {
				return false;
			}
		}
		try {
			return !element.isDisplayed();
		} catch (NoSuchElementException e) {
			return true;
		}
	}

	public final boolean isElementHidden(By element) {
		return isElementHidden(element, true);
	}

	public final boolean isElementHidden(By element, boolean wait) {
		if (wait) {
			try {
				final ExpectedCondition<WebElement> presence = ExpectedConditions.presenceOfElementLocated(element);
				waitUntil((d) -> {
					WebElement e = presence.apply(d);
					return ((e == null) || !e.isDisplayed());
				});
			} catch (TimeoutException e) {
				return false;
			}
		}
		try {
			return !this.browser.findElement(element).isDisplayed();
		} catch (NoSuchElementException e) {
			return true;
		}
	}

	public final boolean isElementDisplayed(WebElement element) {
		return isElementDisplayed(element, true, PageHelper.DEFAULT_TIMEOUT);
	}

	public final boolean isElementDisplayed(WebElement element, boolean wait) {
		return isElementDisplayed(element, wait, PageHelper.DEFAULT_TIMEOUT);
	}

	public final boolean isElementDisplayed(WebElement element, boolean wait, Timeout timeout) {
		if (timeout == null) {
			timeout = PageHelper.DEFAULT_TIMEOUT;
		}
		if (wait) {
			try {
				waitForElement(element, WaitType.VISIBLE, timeout.duration);
			} catch (TimeoutException e) {
				return false;
			}
		}
		return element.isDisplayed();
	}

	public final boolean isElementDisplayed(By element) {
		return isElementDisplayed(element, true);
	}

	public final boolean isElementDisplayed(By element, boolean wait) {
		return isElementDisplayed(element, wait, PageHelper.DEFAULT_TIMEOUT);
	}

	public final boolean isElementDisplayed(By element, boolean wait, Timeout timeout) {
		if (timeout == null) {
			timeout = PageHelper.DEFAULT_TIMEOUT;
		}
		if (wait) {
			try {
				return waitForElement(element, WaitType.VISIBLE, timeout.duration).isDisplayed();
			} catch (TimeoutException e) {
				return false;
			}
		}
		try {
			return this.browser.findElement(element).isDisplayed();
		} catch (NoSuchElementException e) {
			return false;
		}
	}

	public final boolean isElementPresent(By element) {
		return isElementPresent(element, true);
	}

	public final boolean isElementPresent(By element, boolean wait) {
		return isElementPresent(element, wait, PageHelper.DEFAULT_TIMEOUT);
	}

	public final boolean isElementPresent(By element, boolean wait, Timeout timeout) {
		if (timeout == null) {
			timeout = PageHelper.DEFAULT_TIMEOUT;
		}

		if (wait) {
			try {
				waitForElement(element, WaitType.PRESENT, timeout.duration);
				return true;
			} catch (TimeoutException e) {
				return false;
			}
		}
		try {
			this.browser.findElement(element);
			return true;
		} catch (NoSuchElementException e) {
			return false;
		}
	}

	public final boolean isElementEnabled(WebElement element) {
		return isElementEnabled(element, true, PageHelper.DEFAULT_TIMEOUT);
	}

	public final boolean isElementEnabled(WebElement element, boolean wait) {
		return isElementEnabled(element, wait, PageHelper.DEFAULT_TIMEOUT);
	}

	public final boolean isElementEnabled(WebElement element, boolean wait, Timeout timeout) {
		if (timeout == null) {
			timeout = PageHelper.DEFAULT_TIMEOUT;
		}
		if (wait) {
			try {
				return waitForElement(element, WaitType.CLICKABLE, timeout.duration);
			} catch (TimeoutException e) {
				return false;
			}
		}
		return element.isEnabled();
	}

	public final boolean isElementEnabled(By element) {
		return isElementEnabled(element, true);
	}

	public final boolean isElementEnabled(By element, boolean wait) {
		return isElementEnabled(element, wait, PageHelper.DEFAULT_TIMEOUT);
	}

	public final boolean isElementEnabled(By element, boolean wait, Timeout timeout) {
		if (timeout == null) {
			timeout = PageHelper.DEFAULT_TIMEOUT;
		}
		if (wait) {
			try {
				return (waitForElement(element, WaitType.CLICKABLE, timeout.duration) != null);
			} catch (TimeoutException e) {
				return false;
			}
		}
		try {
			return this.browser.findElement(element).isEnabled();
		} catch (NoSuchElementException e) {
			return false;
		}
	}

	public final boolean isElementSelected(WebElement element) {
		return isElementSelected(element, true, PageHelper.DEFAULT_TIMEOUT);
	}

	public final boolean isElementSelected(WebElement element, boolean wait) {
		return isElementSelected(element, wait, PageHelper.DEFAULT_TIMEOUT);
	}

	public final boolean isElementSelected(WebElement element, boolean wait, Timeout timeout) {
		if (timeout == null) {
			timeout = PageHelper.DEFAULT_TIMEOUT;
		}
		if (wait) {
			try {
				return waitForElement(element, WaitType.SELECTED, timeout.duration);
			} catch (TimeoutException e) {
				return false;
			}
		}
		return element.isEnabled();
	}

	public final boolean isElementSelected(By element) {
		return isElementSelected(element, true);
	}

	public final boolean isElementSelected(By element, boolean wait) {
		return isElementSelected(element, wait, PageHelper.DEFAULT_TIMEOUT);
	}

	public final boolean isElementSelected(By element, boolean wait, Timeout timeout) {
		if (timeout == null) {
			timeout = PageHelper.DEFAULT_TIMEOUT;
		}
		if (wait) {
			try {
				return (waitForElement(element, WaitType.SELECTED, timeout.duration) != null);
			} catch (TimeoutException e) {
				return false;
			}
		}
		try {
			return this.browser.findElement(element).isSelected();
		} catch (NoSuchElementException e) {
			return false;
		}
	}

	public final <C extends Comparable<C>> boolean isListSorted(C[] sourceList, boolean isAscending) {
		// The list is out of order the moment one of the items is out of order with regards to
		// its predecessor
		final int o = (isAscending ? 1 : -1);
		return Comparators.isInOrder(Arrays.asList(sourceList), (a, b) -> {
			int ret = 0;
			if (a != b) {
				if (a == null) {
					ret = -1;
				} else if (b == null) {
					ret = 1;
				} else {
					ret = a.compareTo(b);
				}
			}
			return o * ret;
		});
	}

	public final String[] getAllTexts(By... elements) {
		List<String> ret = new ArrayList<>(elements.length);
		for (By by : elements) {
			waitUntil(ExpectedConditions.presenceOfAllElementsLocatedBy(by)).forEach((e) -> ret.add(e.getText()));
		}
		return ret.toArray(JsHelper.EMPTY_ARRAY);
	}

	public final String[] getAllTexts(WebElement... elements) {
		List<String> ret = new ArrayList<>(elements.length);
		for (WebElement e : elements) {
			ret.add(e.getText());
		}
		return ret.toArray(JsHelper.EMPTY_ARRAY);
	}

	public final String getText(By element) {
		final ExpectedCondition<WebElement> presence = ExpectedConditions.presenceOfElementLocated(element);
		return waitUntil((d) -> {
			WebElement e = presence.apply(d);
			if (e == null) { return null; }
			String text = StringUtils.trim(e.getText());
			return (StringUtils.isNotEmpty(text) ? text : null);
		});
	}

	public final String[] getAllTextsInArray(By... elements) {
		return JsHelper.trimArray(getAllTexts(elements));
	}

	public final void switchToiFrame(By frame) throws InterruptedException {
		switchToiFrame(frame, Timeout.xs);
	}

	public final void switchToiFrame(By frame, Timeout sleep) throws InterruptedException {
		waitForAngularEnabled(false);
		WebElement e = waitForElement(frame, WaitType.VISIBLE);
		Thread.sleep(TimeUnit.SECONDS.toMillis(sleep.duration.toMillis()));
		this.browser.switchTo().frame(e);
	}

	public final void switchToDefaultContent() {
		this.browser.switchTo().defaultContent();
		waitForAngularEnabled(true);
	}

	public final void acceptAlert() {
		this.browser.switchTo().alert().accept();
	}

	public final void closeAlertIfPresent() throws InterruptedException {
		try {
			Thread.sleep(Timeout.xs.duration.toMillis());
			acceptAlert();
			Thread.sleep(Timeout.xs.duration.toMillis());
		} catch (NoAlertPresentException e) {
			// Ignore that there's no alert
		}
	}

	public final String getUniqueId() {
		return UUID.randomUUID().toString().replaceAll("-", "");
	}

	public final String getUniqueIdForCategory(int length) {
		return RandomStringUtils.randomAlphanumeric(length);
	}

	public final String getUniqueIdWithAlphabetsOnly() {
		return RandomStringUtils.randomAlphabetic(16);
	}

	public final String getUniqueIntId(int size) {
		return String
			.valueOf(Math.floor(Math.pow(10, size - 1) + (JsHelper.RANDOM.nextDouble() * 9 * Math.pow(10, size - 1))));
	}

	public final void uploadFile(By element, String path) {
		getRemoteWebDriver().setFileDetector(new LocalFileDetector());
		waitForElement(element, WaitType.PRESENT).sendKeys(path);
	}

	public final String getAlertText() {
		return getAlertText(null, null);
	}

	public final String getAlertText(Timeout timeout) {
		return getAlertText(timeout, null);
	}

	public final String getAlertText(String message) {
		return getAlertText(null, message);
	}

	public final String getAlertText(Timeout timeout, String message) {
		return waitForAlertToBePresent(timeout, message).getText();
	}

	public final Alert waitForAlertToBePresent() {
		return waitForAlertToBePresent(null, null);
	}

	public final Alert waitForAlertToBePresent(Timeout timeout) {
		return waitForAlertToBePresent(timeout, null);
	}

	public final Alert waitForAlertToBePresent(String message) {
		return waitForAlertToBePresent(null, message);
	}

	public final Alert waitForAlertToBePresent(Timeout timeout, String message) {
		timeout = getOrDefault(timeout, PageHelper.DEFAULT_TIMEOUT);
		message = getOrDefault(message, "Alert is not present");
		return waitUntil(ExpectedConditions.alertIsPresent(), timeout.duration, message);
	}

	public final void sleepForXSec(int ms) throws InterruptedException {
		Thread.sleep(ms);
	}

	public final String randomString(int size) {
		return RandomStringUtils.randomAlphanumeric(size);
	}

	public final Number numberFromString(String str) {
		return Long.valueOf(str.replaceAll("\\D", ""));
	}

	public final String[] getAllTextsInLowerCase(By... elements) {
		String[] ret = getAllTexts(elements);
		for (int i = 0; i < ret.length; i++) {
			ret[i] = StringUtils.lowerCase(ret[i]);
		}
		return ret;
	}

	public final String replaceSpaceWithMinus(String text) {
		return text.replaceAll("\\s+", "-");
	}

	public final String getCssValue(By element, String attribute) {
		return StringUtils.trim(waitForElement(element, WaitType.VISIBLE).getCssValue(attribute));
	}

	public final String getCssValue(WebElement element, String attribute) {
		waitForElement(element, WaitType.VISIBLE);
		return StringUtils.trim(element.getCssValue(attribute));
	}
}
