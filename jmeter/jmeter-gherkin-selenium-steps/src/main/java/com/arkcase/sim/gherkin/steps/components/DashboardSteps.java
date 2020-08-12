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
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.jbehave.core.annotations.Alias;
import org.jbehave.core.annotations.BeforeStory;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Named;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import com.arkcase.sim.components.WebDriverHelper.WaitType;
import com.arkcase.sim.components.html.WaitHelper;
import com.arkcase.sim.tools.CssMatcher;

public class DashboardSteps extends ComponentSteps {

	private static final By NAV_ROOT = By.cssSelector("nav.nav-primary ul.nav-main");
	private static final By NAV_ENTRIES = By.tagName("li");
	private static final By NAV_LINK = By.tagName("a");
	private static final CssMatcher NAV_ACTIVE = new CssMatcher.ClassName("active");

	private class NavEntry {
		private final String name;
		private final WebElement element;
		private final WebElement link;

		private NavEntry(WebElement element) {
			this.element = element;
			this.link = element.findElement(DashboardSteps.NAV_LINK);
			this.name = DashboardSteps.normalize(this.link.getAttribute("title"));
		}

		private void click() {
			getWaitHelper().waitForElement(this.link, WaitType.CLICKABLE);
			this.link.click();
		}

		private boolean isActive() {
			getWaitHelper().waitForElement(this.element, WaitType.VISIBLE);
			return DashboardSteps.NAV_ACTIVE.test(this.element);
		}
	}

	private static final By USER_MENU = By.cssSelector("div.user-menu.dropdown a.dropdown-toggle");
	private static final By LOGOUT_LINK = By
		.cssSelector("div.user-menu.dropdown ul.dropdown-menu a[ng-click=\"onClickLogout()\"]");

	private WebElement root = null;
	private Map<String, NavEntry> nav = null;

	@BeforeStory
	protected void beforeStory() {
		this.root = null;
		this.nav = null;
	}

	private static String normalize(String name) {
		if (name == null) { return null; }
		name = StringUtils.lowerCase(StringUtils.strip(name));
		name = name.replaceAll("\\s+", " ");
		return name;
	}

	private WebElement root(WaitType wait) {
		if (this.root == null) {
			WaitHelper wh = getWaitHelper();
			this.root = (wait != null ? wh.waitForElement(DashboardSteps.NAV_ROOT, wait)
				: wh.findElement(DashboardSteps.NAV_ROOT));
		}
		return this.root;
	}

	private NavEntry nav(String name) {
		return nav(name, null);
	}

	private NavEntry nav(String name, WaitType wait) {
		if (this.nav == null) {
			Map<String, NavEntry> nav = new LinkedHashMap<>();
			for (WebElement e : root(wait).findElements(DashboardSteps.NAV_ENTRIES)) {
				NavEntry n = new NavEntry(e);
				nav.put(n.name, n);
			}
			this.nav = Collections.unmodifiableMap(nav);
		}
		NavEntry nav = this.nav.get(DashboardSteps.normalize(name));
		if (nav == null) {
			throw new NoSuchElementException(
				String.format("Invalid navigation option [%s] (valid values = %s)", name, this.nav.keySet()));
		}
		return nav;
	}

	@When("the navigation list is ready")
	@Alias("the nav list is ready")
	public void waitForNavList() {
		root(WaitType.VISIBLE);
	}

	@Given("the navigation list is ready")
	@Alias("the nav list is ready")
	public void givenWaitForNavList2() {
		waitForNavList();
	}

	@Given("the navigation is set to $nav")
	public void givenCurrentNavIs(@Named("nav") String nav) {
		if (!nav(nav).isActive()) {
			throw new IllegalStateException(String.format("The navigation choice [%s] is not active", nav));
		}
	}

	@When("selecting the $area area")
	@Then("select the $area area")
	public void selectTab(@Named("area") String area) {
		nav(area).click();
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
}