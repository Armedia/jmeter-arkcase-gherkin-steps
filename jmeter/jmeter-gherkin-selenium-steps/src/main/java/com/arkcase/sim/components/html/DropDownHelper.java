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
package com.arkcase.sim.components.html;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class DropDownHelper extends WaitHelper {

	public DropDownHelper(WebDriver browser) {
		super(browser);
	}

	public final String getCssForOptionValue(String value) {
		return String.format("option[value=\"%s\"]", escapeChars('"', getOrDefault(value, "")));
	}

	public final void selectOptionByVal(By by, String value) {
		this.browser.findElement(By.cssSelector(getCssForOptionValue(value))).click();
	}

	public final String getXPathForOptionValue(String value) {
		return String.format("//option[normalize-space(.)=\"%s\"]", escapeChars('"', getOrDefault(value, "")));
	}

	public final void selectOptionByText(By by, String value) {
		this.browser.findElement(By.xpath(getXPathForOptionValue(value))).click();
	}

}
