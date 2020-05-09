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

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import com.arkcase.sim.components.WebDriverHelper.WaitType;
import com.arkcase.sim.components.html.WaitHelper;

public class CreateFormData extends AbstractFormData {

	private static final By TAB_PANE = By.cssSelector("ng-form[name=\"createNewOrderForm\"] div.tab-pane");

	private static final By EXPAND_ALL = By.cssSelector("i.fa-expand");

	private static final Map<String, FormTab> TABS;

	static {
		// TODO: Initialize the TAB defintions!
		Map<String, FormTab> tabs = new LinkedHashMap<>();

		TABS = Collections.unmodifiableMap(tabs);
	}

	protected FormTab getTab(String tabName) {
		final FormTab tab = CreateFormData.TABS.get(tabName);
		if (tab == null) { throw new NoSuchElementException(String.format("No tab named [%s]", tabName)); }
		return tab;
	}

	protected WebElement findTab(String tabName, boolean wait) {
		return findTab(getTab(tabName), wait);
	}

	protected WebElement findTab(FormTab tab, boolean wait) {
		WaitHelper wh = getWaitHelper();
		WebElement body = null;
		if (wait) {
			wh.waitForElement(tab.title, WaitType.CLICKABLE).click();
			body = wh.waitForElement(tab.body, WaitType.PRESENT);
		} else {
			body = wh.findElement(tab.body);
		}

		WebElement expandAll = body.findElement(CreateFormData.EXPAND_ALL);
		if (wait) {
			wh.waitForElement(expandAll, WaitType.CLICKABLE);
		}
		expandAll.click();
		return body;
	}

	protected WebElement findSection(String tabName, String sectionName, boolean wait) {
		final FormTab tab = getTab(tabName);
		final FormSection section = tab.getSection(sectionName);
		if (section == null) {
			throw new NoSuchElementException(String.format("No section named [%s] in tab [%s]", tabName, sectionName));
		}

		final WebElement tabBody = findTab(tab, wait);

		WebElement sectionBody = tabBody.findElement(section.body);
		if (wait) {
			getWaitHelper().waitForElement(sectionBody, WaitType.VISIBLE);
		}

		return sectionBody;
	}

	protected WebElement findField(WebElement section, String label) {
		// Find the field with the given name within the active section
		return null;
	}
}
