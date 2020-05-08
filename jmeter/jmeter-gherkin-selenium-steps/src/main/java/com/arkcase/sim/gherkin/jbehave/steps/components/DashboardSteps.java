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
package com.arkcase.sim.gherkin.jbehave.steps.components;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.jbehave.core.annotations.Alias;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Named;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.pagefactory.ByChained;

import com.arkcase.sim.components.WebDriverHelper.WaitType;
import com.arkcase.sim.components.html.WaitHelper;

public class DashboardSteps extends ComponentSteps {

	private static final By NAVIGATION_LIST = By.cssSelector("nav.nav-primary ul.nav-main");
	private static final Map<String, By> NAVIGATION;
	static {
		String[] titles = {
			"Dashboard", "Queues", "Search", "Tasks", "Documents", "My Documents", "Notifications", "Subscriptions"
		};

		Map<String, By> navigation = new HashMap<>();
		for (String title : titles) {
			navigation.put(StringUtils.lowerCase(title),
				new ByChained(DashboardSteps.NAVIGATION_LIST, By.cssSelector(String.format("a[title=\"%s\"]", title))));
		}
		NAVIGATION = Collections.unmodifiableMap(navigation);
	}

	private static final By USER_MENU = By.cssSelector("div.user-menu.dropdown a.dropdown-toggle");
	private static final By LOGOUT_LINK = By
		.cssSelector("div.user-menu.dropdown ul.dropdown-menu a[ng-click=\"onClickLogout()\"]");

	private By getAreaLocator(String name) {
		By by = DashboardSteps.NAVIGATION.get(StringUtils.lowerCase(name));
		if (by == null) {
			throw new IllegalArgumentException(
				String.format("No navigation choice with the alias [%s] was found", name));
		}
		return by;
	}

	private WebElement getAreaSelector(String name) {
		return getAreaSelector(name, true);
	}

	private WebElement getAreaSelector(String name, boolean required) {
		By by = getAreaLocator(name);
		try {
			return getWaitHelper().waitForElement(by, WaitType.CLICKABLE);
		} catch (final NoSuchElementException e) {
			if (!required) { return null; }
			throw e;
		}
	}

	@When("the navigation list is ready")
	@Alias("the nav list is ready")
	public void waitForNavList() {
		getWaitHelper().waitForElement(DashboardSteps.NAVIGATION_LIST, WaitType.VISIBLE);
	}

	@Given("the navigation list is ready")
	@Alias("the nav list is ready")
	public void givenWaitForNavList2() {
		waitForNavList();
	}

	@Then("close the session")
	@Alias("exit")
	public void closeSession() {
		logout();
	}

	@Then("logout")
	@Alias("sign out")
	public void logout() {
		WaitHelper wh = getWaitHelper();
		wh.waitForElement(DashboardSteps.USER_MENU, WaitType.CLICKABLE).click();
		wh.waitForElement(DashboardSteps.LOGOUT_LINK, WaitType.CLICKABLE).click();
	}

	@When("selecting the $area tab")
	@Then("select the $area tab")
	public void selectTab(@Named("area") String area) {
		getAreaSelector(area).click();
	}
}
