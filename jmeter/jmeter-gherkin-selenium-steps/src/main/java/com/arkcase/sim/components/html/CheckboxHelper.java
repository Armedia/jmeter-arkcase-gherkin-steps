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

import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class CheckboxHelper extends WaitHelper {

	public CheckboxHelper(WebDriver browser) {
		super(browser);
	}

	public final void markCheckbox(By by, boolean markChecked) {
		markCheckbox(by, markChecked, null, null);
	}

	public final void markCheckbox(By by, boolean markChecked, Duration wait) {
		markCheckbox(by, markChecked, wait, null);
	}

	public final void markCheckbox(By by, boolean markChecked, Duration wait, Duration pollTime) {
		markCheckbox(waitForElement(by, WaitType.CLICKABLE, wait, pollTime), markChecked);
	}

	public final void markCheckbox(WebElement element, boolean markChecked) {
		if (element.isSelected()) {
			element.click();
		}
	}
}
