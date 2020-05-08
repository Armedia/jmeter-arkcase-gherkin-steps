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
package com.arkcase.sim.components;

import java.lang.reflect.Proxy;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

import org.openqa.selenium.By;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebDriverHelper {

	public static final Supplier<String> NO_MESSAGE = null;

	public static final Duration DEFAULT_WAIT = Duration.ofMinutes(5);
	public static final Duration DEFAULT_POLL_FREQ = Duration.ofMillis(50);

	public static final Logger LOG = LoggerFactory.getLogger(WebDriverHelper.class);

	private static final ExpectedCondition<Boolean> ALWAYS_TRUE = (d) -> Boolean.TRUE;

	public static <T> Supplier<T> nullableSupplier(T value) {
		return (value != null ? () -> value : null);
	}

	public static <T> Supplier<T> getOrDefaultSupplier(T value, T def) {
		return () -> WebDriverHelper.getOrDefault(value, def);
	}

	public static <T> T getOrDefault(T value, T def) {
		return (value != null ? value : def);
	}

	public static Object runJavaScript(WebDriver driver, String script, Object... args) {
		return JavascriptExecutor.class.cast(driver).executeScript(script, args);
	}

	public static Object runAsyncJavaScript(WebDriver driver, String script, Object... args) {
		return JavascriptExecutor.class.cast(driver).executeAsyncScript(script, args);
	}

	private static final Class<?>[] WECLASS = {
		WebElement.class
	};
	private static final WebElement NULL_ELEMENT = WebElement.class.cast(
		Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), WebDriverHelper.WECLASS, (p, m, a) -> {
			throw new RuntimeException("Unexpected call");
		}));

	private static ExpectedCondition<WebElement> invisibilityOfElementLocated(By by) {
		return new ExpectedCondition<WebElement>() {
			@Override
			public WebElement apply(WebDriver driver) {
				try {
					// If the element is still there, then we
					if (driver.findElement(by).isDisplayed()) { return null; }
				} catch (NoSuchElementException | StaleElementReferenceException e) {
					// Element is gone, and thus no longer visible
				}
				return WebDriverHelper.NULL_ELEMENT;
			}
		};
	}

	public static ExpectedCondition<Boolean> booleanize(final ExpectedCondition<?> condition) {
		return new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver driver) {
				return (condition.apply(driver) != null);
			}
		};
	}

	private static ExpectedCondition<WebElement> elementSelectionStateToBe(By by, boolean selected) {
		return new ExpectedCondition<WebElement>() {
			@Override
			public WebElement apply(WebDriver driver) {
				try {
					WebElement element = driver.findElement(by);
					if (element.isSelected() != selected) { return null; }
					return element;
				} catch (StaleElementReferenceException e) {

				}
				return null;
			}
		};
	}

	public static enum WaitType {
		//
		PRESENT, //
		VISIBLE, //
		CLICKABLE, //
		ENABLED, //
		SELECTED, //
		UNSELECTED, //
		HIDDEN, //
		//
		;

	}

	public static ExpectedCondition<WebElement> renderCondition(By by, WaitType waitType) {
		Objects.requireNonNull(by, "Must provide a non-null element by");
		ExpectedCondition<WebElement> condition = null;
		switch (WebDriverHelper.getOrDefault(waitType, WaitType.PRESENT)) {
			case ENABLED:
			case CLICKABLE:
				condition = ExpectedConditions.elementToBeClickable(by);
				break;
			case HIDDEN:
				condition = WebDriverHelper.invisibilityOfElementLocated(by);
				break;
			case PRESENT:
				condition = ExpectedConditions.presenceOfElementLocated(by);
				break;
			case SELECTED:
				condition = WebDriverHelper.elementSelectionStateToBe(by, true);
				break;
			case UNSELECTED:
				condition = WebDriverHelper.elementSelectionStateToBe(by, false);
				break;
			case VISIBLE:
				condition = ExpectedConditions.visibilityOfElementLocated(by);
				break;
		}
		return condition;
	}

	public static ExpectedCondition<Boolean> renderCondition(WebElement element, WaitType waitType) {
		Objects.requireNonNull(element, "Must provide a non-null element");
		ExpectedCondition<Boolean> condition = null;
		switch (WebDriverHelper.getOrDefault(waitType, WaitType.PRESENT)) {
			case PRESENT:
				return WebDriverHelper.ALWAYS_TRUE;
			case ENABLED:
			case CLICKABLE:
				condition = WebDriverHelper.booleanize(ExpectedConditions.elementToBeClickable(element));
				break;
			case HIDDEN:
				condition = ExpectedConditions.invisibilityOf(element);
				break;
			case SELECTED:
				condition = ExpectedConditions.elementSelectionStateToBe(element, true);
				break;
			case UNSELECTED:
				condition = ExpectedConditions.elementSelectionStateToBe(element, false);
				break;
			case VISIBLE:
				condition = WebDriverHelper.booleanize(ExpectedConditions.visibilityOf(element));
				break;
		}
		return condition;
	}

	public abstract class AbstractWait {

		protected Duration duration = WebDriverHelper.DEFAULT_WAIT;
		protected Duration frequency = WebDriverHelper.DEFAULT_POLL_FREQ;
		protected Supplier<String> message = null;

		public AbstractWait duration(Duration duration) {
			this.duration = WebDriverHelper.getOrDefault(duration, WebDriverHelper.DEFAULT_WAIT);
			return this;
		}

		public final Duration duration() {
			return this.duration;
		}

		public AbstractWait frequency(Duration frequency) {
			this.frequency = WebDriverHelper.getOrDefault(frequency, WebDriverHelper.DEFAULT_POLL_FREQ);
			return this;
		}

		public final Duration frequency() {
			return this.frequency;
		}

		public AbstractWait message(String message) {
			return message(WebDriverHelper.nullableSupplier(message));
		}

		public final Supplier<String> message() {
			return this.message;
		}

		public AbstractWait message(Supplier<String> message) {
			this.message = message;
			return this;
		}

		protected <T> T until(ExpectedCondition<T> condition) {
			Objects.requireNonNull(condition, "Must provide a condition to wait for");
			Duration period = this.duration;
			if (period.isNegative()) {
				period = WebDriverHelper.DEFAULT_WAIT;
			}
			Duration frequency = this.frequency;
			if (frequency.isNegative() || frequency.isZero()) {
				frequency = WebDriverHelper.DEFAULT_POLL_FREQ;
			}

			FluentWait<WebDriver> waiter = new WebDriverWait(WebDriverHelper.this.browser, period.getSeconds(),
				frequency.toMillis());
			if (this.message != null) {
				waiter = waiter.withMessage(this.message);
			}
			return waiter.until(condition);
		}
	}

	public class WaitTool extends AbstractWait {
		@Override
		public WaitTool duration(Duration period) {
			super.duration(period);
			return this;
		}

		@Override
		public WaitTool frequency(Duration frequency) {
			super.frequency(frequency);
			return this;
		}

		@Override
		public WaitTool message(String message) {
			super.message(message);
			return this;
		}

		@Override
		public WaitTool message(Supplier<String> message) {
			super.message(message);
			return this;
		}

		@Override
		public <T> T until(ExpectedCondition<T> condition) {
			return super.until(condition);
		}
	}

	public class ElementWait extends AbstractWait {

		protected WebElement element = null;
		protected WaitType waitType = WaitType.PRESENT;

		public ElementWait element(WebElement element) {
			this.element = element;
			return this;
		}

		public WebElement element() {
			return this.element;
		}

		public ElementWait waitType(WaitType waitType) {
			this.waitType = WebDriverHelper.getOrDefault(waitType, WaitType.PRESENT);
			return this;
		}

		public WaitType waitType() {
			return this.waitType;
		}

		@Override
		public ElementWait duration(Duration period) {
			super.duration(period);
			return this;
		}

		@Override
		public ElementWait frequency(Duration frequency) {
			super.frequency(frequency);
			return this;
		}

		@Override
		public ElementWait message(String message) {
			super.message(message);
			return this;
		}

		@Override
		public ElementWait message(Supplier<String> message) {
			super.message(message);
			return this;
		}

		public boolean perform() {
			return until(WebDriverHelper.renderCondition(this.element, this.waitType));
		}
	}

	public class LocatorWait extends AbstractWait {

		protected By by = null;
		protected WaitType waitType = WaitType.PRESENT;

		public LocatorWait by(By by) {
			this.by = by;
			return this;
		}

		public By by() {
			return this.by;
		}

		public LocatorWait waitType(WaitType waitType) {
			this.waitType = WebDriverHelper.getOrDefault(waitType, WaitType.PRESENT);
			return this;
		}

		public WaitType waitType() {
			return this.waitType;
		}

		@Override
		public LocatorWait duration(Duration period) {
			super.duration(period);
			return this;
		}

		@Override
		public LocatorWait frequency(Duration frequency) {
			super.frequency(frequency);
			return this;
		}

		@Override
		public LocatorWait message(String message) {
			super.message(message);
			return this;
		}

		@Override
		public LocatorWait message(Supplier<String> message) {
			super.message(message);
			return this;
		}

		public WebElement perform() {
			return until(WebDriverHelper.renderCondition(this.by, this.waitType));
		}
	}

	protected final WebDriver browser;
	protected final RemoteWebDriver remoteBrowser;
	protected final Capabilities capabilities;

	public WebDriverHelper(WebDriver browser) {
		this.browser = Objects.requireNonNull(browser, "Must provide a WebDriver instance");
		if (RemoteWebDriver.class.isInstance(browser)) {
			this.remoteBrowser = RemoteWebDriver.class.cast(browser);
			this.capabilities = this.remoteBrowser.getCapabilities();
		} else {
			this.remoteBrowser = null;
			this.capabilities = null;
		}
	}

	public final WebDriver getBrowser() {
		return this.browser;
	}

	public final RemoteWebDriver getRemoteWebDriver() {
		return this.remoteBrowser;
	}

	public final Capabilities getCapabilities() {
		return this.capabilities;
	}

	public final String getBrowserName() {
		Capabilities cap = getCapabilities();
		return (cap != null ? cap.getBrowserName() : null);
	}

	public final Object runJavaScript(String script, Object... args) {
		return WebDriverHelper.runJavaScript(this.browser, script, args);
	}

	public final Object runAsyncJavaScript(String script, Object... args) {
		return WebDriverHelper.runAsyncJavaScript(this.browser, script, args);
	}

	public final String escapeChars(char c, String s) {
		String exp = String.format("\\Q%s\\E", c);
		String rep = String.format("\\%s", c);
		return s.replaceAll(exp, rep);
	}

	public final List<WebElement> findElements(By by) {
		return this.browser.findElements(by);
	}

	public final WebElement findElement(By by) {
		return this.browser.findElement(by);
	}

	public final <T> T waitUntil(ExpectedCondition<T> condition) {
		return waitUntil(condition, WebDriverHelper.NO_MESSAGE);
	}

	public final <T> T waitUntil(ExpectedCondition<T> condition, String message) {
		return waitUntil(condition, WebDriverHelper.nullableSupplier(message));
	}

	public final <T> T waitUntil(ExpectedCondition<T> condition, Supplier<String> message) {
		return waitUntil(condition, null, null, message);
	}

	public final <T> T waitUntil(ExpectedCondition<T> condition, Duration wait) {
		return waitUntil(condition, wait, WebDriverHelper.NO_MESSAGE);
	}

	public final <T> T waitUntil(ExpectedCondition<T> condition, Duration wait, String message) {
		return waitUntil(condition, wait, WebDriverHelper.nullableSupplier(message));
	}

	public final <T> T waitUntil(ExpectedCondition<T> condition, Duration wait, Supplier<String> message) {
		return waitUntil(condition, wait, null, message);
	}

	public final <T> T waitUntil(ExpectedCondition<T> condition, Duration wait, Duration pollTime) {
		return waitUntil(condition, wait, pollTime, WebDriverHelper.NO_MESSAGE);
	}

	public final <T> T waitUntil(ExpectedCondition<T> condition, Duration wait, Duration pollTime, String message) {
		return waitUntil(condition, wait, pollTime, WebDriverHelper.nullableSupplier(message));
	}

	public final <T> T waitUntil(ExpectedCondition<T> condition, Duration wait, Duration pollTime,
		Supplier<String> message) {
		return new WaitTool().duration(wait).frequency(pollTime).message(message).until(condition);
	}

	public final WebElement waitForElement(By by, WaitType waitType) {
		return waitForElement(by, waitType, WebDriverHelper.NO_MESSAGE);
	}

	public final WebElement waitForElement(By by, WaitType waitType, String message) {
		return waitForElement(by, waitType, WebDriverHelper.nullableSupplier(message));
	}

	public final WebElement waitForElement(By by, WaitType waitType, Supplier<String> message) {
		return waitForElement(by, waitType, null, null, message);
	}

	public final WebElement waitForElement(By by, WaitType waitType, Duration wait) {
		return waitForElement(by, waitType, wait, WebDriverHelper.NO_MESSAGE);
	}

	public final WebElement waitForElement(By by, WaitType waitType, Duration wait, String message) {
		return waitForElement(by, waitType, wait, WebDriverHelper.nullableSupplier(message));
	}

	public final WebElement waitForElement(By by, WaitType waitType, Duration wait, Supplier<String> message) {
		return waitForElement(by, waitType, wait, null, message);
	}

	public final WebElement waitForElement(By by, WaitType waitType, Duration wait, Duration pollTime) {
		return waitForElement(by, waitType, wait, pollTime, WebDriverHelper.NO_MESSAGE);
	}

	public final WebElement waitForElement(By by, WaitType waitType, Duration wait, Duration pollTime, String message) {
		return waitForElement(by, waitType, wait, pollTime, (message != null ? () -> message : null));
	}

	public final WebElement waitForElement(By by, WaitType waitType, Duration wait, Duration pollTime,
		Supplier<String> message) {
		WebElement ret = waitUntil(WebDriverHelper.renderCondition(by, waitType), wait, pollTime, message);
		return (ret != WebDriverHelper.NULL_ELEMENT ? ret : null);
	}

	public final boolean waitForElement(WebElement element, WaitType waitType) {
		return waitForElement(element, waitType, WebDriverHelper.NO_MESSAGE);
	}

	public final boolean waitForElement(WebElement element, WaitType waitType, String message) {
		return waitForElement(element, waitType, WebDriverHelper.nullableSupplier(message));
	}

	public final boolean waitForElement(WebElement element, WaitType waitType, Supplier<String> message) {
		return waitForElement(element, waitType, null, null, message);
	}

	public final boolean waitForElement(WebElement element, WaitType waitType, Duration wait) {
		return waitForElement(element, waitType, wait, WebDriverHelper.NO_MESSAGE);
	}

	public final boolean waitForElement(WebElement element, WaitType waitType, Duration wait, String message) {
		return waitForElement(element, waitType, wait, WebDriverHelper.nullableSupplier(message));
	}

	public final boolean waitForElement(WebElement element, WaitType waitType, Duration wait,
		Supplier<String> message) {
		return waitForElement(element, waitType, wait, null, message);
	}

	public final boolean waitForElement(WebElement element, WaitType waitType, Duration wait, Duration pollTime) {
		return waitForElement(element, waitType, wait, pollTime, WebDriverHelper.NO_MESSAGE);
	}

	public final boolean waitForElement(WebElement element, WaitType waitType, Duration wait, Duration pollTime,
		String message) {
		return waitForElement(element, waitType, wait, pollTime, (message != null ? () -> message : null));
	}

	public final boolean waitForElement(WebElement element, WaitType waitType, Duration wait, Duration pollTime,
		Supplier<String> message) {
		return waitUntil(WebDriverHelper.renderCondition(element, waitType), wait, pollTime, message);
	}
}