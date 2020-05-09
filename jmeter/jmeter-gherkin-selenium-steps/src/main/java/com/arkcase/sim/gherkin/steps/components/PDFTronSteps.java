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

public class PDFTronSteps extends BasicWebDriverSteps {

	private static final By IFRAME = By.cssSelector("ark-document-viewer iframe");
	private static final By DOCVIEWER = By.id("DocumentViewer");

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
		wh.findElement(PDFTronSteps.DOCVIEWER);
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
		wh.waitForElement(PDFTronSteps.DOCVIEWER, WaitType.VISIBLE);
		browser.switchTo().parentFrame();
	}

	@Then("wait for PDFTron")
	@Alias("wait for pdftron")
	public void waitForPDFTron2() {
		waitForPDFTron();
	}
}