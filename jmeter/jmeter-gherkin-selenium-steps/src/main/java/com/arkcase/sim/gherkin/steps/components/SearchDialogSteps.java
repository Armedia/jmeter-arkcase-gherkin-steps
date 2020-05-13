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

import org.apache.commons.lang3.StringUtils;
import org.jbehave.core.annotations.Alias;
import org.jbehave.core.annotations.Aliases;
import org.jbehave.core.annotations.BeforeStory;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Named;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementNotVisibleException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import com.arkcase.sim.components.WebDriverHelper.WaitType;
import com.arkcase.sim.components.html.WaitHelper;
import com.arkcase.sim.gherkin.steps.BasicWebDriverSteps;
import com.arkcase.sim.tools.ByTools;

// TODO: There are multiple search dialogs, support them all as one? or separately?
public class SearchDialogSteps extends BasicWebDriverSteps {

	private static final By ROOT_LOCATOR = By.cssSelector("div.modal-dialog div.modal-content div.modal-search");
	private static final By SEARCH_FIELD = By.cssSelector("div.input-group input[ng-model=\"searchQuery\"]");
	private static final By SEARCH_BUTTON = By.cssSelector("div.input-group button.btn-primary");
	private static final By NO_RESULTS = ByTools.firstOf(By.cssSelector("div[ng-if=\"showNoData\"]"),
		By.cssSelector("div[ng-if=\"showNoDataResult\"]"));

	private WebElement root = null;
	private AngularTable results = null;

	@BeforeStory
	protected void resetState() {
		this.root = null;
		this.results = null;
	}

	private WebElement root(WaitType wait) {
		if (this.root == null) {
			this.root = (wait != null ? getWaitHelper().waitForElement(SearchDialogSteps.ROOT_LOCATOR, wait)
				: getWaitHelper().findElement(SearchDialogSteps.ROOT_LOCATOR));
		}
		return this.root;
	}

	private WebElement root() {
		return root(null);
	}

	@Given("the search dialog is active")
	@Alias("the search dialog is visible")
	public void checkSearchIsActive() {
		if (!root().isDisplayed()) { throw new ElementNotVisibleException("The search dialog is not visible"); }
	}

	@When("the search dialog is active")
	@Alias("the search dialog is visible")
	public void waitForSearch() {
		root(WaitType.VISIBLE);
	}

	@Then("wait for the search dialog")
	public void waitForSearch2() {
		waitForSearch();
	}

	@Then("wait for the search results")
	public void waitForSearchResults() {
		getWaitHelper().waitForAngular();
	}

	@Then("search for [$value]")
	@Alias("search for $value")
	public void searchForValue(@Named("value") String value) {
		if (StringUtils.isEmpty(value)) {
			throw new IllegalArgumentException("Must provide a non-empty, non-null search string");
		}
		WebElement searchField = clearSearchField();
		searchField.sendKeys(value);
		WebElement searchButton = root().findElement(SearchDialogSteps.SEARCH_BUTTON);
		WaitHelper wh = getWaitHelper();
		wh.waitForElement(searchButton, WaitType.CLICKABLE);
		searchButton.click();
		wh.waitForAngular();
		this.results = null;
	}

	@When("clearing the search field")
	@Then("clear the search field")
	public WebElement clearSearchField() {
		WebElement searchField = root().findElement(SearchDialogSteps.SEARCH_FIELD);
		searchField.clear();
		return searchField;
	}

	@Given("the search came up empty")
	@Aliases(values = {
		"the search failed", //
		"the search returned no results", //
		"there are no search results", //
	})
	public void searchWasEmpty() {
		root().findElement(SearchDialogSteps.NO_RESULTS);
	}

	@Given("the search returned results")
	@Aliases(values = {
		"the search had matches", //
		"the search found results", //
		"there are search results", //
	})
	public void searchWasNotEmpty() {
		try {
			searchWasEmpty();
			throw new NoSuchElementException("The search didn't return any results");
		} catch (NoSuchElementException e) {
			this.results = new AngularTable(getWaitHelper(), root());
		}
	}

	@Then("select row $row")
	@Alias("check row $row")
	public void selectRow(@Named("row") int row) {
		if (this.results == null) { throw new NoSuchElementException("No search results are available"); }
		this.results.select(row);
	}

	@Then("unselect row $row")
	@Alias("uncheck row $row")
	public void unselectRow(@Named("row") int row) {
		if (this.results == null) { throw new NoSuchElementException("No search results are available"); }
		this.results.unselect(row);
	}

	@Then("select all rows")
	@Alias("check all rows")
	public void selectAll() {
		if (this.results == null) { throw new NoSuchElementException("No search results are available"); }
		this.results.selectAll();
	}

	@Then("unselect all rows")
	@Aliases(values = {
		"select no rows", //
		"uncheck all rows", //
	})
	public void unselectAll() {
		if (this.results == null) { throw new NoSuchElementException("No search results are available"); }
		this.results.selectNone();
	}
}