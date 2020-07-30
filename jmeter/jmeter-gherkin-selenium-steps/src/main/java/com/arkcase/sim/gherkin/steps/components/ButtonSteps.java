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
package com.arkcase.sim.gherkin.steps.components;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Named;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.pagefactory.ByChained;
import org.openqa.selenium.support.ui.ExpectedConditions;

import com.arkcase.sim.components.WebDriverHelper.WaitType;
import com.arkcase.sim.tools.ExpectedConditionTools;

public class ButtonSteps extends ComponentSteps {

	private static final Map<String, By> BUTTONS;
	static {

		final By moduleHeader = By.cssSelector("div.module-header");
		final By footerPanel = By.cssSelector("footer.panel");
		final By topButtonGroup = new ByChained(By.cssSelector("div.content-body"), By.cssSelector("div.btn-group"));
		final By createOrderForm = By.cssSelector("ng-form[name=\"createNewOrderForm\"]");
		Object[][] buttons = {
			{
				By.cssSelector("div.idp img[alt=\"Sharecare\"]"), //
				"Sharecare Authentication", "Sharecare"
			}, {
				By.cssSelector("div.idp img[alt=\"Armedia Development FedSvcs\"]"), //
				"Armedia Authentication", "Armedia Development FedSvcs", "Armedia"
			}, {
				new ByChained(moduleHeader, By.cssSelector("button.btn-exit")), //
				"Close"
			}, {
				new ByChained(moduleHeader, By.cssSelector("button.btn-sm[title=\"Expand Document Viewer\"]")), //
				"Expand Document Viewer", "Expand Viewer", "Expand Document", "Expand"
			}, {
				new ByChained(moduleHeader, By.cssSelector("button.btn-sm[title=\"Skip Request\"]")), //
				"Skip Request", "Skip"
			}, {
				new ByChained(footerPanel, By.cssSelector("button.btn-reject[permission=\"reject\"]")), //
				"Reject"
			}, {
				new ByChained(footerPanel, By.cssSelector("button.btn-next-queue[permission=\"billOrder\"]")), //
				"Bill Request", "Bill"
			}, {
				new ByChained(footerPanel, By.cssSelector("button.btn-next-queue[permission=\"fulfillOrder\"]")), //
				"Fulfill Request", "Fulfill"
			}, {
				new ByChained(footerPanel, By.cssSelector("button.btn-next-queue[permission=\"qcOrder\"]")), //
				"QC Request", "QC"
			}, {
				new ByChained(footerPanel, By.cssSelector("button.btn-next-queue[permission=\"distributeOrder\"]")), //
				"Distribute Request", "Distribute"
			}, {
				new ByChained(footerPanel, By.cssSelector("button.btn-complete-task[permission=\"accept\"]")), //
				"Complete Task", "Complete"
			}, {
				new ByChained(footerPanel,
					By.cssSelector("button.btn-next-queue[permission=\"pendingResolutionResults\"]")), //
				"Reject Reasons"
			}, {
				new ByChained(topButtonGroup, By.cssSelector("button.btn-primary[permission=\"downloadOrder\"]")), //
				"Download"
			}, {
				new ByChained(topButtonGroup, By.cssSelector("button.btn-primary[permission=\"assignOrder\"]")), //
				"Assign Requests", "Assign"
			}, {
				new ByChained(topButtonGroup, By.cssSelector("button.btn-primary[permission=\"bulkUpdateOrders\"]")), //
				"Bulk Update"
			}, {
				new ByChained(topButtonGroup, By.cssSelector("button.btn-primary[permission=\"bulkArchiveOrders\"]")), //
				"Bulk Archive"
			}, {
				new ByChained(topButtonGroup, By.cssSelector("button.btn-primary[permission=\"bulkRejectOrders\"]")), //
				"Bulk Reject"
			}, {
				new ByChained(topButtonGroup, By.cssSelector("button.btn-primary[permission=\"completeOrders\"]")), //
				"Complete Requests"
			}, {
				new ByChained(topButtonGroup, By.cssSelector("button.btn-primary[permission=\"startWork\"]")), //
				"Start Working", "Start"
			}, {
				new ByChained(topButtonGroup, By.cssSelector("a.btn-primary[permission=\"printOrder\"]")), //
				"Local Print"
			}, {
				new ByChained(topButtonGroup, By.cssSelector("button.btn-primary[permission=\"printOrder\"]")), //
				"Central Print"
			}, {
				new ByChained(createOrderForm,
					By.cssSelector("ng-form[name=\"shippingBillingForm\"] panel-view[header=\"Requester Specifics\"]"),
					By.xpath(".//button[normalize-space(.) = \"Search Existing Requesters\"]")), //
				"Search Existing Requesters"
			},
			// TODO: Add MORE buttons so it's easier to write the code
		};

		Map<String, By> buttonLocators = new HashMap<>();
		for (Object[] o : buttons) {
			By by = By.class.cast(o[0]);
			for (int i = 1; i < o.length; i++) {
				String key = ButtonSteps.normalize(o[i].toString());
				By existing = buttonLocators.put(key, by);
				if (existing != null) {
					throw new RuntimeException(
						String.format("The button alias [%s] has two selectors:%n%s%n%s", o[i], existing, by));
				}
			}
		}
		BUTTONS = Collections.unmodifiableMap(buttonLocators);
	}

	private static String normalize(String name) {
		if (name == null) { return name; }
		name = name.trim().replaceAll("\\s+", " ");
		// name = StringUtils.lowerCase(name);
		return name;
	}

	private WebElement getButton(String name) {
		return getButton(name, true);
	}

	private WebElement getButton(String name, boolean required) {
		// First, sanitize the name
		final String normalizedName = ButtonSteps.normalize(name);

		WebDriver browser = getBrowser();

		By by = ButtonSteps.BUTTONS.get(normalizedName);
		List<WebElement> matches = null;

		// First, try the ones we specifically saved locators for...
		if ((matches == null) && (by != null)) {
			matches = browser.findElements(by);
		}

		// If we didn't have a locator, or we couldn't find it, we try by name
		if ((matches == null) || matches.isEmpty()) {
			matches = getButtonByTitleOrLabel(normalizedName);
		}

		// No matches...
		if ((matches == null) || matches.isEmpty()) {
			if (!required) { return null; }
			throw new NoSuchElementException(String.format("Failed to find a button named [%s]", name));
		}
		if (matches.size() > 1) {
			throw new RuntimeException(String.format("Found %d buttons named [%s]", matches.size(), name));
		}
		return matches.get(0);
	}

	private List<WebElement> getButtonByTitleOrLabel(String label) {
		WebDriver browser = getBrowser();

		// Escape double quotes on the label...
		label = label.replace("\"", "\\\"");

		// First, find by title
		List<WebElement> elements = browser
			.findElements(By.xpath("//button[normalize-space(@title) = \"" + label + "\"]"));
		if ((elements != null) && !elements.isEmpty()) { return elements; }

		// No luck by title? Try the text
		elements = browser.findElements(By.xpath("//button[normalize-space(.) = \"" + label + "\"]"));
		if ((elements != null) && !elements.isEmpty()) { return elements; }

		// No winners? Return null...
		return null;
	}

	@Given("the $name button is visible")
	public void buttonIsVisible(@Named("name") String name) {
		if (!getButton(name).isDisplayed()) {
			throw new IllegalStateException(String.format("The [%s] button is not visible", name));
		}
	}

	@Given("the $name button is not visible")
	public void buttonIsNotVisible(@Named("name") String name) {
		if (getButton(name).isDisplayed()) {
			throw new IllegalStateException(String.format("The [%s] button is visible", name));
		}
	}

	@Given("the $name button is clickable")
	public void buttonIsClickable(@Named("name") String name) {
		if (!getButton(name).isEnabled()) {
			throw new IllegalStateException(String.format("The [%s] button is not clickable", name));
		}
	}

	@Given("the $name button is not clickable")
	public void buttonIsNotClickable(@Named("name") String name) {
		if (getButton(name).isEnabled()) {
			throw new IllegalStateException(String.format("The [%s] button is not clickable", name));
		}
	}

	@Then("click on the $name button")
	@When("clicking on the $name button")
	public void clickButton(@Named("name") String name) {
		waitForButtonToBeClickable(name).click();
	}

	@Then("click on the $name button, switch to the new window")
	@When("clicking on the $name button, switching to the new window")
	public void clickButtonAndWaitForWindow(@Named("name") String name) {
		WebDriver browser = getBrowser();
		Set<String> handles = new HashSet<>(browser.getWindowHandles());
		clickButton(name);
		String newWindow = getWaitHelper().waitUntil(ExpectedConditionTools.newWindowOpened(handles));
		browser.switchTo().window(newWindow);
	}

	@Then("click on the $name button, wait for the window to close")
	@When("clicking on the $name button, witing for the window to close")
	public void clickButtonAndWaitForWindowToClose(@Named("name") String name) {
		WebDriver browser = getBrowser();
		String window = browser.getWindowHandle();
		clickButton(name);
		getWaitHelper().waitUntil(ExpectedConditionTools.windowIsClosed(window));
	}

	@Then("wait for the $name button to be visible")
	@When("the $name button is visible")
	public WebElement waitForButtonToBeVisible(@Named("name") String name) {
		WebElement button = getButton(name);
		getWaitHelper().waitForElement(button, WaitType.VISIBLE);
		return button;
	}

	@Then("wait for the $name button to be clickable, and click it")
	public void waitForButtonToBeClickableAndClickIt(@Named("name") String name) {
		waitForButtonToBeClickable(name).click();
	}

	@Then("wait for the $name button to be hidden")
	@When("the $name button is not visible")
	public void waitForButtonToBeHidden(@Named("name") String name) {
		WebElement button = getButton(name);
		getWaitHelper().waitForElement(button, WaitType.VISIBLE);
	}

	@Then("wait for the $name button to be clickable")
	@When("the $name button is clickable")
	public WebElement waitForButtonToBeClickable(@Named("name") String name) {
		WebElement button = getButton(name);
		getWaitHelper().waitForElement(button, WaitType.VISIBLE);
		return button;
	}

	@Then("wait for the $name button to not be clickable")
	@When("the $name button is not clickable")
	public void waitForButtonToNotBeClickable(@Named("name") String name) {
		WebElement button = getButton(name);
		getWaitHelper().waitUntil(ExpectedConditions.not(ExpectedConditions.elementToBeClickable(button)));
	}
}