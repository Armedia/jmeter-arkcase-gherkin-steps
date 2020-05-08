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
package com.arkcase.sim.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.pagefactory.ByChained;

import com.arkcase.sim.components.WebDriverHelper;
import com.arkcase.sim.components.html.ButtonHelper;
import com.arkcase.sim.components.html.CheckboxHelper;
import com.arkcase.sim.components.html.DropDownHelper;
import com.arkcase.sim.components.html.ElementHelper;
import com.arkcase.sim.components.html.PageHelper;
import com.arkcase.sim.components.html.TextBoxHelper;
import com.arkcase.sim.components.html.WaitHelper;
import com.arkcase.sim.tools.ByTools;
import com.arkcase.sim.tools.TextTools;

public class BasePageHelper extends WaitHelper {

	public static class AddNewPersonModal extends WebDriverHelper {
		private static final By BY_ADD_NEW_PERSON = ByTools.cssContainingText("[ng-click=\"addNewPerson()\"]",
			"Add new Person");
		private static final By BY_PERSON = By.id("person");
		private static final By BY_TYPES = By.id("types");
		private static final By BY_SAVE_PERSON_BUTTON = ByTools.cssContainingText("[ng-click=\"onClickOk()\"]",
			"Save Person");
		private static final By BY_CANCEL_BUTTON = ByTools.cssContainingText("[ng-click=\"onClickCancel()\"]",
			"Cancel");

		public final WebElement addNewPersonButton;
		public final WebElement searchExistingPersonField;
		public final WebElement personAssociationTypesDropDown;
		public final WebElement savePersonButton;
		public final WebElement cancelButton;

		private AddNewPersonModal(WebDriver browser) {
			super(browser);
			this.addNewPersonButton = findElement(AddNewPersonModal.BY_ADD_NEW_PERSON);
			this.searchExistingPersonField = findElement(AddNewPersonModal.BY_PERSON);
			this.personAssociationTypesDropDown = findElement(AddNewPersonModal.BY_TYPES);
			this.savePersonButton = findElement(AddNewPersonModal.BY_SAVE_PERSON_BUTTON);
			this.cancelButton = findElement(AddNewPersonModal.BY_CANCEL_BUTTON);
		}
	}

	public static class SearchUserAndGroup extends WaitHelper {
		private static final By BY_SEARCH_FIELD = ByTools.ngModel("searchQuery");
		private static final By BY_SEARCH_BUTTON = ByTools.cssContainingText("[ng-click=\"queryExistingItems()\"]",
			"Search");
		private static final By BY_OK_BUTTON = ByTools.cssContainingText("[ng-click=\"onClickOk()\"]", "OK");

		public final WebElement searchField;
		public final WebElement searchButton;
		public final WebElement okButton;

		public SearchUserAndGroup(WebDriver browser) {
			super(browser);
			this.searchField = findElement(SearchUserAndGroup.BY_SEARCH_FIELD);
			this.searchButton = findElement(SearchUserAndGroup.BY_SEARCH_BUTTON);
			this.okButton = findElement(SearchUserAndGroup.BY_OK_BUTTON);
		}
	}

	public static class SearchObject extends WaitHelper {
		private static final By BY_SUBMIT_RESULT_BUTTON = ByTools.cssContainingText("[ng-click=\"onClickOk()\"]",
			"Submit Result");

		public final WebElement submitResultButton;

		public SearchObject(WebDriver browser) {
			super(browser);
			this.submitResultButton = findElement(SearchObject.BY_SUBMIT_RESULT_BUTTON);
		}
	}

	protected final ButtonHelper buttonHelper;
	protected final CheckboxHelper checkboxHelper;
	protected final DropDownHelper dropDownHelper;
	protected final ElementHelper elementHelper;
	protected final PageHelper pageHelper;
	protected final TextBoxHelper textBoxHelper;

	protected static final By BY_USER_PROFILE_BOX = By.cssSelector("div.user-menu.dropdown");
	protected static final By BY_USER_MENU_TOGGLE = new ByChained( //
		BasePageHelper.BY_USER_PROFILE_BOX, //
		By.cssSelector("a.dropdown-toggle") //
	);
	protected static final By BY_USER_MENU_LOGOUT_LINK = new ByChained( //
		BasePageHelper.BY_USER_PROFILE_BOX, //
		By.cssSelector("ul.dropdown-menu"), //
		By.cssSelector("a[ng-click=\"onClickLogout()\"]") //
	);
	protected static final By BY_NEW_BUTTON = new ByChained( //
		By.cssSelector("header.navbar-arkcase"), //
		By.tagName("nav"), //
		By.cssSelector("ul.nav.navbar-nav"), //
		By.cssSelector("li.dropdown"), //
		By.cssSelector("a.dropdown-toggle"), //
		By.cssSelector("i.fa.fa-plus-circle") //
	);

	public BasePageHelper(WebDriver browser) {
		super(browser);
		this.buttonHelper = new ButtonHelper(browser);
		this.checkboxHelper = new CheckboxHelper(browser);
		this.dropDownHelper = new DropDownHelper(browser);
		this.elementHelper = new ElementHelper(browser);
		this.pageHelper = new PageHelper(browser);
		this.textBoxHelper = new TextBoxHelper(browser);
	}

	public final WebElement userProfile() {
		return findElement(BasePageHelper.BY_USER_PROFILE_BOX);
	}

	public final WebElement getActiveMenuItem(String text) {
		return findElement(By.xpath(TextTools.interpolate(
			"//div[contains(@class,\"df-sidebar__item--active\")]//a[normalize-space(.)=\"${0}\"]", text)));
	}

	public final WebElement getSidebarMenu(String menuText) {
		return findElement(By.xpath(TextTools.interpolate("//a[normalize-space(.)='${0}']", menuText)));
	}

	public final WebElement newButton() {
		return findElement(BasePageHelper.BY_NEW_BUTTON);
	}

	public final WebElement getNewObject(String name) {
		return findElement(By.cssSelector(TextTools.interpolate("[title='${0}']", name)));
	}

	public final AddNewPersonModal addNewPersonModal() {
		return new AddNewPersonModal(this.browser);
	}

	public final SearchUserAndGroup searchUserAndGroup() {
		return new SearchUserAndGroup(this.browser);
	}

	public final SearchObject searchObject() {
		return new SearchObject(this.browser);
	}

	public final WebElement returnSearchedGridCell(String cellText) {
		return findElement(returnSearchedGridCellBy(cellText));
	}

	public final By returnSearchedGridCellBy(String cellText) {
		return ByTools.cssContainingText(".ui-grid-cell-contents.ng-binding.ng-scope", cellText);
	}

	public void logout() {
		waitForElement(BasePageHelper.BY_USER_MENU_TOGGLE, WaitType.CLICKABLE).click();
		waitForElement(BasePageHelper.BY_USER_MENU_LOGOUT_LINK, WaitType.CLICKABLE).click();
	}

	public void searchUserGroup(String assignee, String owningGroup) {
		SearchUserAndGroup searchUserAndGroup = searchUserAndGroup();
		this.textBoxHelper.sendKeys(searchUserAndGroup.searchField, assignee);
		searchUserAndGroup.searchButton.click();
		waitForElement(returnSearchedGridCellBy(assignee), WaitType.VISIBLE).click();
		waitForElement(returnSearchedGridCellBy(owningGroup), WaitType.VISIBLE).click();
		searchUserAndGroup.okButton.click();
	}

	public void searchObject(String id) {
		SearchUserAndGroup searchUserAndGroup = searchUserAndGroup();
		SearchObject searchObject = searchObject();
		this.textBoxHelper.sendKeys(searchUserAndGroup.searchField, id);
		searchUserAndGroup.searchButton.click();
		waitForElement(returnSearchedGridCellBy(id), WaitType.VISIBLE).click();
		searchObject.submitResultButton.click();
	}
}
