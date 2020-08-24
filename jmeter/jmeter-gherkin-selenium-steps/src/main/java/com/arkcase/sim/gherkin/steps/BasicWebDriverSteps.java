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
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.jbehave.core.annotations.Alias;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Named;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Point;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriver.Window;

import com.armedia.commons.jmeter.gherkin.Gherkin;

@Gherkin.Steps
public class BasicWebDriverSteps extends WebDriverClient {

	public static final String DEFAULT_SCREENSHOT_DIRECTORY = "screenshots";
	public static final String DEFAULT_SCREENSHOT_NAME = "screenshot";
	public static final String DEFAULT_PAGE_SOURCE_DIRECTORY = "pagesources";
	public static final String DEFAULT_PAGE_SOURCE_NAME = "pagesource.html";
	public static final String DEFAULT_PAGE_SOURCE_EXTENSION = "html";
	public static final String DEFAULT_SNAPSHOT_DIRECTORY = "snapshot";
	public static final String DEFAULT_SNAPSHOT_NAME = "snapshot";

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

	@Then("take a screenshot")
	public void takeScreenshot() {
		takeScreenshot(BasicWebDriverSteps.DEFAULT_SCREENSHOT_DIRECTORY, BasicWebDriverSteps.DEFAULT_SCREENSHOT_NAME);
	}

	@Then("take a screenshot named [$name]")
	public void takeScreenshot(@Named("name") String name) {
		takeScreenshot(BasicWebDriverSteps.DEFAULT_SCREENSHOT_DIRECTORY, name);
	}

	@Then("take a screenshot to [$dir]")
	public void takeScreenshotTo(@Named("dir") String dir) {
		takeScreenshot(dir, BasicWebDriverSteps.DEFAULT_SCREENSHOT_NAME);
	}

	@Then("take a screenshot to [$dir] named [$name]")
	public void takeScreenshot(@Named("dir") String dir, @Named("name") String name) {
		takeScreenshot(dir, name, Instant.now());
	}

	private void takeScreenshot(String dir, String name, Instant instant) {
		WebDriver browser = getBrowser();
		if (!TakesScreenshot.class.isInstance(browser)) {
			String browserClass = (browser != null ? browser.getClass().getName() : "<null>");
			throw new ClassCastException(
				"The current WebDriver instance " + browserClass + " does not support taking screenshots");
		}

		// First things first: can we create a "screenshots" directory?
		File baseDir = new File(dir).getAbsoluteFile();
		try {
			baseDir = baseDir.getCanonicalFile();
		} catch (IOException e) {
			// Ignore this error...for now...
		}
		if (!baseDir.mkdirs() && !baseDir.isDirectory()) {
			throw new RuntimeException("Failed to find or create the screenshots directory at [" + baseDir + "]");
		}

		TakesScreenshot ts = TakesScreenshot.class.cast(browser);
		File screenShot = ts.getScreenshotAs(OutputType.FILE);
		File target = new File(baseDir, name //
			+ ".@" + String.format("%08x", instant.toEpochMilli()) //
			+ ".t" + String.format("%08x", Thread.currentThread().getId()) //
			+ "." + FilenameUtils.getExtension(screenShot.getName()) //
		);

		try {
			FileUtils.moveFile(screenShot, target);
		} catch (IOException e) {
			throw new RuntimeException(
				"Failed to move the screenshot at [" + screenShot.getAbsolutePath() + "] to [" + target + "]", e);
		}
	}

	@Then("save the page source")
	public void savePageSource() {
		savePageSource(BasicWebDriverSteps.DEFAULT_PAGE_SOURCE_DIRECTORY, BasicWebDriverSteps.DEFAULT_PAGE_SOURCE_NAME);
	}

	@Then("save the page source as [$name]")
	public void savePageSource(@Named("name") String name) {
		savePageSource(BasicWebDriverSteps.DEFAULT_PAGE_SOURCE_DIRECTORY, name);
	}

	@Then("save the page source to [$dir]")
	public void savePageSourceTo(@Named("dir") String dir) {
		savePageSource(dir, BasicWebDriverSteps.DEFAULT_PAGE_SOURCE_NAME);
	}

	@Then("save the page source to [$dir] as [$name]")
	public void savePageSource(@Named("dir") String dir, @Named("name") String name) {
		savePageSource(dir, name, Instant.now());
	}

	private void savePageSource(String dir, String name, Instant instant) {
		WebDriver browser = getBrowser();

		// First things first: can we create a "screenshots" directory?
		File baseDir = new File(dir).getAbsoluteFile();
		try {
			baseDir = baseDir.getCanonicalFile();
		} catch (IOException e) {
			// Ignore this error...for now...
		}
		if (!baseDir.mkdirs() && !baseDir.isDirectory()) {
			throw new RuntimeException("Failed to find or create the page sources directory at [" + baseDir + "]");
		}

		String baseName = FilenameUtils.getBaseName(name);
		String extension = FilenameUtils.getExtension(name);
		if (StringUtils.isBlank(extension)) {
			extension = BasicWebDriverSteps.DEFAULT_PAGE_SOURCE_EXTENSION;
		}

		File target = new File(baseDir, baseName //
			+ ".@" + String.format("%08x", instant.toEpochMilli()) //
			+ ".t" + String.format("%08x", Thread.currentThread().getId()) //
			+ "." + extension //
		);

		try {
			FileUtils.write(target, browser.getPageSource(), StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new RuntimeException("Failed to save the page source to [" + target + "]", e);
		}
	}

	@Then("take a full snapshot")
	public void takeSnapshot() {
		takeSnapshot(BasicWebDriverSteps.DEFAULT_SNAPSHOT_NAME);
	}

	@Then("take a full snapshot named [$name]")
	public void takeSnapshot(@Named("name") String name) {
		takeSnapshot(BasicWebDriverSteps.DEFAULT_SNAPSHOT_DIRECTORY, name);
	}

	@Then("take a full snapshot to [$dir]")
	public void takeSnapshotTo(@Named("dir") String dir) {
		takeSnapshot(dir, BasicWebDriverSteps.DEFAULT_SNAPSHOT_NAME);
	}

	@Then("take a full snapshot to [$dir] named [$name]")
	public void takeSnapshot(@Named("dir") String dir, @Named("name") String name) {
		Instant instant = Instant.now();
		savePageSource(dir, name, instant);
		takeScreenshot(dir, name, instant);
	}

	@Then("delete all the cookies")
	@Alias("clear all the cookies")
	public void clearAllCookies() {
		getBrowser().manage().deleteAllCookies();
	}

	@Then("delete the cookie named [$cookie]")
	@Alias("clear the cookie named [$cookie]")
	public void clearAllCookies(@Named("cookie") String cookie) {
		getBrowser().manage().deleteCookieNamed(cookie);
	}
}