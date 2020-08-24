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
package com.arkcase.sim.gherkin.steps;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.HasCapabilities;
import org.openqa.selenium.Point;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.html5.WebStorage;

import com.arkcase.sim.components.AngularHelper;
import com.arkcase.sim.components.WebDriverHelper;
import com.arkcase.sim.components.html.WaitHelper;
import com.armedia.commons.jmeter.gherkin.GherkinContext;

public class WebDriverClient {

	// private static final Point ORIGIN = new Point(0, 0);
	private static final String FETCH_DISPLAY = "return { " + //
		"screen: window.screen, " + //
		"innerWidth : window.innerWidth, " + //
		"innerHeight : window.innerHeight, " + //
		"outerWidth : window.outerWidth, " + //
		"outerHeight : window.outerHeight, " + //
		"x : window.screenX, " + //
		"y : window.screenY " + //
		"}";

	public class ScreenInfo {

		/** The Browser window's position within the entire screen space */
		public final Point position;

		/** The Browser window's relative position within its current screen */
		public final Point relativePosition;

		/** The Browser window's internal dimensions */
		public final Dimension inner;

		/** The Browser window's external dimensions */
		public final Dimension outer;

		/** The Browser's current screen's total dimensions */
		public final Dimension screen;

		/**
		 * Specifies the available on-screen rectangle for the application with minumums (x and y)
		 * and maximums (width + height), such that @{code x + width == screen.width}, and
		 * {@code y + height == screen.height}.
		 */
		public final Point minimum;
		public final Dimension available;

		public ScreenInfo() {
			Object ret = getWaitHelper().runJavaScript(WebDriverClient.FETCH_DISPLAY);

			Map<?, ?> info = Map.class.cast(ret);
			Number x = Number.class.cast(info.get("x"));
			Number y = Number.class.cast(info.get("y"));
			this.position = new Point(x.intValue(), y.intValue());

			Number width = Number.class.cast(info.get("innerWidth"));
			Number height = Number.class.cast(info.get("innerHeight"));
			this.inner = new Dimension(width.intValue(), height.intValue());

			width = Number.class.cast(info.get("outerWidth"));
			height = Number.class.cast(info.get("outerHeight"));
			this.outer = new Dimension(width.intValue(), height.intValue());

			Map<?, ?> screen = Map.class.cast(info.get("screen"));
			width = Number.class.cast(screen.get("width"));
			height = Number.class.cast(screen.get("height"));
			this.screen = new Dimension(width.intValue(), height.intValue());

			x = Number.class.cast(screen.get("availLeft"));
			y = Number.class.cast(screen.get("availTop"));
			this.minimum = new Point(x.intValue(), y.intValue());

			width = Number.class.cast(screen.get("availWidth"));
			height = Number.class.cast(screen.get("availHeight"));
			this.available = new Dimension(height.intValue(), width.intValue());

			this.relativePosition = new Point(this.position.x - this.minimum.x, this.position.y - this.minimum.y);
		}

		public boolean isFullscreen() {
			// A browser is fullscreen if its outer dimensions match the screen's total dimensions,
			// and its inner dimensions match its outer dimensions. The problem here is with
			// automation tools that draw a banner which eats into those inner dimensions
			// and thus causes a discrepancy.
			// TODO: account for discrepancies from automated tools
			return this.inner.equals(this.outer) && this.outer.equals(this.screen);
		}
	}

	private <T> T getBrowserAs(boolean failIfMissing, Class<T> clazz) {
		GherkinContext ctx = GherkinContext.get();
		T t = ctx.getEnv().getAs("browser", clazz);
		if (failIfMissing && (t == null)) {
			throw new NullPointerException(
				String.format("No %s instance was made available to the Gherkin context", clazz.getSimpleName()));
		}
		return t;
	}

	protected final WebDriver getBrowser() {
		return getBrowser(true);
	}

	protected final WebDriver getBrowser(boolean failIfMissing) {
		return getBrowserAs(failIfMissing, WebDriver.class);
	}

	protected final Capabilities getCapabilities() {
		HasCapabilities hc = getBrowserAs(false, HasCapabilities.class);
		return (hc != null ? hc.getCapabilities() : null);
	}

	protected final TakesScreenshot getTakesScreenshot() {
		return getBrowserAs(false, TakesScreenshot.class);
	}

	protected final WebStorage getWebStorage() {
		return getBrowserAs(false, WebStorage.class);
	}

	protected final WaitHelper getWaitHelper() {
		return getAngularHelper();
	}

	protected final AngularHelper getAngularHelper() {
		return getHelper(AngularHelper.class);
	}

	protected final <T extends WebDriverHelper> T getHelper(Class<T> clazz) {
		GherkinContext ctx = GherkinContext.get();
		final String key = clazz.getCanonicalName();
		T t = ctx.getEnv().getAs(key, clazz);
		if (t == null) {
			try {
				t = clazz.getConstructor(WebDriver.class).newInstance(getBrowser());
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException | NoSuchMethodException | SecurityException e) {
				throw new RuntimeException("Failed to instantiate a copy of " + clazz.getCanonicalName(), e);
			}
			ctx.getEnv().put(key, t);
		}
		return t;
	}

	protected final Set<String> getCssClasses(WebElement element) {
		String cssClasses = element.getAttribute("class");
		Set<String> c = new LinkedHashSet<>();
		if (StringUtils.isNotBlank(cssClasses)) {
			c.addAll(Arrays.asList(cssClasses.split("\\s")));
		}
		return c;
	}

	protected void checkBrowserUrl(String url, boolean match, BiConsumer<String, String> onMismatch) {
		WebDriver browser = getBrowser();
		String current = browser.getCurrentUrl();
		if (Objects.equals(current, url.toString()) != match) {
			if (onMismatch != null) {
				onMismatch.accept(url, current);
			}
		}
	}
}