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
package com.arkcase.sim.components;

import java.lang.reflect.Proxy;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import org.openqa.selenium.By;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.interactions.MoveTargetOutOfBoundsException;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebDriverHelper {

	private static final String SCROLL_SCRIPT = "arguments[0].scrollIntoView(true);";

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
					// If the target is still there, then we
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
		Objects.requireNonNull(by, "Must provide a non-null target by");
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
		Objects.requireNonNull(element, "Must provide a non-null target");
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
		protected Supplier<String> errorMessage = null;

		public AbstractWait duration(Duration duration) {
			this.duration = WebDriverHelper.getOrDefault(duration, WebDriverHelper.DEFAULT_WAIT);
			return this;
		}

		public final Duration duration() {
			return this.duration;
		}

		public AbstractWait pollFrequency(Duration frequency) {
			this.frequency = WebDriverHelper.getOrDefault(frequency, WebDriverHelper.DEFAULT_POLL_FREQ);
			return this;
		}

		public final Duration pollFrequency() {
			return this.frequency;
		}

		public AbstractWait errorMessage(String errorMessage) {
			return errorMessage(WebDriverHelper.nullableSupplier(errorMessage));
		}

		public final Supplier<String> errorMessage() {
			return this.errorMessage;
		}

		public AbstractWait errorMessage(Supplier<String> errorMessage) {
			this.errorMessage = errorMessage;
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
			if (this.errorMessage != null) {
				waiter = waiter.withMessage(this.errorMessage);
			}
			return waiter.until(condition);
		}
	}

	public class ConditionWait extends AbstractWait {
		@Override
		public ConditionWait duration(Duration period) {
			super.duration(period);
			return this;
		}

		@Override
		public ConditionWait pollFrequency(Duration frequency) {
			super.pollFrequency(frequency);
			return this;
		}

		@Override
		public ConditionWait errorMessage(String message) {
			super.errorMessage(message);
			return this;
		}

		@Override
		public ConditionWait errorMessage(Supplier<String> message) {
			super.errorMessage(message);
			return this;
		}

		@Override
		public final <T> T until(ExpectedCondition<T> condition) {
			return super.until(condition);
		}
	}

	public class TargettedWait<T, R> extends AbstractWait {
		protected T target = null;
		protected WaitType waitType = WaitType.PRESENT;
		private final BiFunction<T, WaitType, ExpectedCondition<R>> conditionRenderer;

		protected TargettedWait(BiFunction<T, WaitType, ExpectedCondition<R>> conditionRenderer) {
			this.conditionRenderer = Objects.requireNonNull(conditionRenderer);
		}

		public TargettedWait<T, R> target(T target) {
			this.target = target;
			return this;
		}

		public T target() {
			return this.target;
		}

		public TargettedWait<T, R> waitType(WaitType waitType) {
			this.waitType = WebDriverHelper.getOrDefault(waitType, WaitType.PRESENT);
			return this;
		}

		public final WaitType waitType() {
			return this.waitType;
		}

		@Override
		public TargettedWait<T, R> duration(Duration period) {
			super.duration(period);
			return this;
		}

		@Override
		public TargettedWait<T, R> pollFrequency(Duration frequency) {
			super.pollFrequency(frequency);
			return this;
		}

		@Override
		public TargettedWait<T, R> errorMessage(String message) {
			super.errorMessage(message);
			return this;
		}

		@Override
		public TargettedWait<T, R> errorMessage(Supplier<String> message) {
			super.errorMessage(message);
			return this;
		}

		public final R perform() {
			return until(this.conditionRenderer.apply(this.target, this.waitType));
		}
	}

	public class WebElementWait extends TargettedWait<WebElement, Boolean> {

		public WebElementWait() {
			super(WebDriverHelper::renderCondition);
		}

		@Override
		public WebElementWait target(WebElement element) {
			super.target(element);
			return this;
		}

		@Override
		public WebElementWait waitType(WaitType waitType) {
			this.waitType = WebDriverHelper.getOrDefault(waitType, WaitType.PRESENT);
			return this;
		}

		@Override
		public WebElementWait duration(Duration period) {
			super.duration(period);
			return this;
		}

		@Override
		public WebElementWait pollFrequency(Duration frequency) {
			super.pollFrequency(frequency);
			return this;
		}

		@Override
		public WebElementWait errorMessage(String errorMessage) {
			super.errorMessage(errorMessage);
			return this;
		}

		@Override
		public WebElementWait errorMessage(Supplier<String> errorMessage) {
			super.errorMessage(errorMessage);
			return this;
		}
	}

	public class LocatorWait extends TargettedWait<By, WebElement> {

		public LocatorWait() {
			super(WebDriverHelper::renderCondition);
		}

		@Override
		public LocatorWait target(By element) {
			super.target(element);
			return this;
		}

		@Override
		public LocatorWait waitType(WaitType waitType) {
			this.waitType = WebDriverHelper.getOrDefault(waitType, WaitType.PRESENT);
			return this;
		}

		@Override
		public LocatorWait duration(Duration period) {
			super.duration(period);
			return this;
		}

		@Override
		public LocatorWait pollFrequency(Duration frequency) {
			super.pollFrequency(frequency);
			return this;
		}

		@Override
		public LocatorWait errorMessage(String errorMessage) {
			super.errorMessage(errorMessage);
			return this;
		}

		@Override
		public LocatorWait errorMessage(Supplier<String> errorMessage) {
			super.errorMessage(errorMessage);
			return this;
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
		return new ConditionWait() //
			.duration(wait) //
			.pollFrequency(pollTime) //
			.errorMessage(message) //
			.until(condition) //
		;
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
		WebElement ret = new LocatorWait() //
			.target(by) //
			.waitType(waitType) //
			.duration(wait) //
			.pollFrequency(pollTime) //
			.errorMessage(message) //
			.perform() //
		;
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
		return new WebElementWait() //
			.target(element) //
			.waitType(waitType) //
			.duration(wait) //
			.pollFrequency(pollTime) //
			.errorMessage(message) //
			.perform() //
		;
	}

	protected final Actions newActions() {
		return new Actions(this.browser);
	}

	public final void scrollTo(WebElement element) {
		try {
			newActions().moveToElement(element).perform();
		} catch (MoveTargetOutOfBoundsException e) {
			runJavaScript(WebDriverHelper.SCROLL_SCRIPT, element);
		}
	}
}