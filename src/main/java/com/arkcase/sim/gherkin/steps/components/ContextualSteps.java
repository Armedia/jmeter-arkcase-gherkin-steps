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

import java.util.Objects;
import java.util.function.Supplier;

import org.openqa.selenium.WebElement;

import com.arkcase.sim.gherkin.steps.BasicWebDriverSteps;

public class ContextualSteps extends BasicWebDriverSteps implements Supplier<WebElement> {

	private WebElement context = null;

	public final WebElement setContext(WebElement context) {
		return (this.context = Objects.requireNonNull(context, "Must provide a non-null context element"));
	}

	public final void clearContext() {
		this.context = null;
	}

	public final WebElement getContext() {
		return get();
	}

	@Override
	public final WebElement get() {
		return this.context;
	}
}