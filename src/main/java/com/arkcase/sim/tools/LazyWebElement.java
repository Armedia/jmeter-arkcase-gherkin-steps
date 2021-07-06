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
package com.arkcase.sim.tools;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Point;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;

public class LazyWebElement extends LazyReference<WebElement> implements WebElement, WebElementWrapper {

	public LazyWebElement(SearchContext ctx, By locator) {
		super(() -> ctx.findElement(locator));
	}

	@Override
	public <X> X getScreenshotAs(OutputType<X> target) throws WebDriverException {
		return get().getScreenshotAs(target);
	}

	@Override
	protected WebElement construct() {
		try {
			return super.construct();
		} catch (Throwable t) {
			// Reset if we run into any trouble so the object remains
			// valid and can attempt the fetch again later
			throw new ConstructionException(t.getMessage(), t);
		}
	}

	@Override
	public void click() {
		get().click();
	}

	@Override
	public void submit() {
		get().submit();
	}

	@Override
	public void sendKeys(CharSequence... keysToSend) {
		get().sendKeys(keysToSend);
	}

	@Override
	public void clear() {
		get().clear();
	}

	@Override
	public String getTagName() {
		return get().getTagName();
	}

	@Override
	public String getAttribute(String name) {
		return get().getAttribute(name);
	}

	@Override
	public boolean isSelected() {
		return get().isSelected();
	}

	@Override
	public boolean isEnabled() {
		return get().isEnabled();
	}

	@Override
	public String getText() {
		return get().getText();
	}

	@Override
	public List<WebElement> findElements(By by) {
		return get().findElements(by);
	}

	@Override
	public WebElement findElement(By by) {
		return get().findElement(by);
	}

	@Override
	public boolean isDisplayed() {
		return get().isDisplayed();
	}

	@Override
	public Point getLocation() {
		return get().getLocation();
	}

	@Override
	public Dimension getSize() {
		return get().getSize();
	}

	@Override
	public Rectangle getRect() {
		return get().getRect();
	}

	@Override
	public String getCssValue(String propertyName) {
		return get().getCssValue(propertyName);
	}
}