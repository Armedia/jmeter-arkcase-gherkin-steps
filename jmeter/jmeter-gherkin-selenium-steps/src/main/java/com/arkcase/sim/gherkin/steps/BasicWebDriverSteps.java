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

import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.util.Set;

import org.jbehave.core.annotations.Alias;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Named;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriver.Window;

import com.armedia.commons.jmeter.gherkin.Gherkin;

@Gherkin.Steps
public class BasicWebDriverSteps extends WebDriverClient {

	@When("switching to the main page")
	@Then("switch to the main page")
	public void switchToMainWindow() {
		WebDriver browser = getBrowser();
		Set<String> windowHandles = browser.getWindowHandles();
		if (windowHandles.isEmpty()) { throw new IllegalStateException("No more windows to switch to"); }
		browser.switchTo().window(windowHandles.iterator().next());
	}

	@Given("the browser instance is not available")
	public void browserIsNotActive() {
		WebDriver driver = getBrowser(false);
		if (driver != null) { throw new IllegalStateException("A browser instance is already available: " + driver); }
	}

	@Given("the browser instance is available")
	public void browserIsActive() {
		getBrowser(true);
	}

	@Given("the browser URL is not $url")
	public void checkBrowserNotUrl(@Named("url") String url) {
		checkBrowserUrl(url, false, (u, c) -> {
			throw new IllegalStateException(
				String.format("The browser's URL is [%s], when it should have been different", c));
		});
	}

	@Given("the browser URL is $url")
	public void checkBrowserUrl(@Named("url") String url) {
		checkBrowserUrl(url, true, (u, c) -> {
			throw new IllegalStateException(
				String.format("The browser's URL is [%s], when it should have been [%s]", c, url));
		});
	}

	@Given("the browser window is maximized")
	public void browserIsMaximized() {
		if (GraphicsEnvironment.isHeadless()) {
			throw new IllegalStateException("Can't maximize the browser window in headless mode");
		}
		Dimension browser = getBrowser().manage().window().getSize();
		java.awt.Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		if ((screen.height != browser.height) || (screen.width != browser.width)) {
			throw new RuntimeException(
				String.format("The browser is not maximized: browser is [%s] vs. screen is [%s]", browser, screen));
		}
	}

	@When("maximizing the browser window")
	@Then("maximize the browser window")
	public void maximizeBrowser() {
		if (GraphicsEnvironment.isHeadless()) {
			throw new IllegalStateException("Can't maximize the browser window in headless mode");
		}
		getBrowser().manage().window().maximize();
	}

	@Given("the browser window is fullscreen")
	public void browserIsFullscreen() {
		if (GraphicsEnvironment.isHeadless()) {
			throw new IllegalStateException("Can't make the browser go fullscreen in headless mode");
		}

		ScreenInfo info = new ScreenInfo();
		if (!info.isFullscreen()) { throw new RuntimeException(String.format("The browser is not fullscreen")); }
	}

	@When("setting the browser window to fullscreen")
	@Then("set the browser window to fullscreen")
	public void fullscreenBrowser() {
		if (GraphicsEnvironment.isHeadless()) {
			throw new IllegalStateException("Can't make the browser go fullscreen in headless mode");
		}
		getBrowser().manage().window().fullscreen();
	}

	@Given("the browser size is $width x $height")
	@When("setting the browser size to $width x $height")
	@Then("set the browser size to $width x $height")
	public void setBrowserResolution(@Named("width") int width, @Named("height") int height) {
		if (width <= 0) { throw new IllegalArgumentException("The width may not be <= 0 (" + width + ")"); }
		if (height <= 0) { throw new IllegalArgumentException("The height may not be <= 0 (" + height + ")"); }
		getBrowser().manage().window().setSize(new Dimension(width, height));
	}

	@Given("the browser width is $width")
	@When("setting the browser width to $width")
	@Then("set the browser width to $width")
	public void setBrowserWidth(@Named("width") int width) {
		if (width <= 0) { throw new IllegalArgumentException("The width may not be <= 0 (" + width + ")"); }
		Window w = getBrowser().manage().window();
		Dimension d = w.getSize();
		w.setSize(new Dimension(width, d.getHeight()));
	}

	@Given("the browser height is $height")
	@When("setting the browser height to $height")
	@Then("set the browser height to $height")
	public void setBrowserHeight(@Named("height") int height) {
		if (height <= 0) { throw new IllegalArgumentException("The height may not be <= 0 (" + height + ")"); }
		Window w = getBrowser().manage().window();
		Dimension d = w.getSize();
		w.setSize(new Dimension(d.getWidth(), height));
	}

	@Given("the browser position is ($x, $y)")
	@When("setting the browser position to ($x, $y)")
	@Then("set the browser position to ($x, $y)")
	public void setBrowserPosition(@Named("x") int x, @Named("y") int y) {
		Window w = getBrowser().manage().window();
		Point pos = new Point(x, y);
		w.setPosition(pos);
	}

	@Then("navigate to $url")
	@Alias("go to $url")
	public void navigateTo(@Named("url") String url) {
		getBrowser().navigate().to(url);
	}

	@Then("navigate backward")
	@Alias("go back")
	public void navigateBack() {
		getBrowser().navigate().back();
	}

	@Then("navigate forward")
	@Alias("go forward")
	public void navigateForward() {
		getBrowser().navigate().forward();
	}

	@Then("reload the page")
	@Alias("refresh the page")
	public void triggerReload() {
		getBrowser().navigate().refresh();
	}

	@Then("close the browser window")
	@Alias("close the window")
	public void closeWindow() {
		getBrowser().close();
	}
}
