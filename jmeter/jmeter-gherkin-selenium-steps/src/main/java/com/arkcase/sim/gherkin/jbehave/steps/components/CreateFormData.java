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
import java.util.LinkedHashMap;
import java.util.Map;

import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.arkcase.sim.components.WebDriverHelper.WaitType;
import com.arkcase.sim.components.html.WaitHelper;

public class CreateFormData extends AbstractFormData {

	private static final Map<String, FieldGroupContainer> TABS;

	static {
		// TODO: Initialize the TAB defintions!
		Map<String, FieldGroupContainer> tabs = new LinkedHashMap<>();

		TABS = Collections.unmodifiableMap(tabs);
	}

	protected FieldGroupContainer findTab(String tabName) {
		final FieldGroupContainer tab = CreateFormData.TABS.get(tabName);
		if (tab == null) { throw new NoSuchElementException(String.format("No tab named [%s]", tabName)); }
		WebDriver browser = getBrowser();
		WaitHelper wh = getWaitHelper();
		WebElement element = wh.waitForElement(tab.title, WaitType.VISIBLE);
		return null;
	}

	protected WebElement findSection(String tabName, String sectionName) {
		final FieldGroupContainer tab = CreateFormData.TABS.get(tabName);
		if (tab == null) { throw new NoSuchElementException(String.format("No tab named [%s]", tabName)); }

		final FieldGroup section = tab.getSection(sectionName);
		if (section == null) {
			throw new NoSuchElementException(String.format("No section named [%s] in tab [%s]", tabName, sectionName));
		}

		WaitHelper wh = getWaitHelper();

		// First, activate the tab
		WebElement element = wh.waitForElement(tab.title, WaitType.CLICKABLE);
		element.click();

		// WaitTool until the body is displayed (animation wait)
		element = wh.waitForElement(tab.body, WaitType.VISIBLE);

		// Now find the element that contains the section
		return element.findElement(section.body);
	}

	protected WebElement findAndActivateSection(String tabName, String sectionName) {
		final FieldGroupContainer tab = CreateFormData.TABS.get(tabName);
		if (tab == null) { throw new NoSuchElementException(String.format("No tab named [%s]", tabName)); }

		final FieldGroup section = tab.getSection(sectionName);
		if (section == null) {
			throw new NoSuchElementException(String.format("No section named [%s] in tab [%s]", tabName, sectionName));
		}

		WaitHelper wh = getWaitHelper();

		// First, activate the tab
		WebElement element = wh.waitForElement(tab.title, WaitType.CLICKABLE);
		element.click();

		// WaitTool until the body is displayed (animation wait)
		element = wh.waitForElement(tab.body, WaitType.VISIBLE);

		// Now find the element that contains the section
		return element.findElement(section.body);
	}

	protected WebElement findField(WebElement section, String label) {
		// Find the field with the given name within the active section
		return null;
	}
}