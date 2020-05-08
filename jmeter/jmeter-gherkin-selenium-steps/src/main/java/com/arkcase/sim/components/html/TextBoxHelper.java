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

import java.nio.CharBuffer;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.arkcase.sim.components.WebDriverHelper;

public class TextBoxHelper extends WaitHelper {

	public TextBoxHelper(WebDriver browser) {
		super(browser);
	}

	public final void clearText(By element) {
		clearText(this.browser.findElement(element));
	}

	public final void clearText(WebElement element) {
		Keys ctrl = Keys.CONTROL;
		if (getCapabilities().getPlatform() == Platform.MAC) {
			ctrl = Keys.COMMAND;
		}

		CharBuffer buf = CharBuffer.allocate(16);
		buf.append(Keys.chord(ctrl, "a")).append(Keys.BACK_SPACE);
		buf.flip();
		element.sendKeys(buf);
		element.clear();
	}

	public final WebElement sendKeys(WebElement element, String value) {
		return sendKeys(element, value, false);
	}

	public final WebElement sendKeys(WebElement element, String value, boolean sendEnter) {
		if (element == null) { return null; }
		if (StringUtils.isNotEmpty(value)) {
			clearText(element);
			element.sendKeys(value);
		}
		if (sendEnter) {
			element.sendKeys(Keys.ENTER);
		}
		return element;
	}

	public final WebElement sendKeys(By element, String value) {
		return sendKeys(element, value, false);
	}

	public final WebElement sendKeys(By element, String value, boolean sendEnter) {
		return sendKeys(waitForElement(element, WaitType.VISIBLE), value, sendEnter);
	}

	public final void typeSlowly(By element, String value, int delay) {
		delay = Math.max(0, delay); // Clean up negative numbers
		if (StringUtils.isEmpty(value)) { return; }
		WebElement e = waitForElement(element, WaitType.VISIBLE);
		WebDriverHelper.LOG.info("Sending keys with {}ms delay: [{}] to {}", delay, value, element);
		clearText(e);
		for (int i = 0; i < value.length(); i++) {
			e.sendKeys(value.subSequence(i, i + 1));
			if (delay > 0) {
				try {
					Thread.sleep(delay);
				} catch (InterruptedException ex) {
					throw new RuntimeException(String.format(
						"Interrupted while typing [%s] slowly into %s with a delay of %d", value, element, delay), ex);
				}
			}
		}
	}
}
