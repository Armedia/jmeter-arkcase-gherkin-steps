/*-
 * #%L
 * Armedia ArkCase JMeter Gherkin+Selenium Step Implementations
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
 */
package com.arkcase.sim.gherkin.steps.components;

import org.jbehave.core.annotations.Alias;
import org.jbehave.core.annotations.Aliases;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.arkcase.sim.components.WebDriverHelper.WaitType;
import com.arkcase.sim.components.html.WaitHelper;
import com.arkcase.sim.gherkin.steps.BasicWebDriverSteps;
import com.arkcase.sim.tools.ByTools;

public class PDFTronSteps extends BasicWebDriverSteps {

	private static final By IFRAME = By.cssSelector("ark-document-viewer iframe");
	private static final By PAGE_CONTAINER = By.cssSelector("div.pageContainer");
	private static final By PAGE_THUMBNAIL = By.cssSelector("div.thumbContainer");

	@Given("PDFTron is ready")
	@Aliases(values = {
		"pdftron is ready", //
		"PDFTron is loaded", //
		"pdftron is loaded", //
	})
	public void PDFTronReady() {
		WaitHelper wh = getWaitHelper();
		WebElement iframe = wh.findElement(PDFTronSteps.IFRAME);
		// Switch to the iframe
		WebDriver browser = getBrowser();
		browser.switchTo().frame(iframe);
		WebElement docViewer = wh.waitForElement(
			ByTools.byOneEach(PDFTronSteps.PAGE_CONTAINER, PDFTronSteps.PAGE_THUMBNAIL), WaitType.PRESENT);
		if (!docViewer.isDisplayed()) { throw new RuntimeException("PDFTron is not ready"); }
		browser.switchTo().parentFrame();
	}

	@When("waiting for PDFTron")
	@Alias("waiting for pdftron")
	public void waitForPDFTron() {
		WaitHelper wh = getWaitHelper();
		WebElement iframe = wh.waitForElement(PDFTronSteps.IFRAME, WaitType.VISIBLE);
		// Switch to the iframe
		WebDriver browser = getBrowser();
		browser.switchTo().frame(iframe);
		wh.waitForElement(ByTools.byOneEach(PDFTronSteps.PAGE_CONTAINER, PDFTronSteps.PAGE_THUMBNAIL),
			WaitType.VISIBLE);
		browser.switchTo().parentFrame();
	}

	@Then("wait for PDFTron")
	@Alias("wait for pdftron")
	public void waitForPDFTron2() {
		waitForPDFTron();
	}
}