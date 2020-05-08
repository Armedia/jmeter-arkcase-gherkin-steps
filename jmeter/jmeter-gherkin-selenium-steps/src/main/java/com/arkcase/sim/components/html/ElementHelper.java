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

import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.pagefactory.ByChained;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;

import com.arkcase.sim.tools.ByTools;

public class ElementHelper extends WaitHelper {

	public ElementHelper(WebDriver browser) {
		super(browser);
	}

	public final void actionMouseMove(By element) {
		newActions() //
			.moveToElement(waitForElement(element, WaitType.VISIBLE)) //
			.build() //
			.perform();
	}

	public final void actionMouseDown(By element) {
		newActions() //
			.moveToElement(waitForElement(element, WaitType.VISIBLE)) //
			.clickAndHold() // TODO: Is this correct?
			.build() //
			.perform();
	}

	public final void actionDragAndDrop(By source, By target) {
		newActions() //
			.dragAndDrop(waitForElement(source, WaitType.VISIBLE), waitForElement(target, WaitType.VISIBLE)) //
			.build() //
			.perform();
	}

	public final void actionDoubleClick(By element) {
		newActions() //
			.doubleClick(waitForElement(element, WaitType.VISIBLE)) //
			.build() //
			.perform();
	}

	public final void actionClick(By element) {
		newActions() //
			.click(waitForElement(element, WaitType.VISIBLE)) //
			.build() //
			.perform();
	}

	public final void actionHoverOver(By element) {
		newActions() //
			.moveToElement(waitForElement(element, WaitType.VISIBLE)) //
			.build() //
			.perform();
	}

	public final void actionHoverOverAndClick(By hover, By target) {
		newActions() //
			.moveToElement(waitForElement(hover, WaitType.VISIBLE)) //
			.click(waitForElement(target, WaitType.VISIBLE)) //
			.build() //
			.perform();
	}

	public final boolean hasOption(By element, String option) {
		try {
			this.browser.findElement(new ByChained(element, ByTools.cssContainingText("option", option)));
			return true;
		} catch (NoSuchElementException e) {
			return false;
		}
	}

	public final WebElement getFocusedElement() {
		return this.browser.switchTo().activeElement();
	}

	public final WebElement currentSelectedOptionByText(String text) {
		String selector = "//option[@selected=\"selected\" and normalize-space(.)=\"%s\"]";
		return this.browser.findElement(By.cssSelector(String.format(selector, escapeChars('"', text))));
	}

	public final WebElement getSelectedOption(By select) {
		return getSelectedOption(this.browser.findElement(select));
	}

	public final WebElement getSelectedOption(WebElement element) {
		return element.findElement(By.cssSelector("option[selected]"));
	}

	public final ExpectedCondition<WebElement> isVisible(By element) {
		return ExpectedConditions.visibilityOfElementLocated(element);
	}

	public final ExpectedCondition<Boolean> isNotVisible(By element) {
		return ExpectedConditions.invisibilityOfElementLocated(element);
	}

	public final ExpectedCondition<WebElement> inDom(By element) {
		return ExpectedConditions.presenceOfElementLocated(element);
	}

	public final ExpectedCondition<Boolean> notInDom(By element) {
		return ExpectedConditions.not(inDom(element));
	}

	public final ExpectedCondition<WebElement> isClickable(By element) {
		return ExpectedConditions.elementToBeClickable(element);
	}

	public final ExpectedCondition<Boolean> hasText(By element, String text) {
		return ExpectedConditions.textToBePresentInElementLocated(element, text);
	}

	public final ExpectedCondition<Boolean> titleIs(String title) {
		return ExpectedConditions.titleIs(title);
	}

	public final ExpectedCondition<Boolean> hasClass(By element, String klass) {
		return (d) -> {
			String classes = d.findElement(element).getAttribute("class");
			if (StringUtils.isBlank(classes)) { return false; }
			String[] k = classes.split(" ");
			for (String c : k) {
				if (StringUtils.equals(c, klass)) { return true; }
			}
			return false;
		};
	}

	public final ExpectedCondition<Boolean> hasClassRegex(By element, String klass) {
		final Pattern p = Pattern.compile("(^|.*\\W)" + klass + "(\\W.*|$)");
		return (d) -> {
			String classes = getOrDefault(d.findElement(element).getAttribute("class"), "");
			return p.matcher(classes).matches();
		};
	}

	public final void click(WebElement element) {
		waitForElement(element, WaitType.CLICKABLE);
		element.click();
	}

	public final void click(By element) {
		waitForElement(element, WaitType.CLICKABLE).click();
	}

	public final void clickIfPresent(By element) {
		try {
			this.browser.findElement(element).click();
		} catch (NoSuchElementException e) {
			// Do nothing...
		}
	}

	public final void clickUsingJs(By element) {
		clickUsingJsNoWait(element);
	}

	public final void clickUsingJsNoWait(By element) {
		runJavaScript("arguments[0].click()", waitForElement(element, WaitType.CLICKABLE));
	}

	public final void selectDropDownByIndex(By element, int option) {
		this.browser.findElements(By.tagName("option")).get(option).click();
	}

	public final void scrollToElement(By element) {
		runJavaScript("arguments[0].scrollIntoView();", this.browser.findElement(element));
	}

	public final String getAttributeValue(By element, String attribute) {
		return StringUtils.trim(this.browser.findElement(element).getAttribute(attribute));
	}

	public final String getText(By element) {
		return waitForElementToHaveText(element).getText();
	}

	public final void openLinkInNewTabUsingTarget(By element) {
		String script = "const item = arguments[0];item.setAttribute(\"target\", \"_blank\"); item.click()";
		runJavaScript(script, this.browser.findElement(element));
	}

	public final void openLinkInNewTabUsingWindowOpener(By element) {
		String script = "return window.open(arguments[0].getAttribute(\"href\"),\"_blank\")";
		runJavaScript(script, this.browser.findElement(element));
	}
}
