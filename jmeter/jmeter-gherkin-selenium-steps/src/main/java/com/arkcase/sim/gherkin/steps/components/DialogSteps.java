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

import java.util.Objects;

import org.jbehave.core.annotations.Aliases;
import org.jbehave.core.annotations.BeforeStory;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Named;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementNotVisibleException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.pagefactory.ByChained;

import com.arkcase.sim.components.WebDriverHelper.WaitType;
import com.arkcase.sim.components.html.WaitHelper;
import com.arkcase.sim.gherkin.steps.BasicWebDriverSteps;
import com.arkcase.sim.tools.ByTools;

public class DialogSteps extends BasicWebDriverSteps {

	protected static final By ROOT_LOCATOR = By
		.xpath("/html/body/div[@modal-render='true' and @role='dialog' and @modal-window='modal-window']");

	protected static final By HEADER_LOCATOR = By.cssSelector("div.modal-header");
	protected static final By TITLE_LOCATOR = By.cssSelector("div.modal-title");

	protected static final By BODY_LOCATOR = By.cssSelector("div.modal-body");

	protected static final By FOOTER_LOCATOR = By.cssSelector("div.modal-footer");

	@BeforeStory
	protected void resetState() {
	}

	static WebElement findDialog(WaitHelper helper, String title, WaitType wait) {
		Objects.requireNonNull(helper, "Must provide a WaitHelper instance");
		By locator = DialogSteps.ROOT_LOCATOR;
		if (title != null) {
			locator = new ByChained(locator, //
				ByTools.addPredicate( //
					DialogSteps.TITLE_LOCATOR, //
					ByTools.Pred.textEquals(title) //
				) //
			);
		}
		if (wait != null) { return helper.waitForElement(locator, wait); }
		return helper.findElement(locator);
	}

	private WebElement findDialog(String title, WaitType wait) {
		return DialogSteps.findDialog(getWaitHelper(), title, wait);
	}

	private WebElement findDialog(String title) {
		return findDialog(title, null);
	}

	private WebElement findDialog(WaitType wait) {
		return findDialog(null, wait);
	}

	private WebElement findDialog() {
		return findDialog(null, null);
	}

	@Given("the [$title] dialog is shown")
	@Aliases(values = {
		"the [$title] dialog is visible", //
		"the [$title] dialog is displayed", //
		"the [$title] dialog is open", //
		"the [$title] dialog is opened", //
		"the [$title] dialog is active", //
	})
	public void namedDialogIsShown(@Named("title") String title) {
		if (!findDialog(title).isDisplayed()) {
			throw new ElementNotVisibleException("The [" + title + "] dialog is not visible");
		}
	}

	@When("the [$title] dialog is shown")
	@Aliases(values = {
		"the [$title] dialog is visible", //
		"the [$title] dialog is displayed", //
		"the [$title] dialog is open", //
		"the [$title] dialog is opened", //
		"the [$title] dialog is active", //
	})
	public void whenNamedDialogIsShown(@Named("title") String title) {
		findDialog(title, WaitType.VISIBLE);
	}

	@Then("wait until the [$title] dialog is shown")
	@Aliases(values = {
		"wait until the [$title] dialog is visible", //
		"wait until the [$title] dialog is displayed", //
		"wait until the [$title] dialog is open", //
		"wait until the [$title] dialog is opened", //
		"wait until the [$title] dialog is active", //
	})
	public void thenWaitUntilNamedDialogIsShown(@Named("title") String title) {
		whenNamedDialogIsShown(title);
	}

	@Given("a dialog is shown")
	@Aliases(values = {
		"a dialog is visible", //
		"a dialog is displayed", //
		"a dialog is open", //
		"a dialog is opened", //
		"a dialog is active", //
	})
	public void aDialogIsShown() {
		findDialog();
		if (!findDialog().isDisplayed()) { throw new ElementNotVisibleException("No dialog"); }
	}

	@When("the dialog is shown")
	@Aliases(values = {
		"the dialog is visible", //
		"the dialog is displayed", //
		"the dialog is open", //
		"the dialog is opened", //
		"the dialog is active", //
	})
	public void whenTheDialogIsShown() {
		findDialog(WaitType.VISIBLE);
	}

	@Then("wait until the dialog is shown")
	@Aliases(values = {
		"wait until the dialog is visible", //
		"wait until the dialog is displayed", //
		"wait until the dialog is open", //
		"wait until the dialog is opened", //
		"wait until the dialog is active", //
		"wait for the dialog to be shown", //
		"wait for the dialog to be visible", //
		"wait for the dialog to be displayed", //
		"wait for the dialog to be open", //
		"wait for the dialog to be opened", //
		"wait for the dialog to be active", //
	})
	public void thenWaitUntilTheDialogIsshown() {
		whenTheDialogIsShown();
	}

	// @Then("click the [$button] button on the dialog")

	// TODO: Add dialog close
	// TODO: Add button support on the bottom?

}