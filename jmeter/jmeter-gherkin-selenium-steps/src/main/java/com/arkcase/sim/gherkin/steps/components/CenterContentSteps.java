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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbehave.core.annotations.Alias;
import org.jbehave.core.annotations.BeforeStory;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Named;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementNotVisibleException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import com.arkcase.sim.components.WebDriverHelper.WaitType;
import com.arkcase.sim.components.html.WaitHelper;
import com.arkcase.sim.gherkin.steps.BasicWebDriverSteps;
import com.arkcase.sim.tools.CssMatcher;
import com.arkcase.sim.tools.LazyReference;
import com.arkcase.sim.tools.LazyWebElement;

public class CenterContentSteps extends BasicWebDriverSteps {

	private static final By ROOT_LOCATOR = By
		.cssSelector("section [ng-controller=\"OrderInfoController\"] div.module-content div.content-body.well-sm");

	private static final By TAB_LOCATOR = By.cssSelector("div ul.nav-tabs li a tab-heading span[ng-bind-html]");
	private static final By TAB_LI_PARENT = By.xpath("../../..");
	private static final CssMatcher ACTIVE_TAB_MATCHER = new CssMatcher.ClassName("active");

	private final WaitHelper helper = getWaitHelper();
	private final LazyWebElement root = new LazyWebElement(getBrowser(), CenterContentSteps.ROOT_LOCATOR);
	private final LazyReference<Map<String, WebElement>> tabs = new LazyReference<>(() -> {
		Map<String, WebElement> map = new HashMap<>();
		List<WebElement> l = this.root.findElements(CenterContentSteps.TAB_LOCATOR);
		if (l != null) {
			for (WebElement e : l) {
				map.put(e.getText(), e);
			}
		}
		return Collections.unmodifiableMap(map);
	});

	@BeforeStory
	protected void resetState() {
		this.root.reset();
		this.tabs.reset();
	}

	private WebElement tab(String name) {
		return tab(name, true);
	}

	private WebElement tab(String name, boolean required) {
		WebElement tab = this.tabs.get().get(name);
		if (required && (tab == null)) {
			throw new NoSuchElementException(
				"No center tab named [" + name + "] was found - only " + this.tabs.get().keySet() + " are available");
		}
		return tab;
	}

	@Given("the center $tab tab is visible")
	@Alias("the center $tab tab is displayed")
	public void checkTabIsVisible(@Named("tab") String tab) {
		if (!tab(tab).findElement(CenterContentSteps.TAB_LI_PARENT).isDisplayed()) {
			throw new ElementNotVisibleException("The [" + tab + "] center tab is not visible");
		}
	}

	@Given("the center $tab tab is active")
	@Alias("the center $tab tab is selected")
	public void checkTabIsActive(@Named("tab") String tab) {
		// Find the ancestor, see if it has the active CSS class
		if (!CenterContentSteps.ACTIVE_TAB_MATCHER.test(tab(tab).findElement(CenterContentSteps.TAB_LI_PARENT))) {
			throw new ElementNotVisibleException("The [" + tab + "] center tab is not active");
		}
	}

	@When("activating the center $tab tab")
	@Alias("selecting the center $tab tab")
	public void activateTab(@Named("tab") String tab) {
		WebElement t = tab(tab);
		this.helper.scrollTo(t);
		this.helper.waitForElement(t, WaitType.CLICKABLE);
		t.click();
		this.helper.waitForElement(t, WaitType.VISIBLE);
		checkTabIsActive(tab);
	}

	@Then("activate the center $tab tab")
	@Alias("select the center $tab tab")
	public void activateTab2(@Named("tab") String tab) {
		activateTab(tab);
	}

	/*-
	protected String renderValue(FieldType type, String value) {
		// Is this value a rendered value?
		// Syntax will be: $gen(type[,param1,param2,...,paramN])
		// TODO: Add code to detect if we want to render a random string,
		// etc...
		return value;
	}
	
	private void setFieldValue(String name, String value) {
		WebElement field = null; // section.getField(name);
		if (field == null) {
			throw new NoSuchElementException("No field named [" + name + "] in section [" + section.getName()
				+ "] from tab [" + section.getTab().getName() + "]");
		}
		field.setValue(renderValue(field.getType(), value));
	}
	
	private void setFieldValues(ExamplesTable values) {
		int rowNumber = 0;
		for (Map<String, String> row : values.getRows()) {
			rowNumber++;
			String field = row.get("name");
			if (field == null) {
				// No field name... warn and skip? Or explode?
				throw new IllegalArgumentException("No field name given for row # " + rowNumber + " = " + row);
			}
			String value = row.get("value");
			setFieldValue(section, field, value);
		}
	}
	
	@When("setting [$field] field to [$value]")
	@Alias("setting $field field to $value")
	public void fillInField(@Named("field") String field, @Named("value") String value) {
		setFieldValue(field, value);
	}
	
	@Then("set the [$field] field to [$value]")
	@Alias("set the $field field to $value")
	public void fillInField2(@Named("field") String field, @Named("value") String value) {
		setFieldValue(field, value);
	}
	
	@When("populating the fields with: $values")
	@Aliases(values = {
		"filling the fields with: $values", //
		"filling in the fields with: $values", //
	})
	public void fillInFields(@Named("values") ExamplesTable values) {
		setFieldValues(values);
	}
	
	@When("clearing the $field field")
	@Then("clear the $field field")
	public void clearField(@Named("field") String field) {
		setFieldValue(field, null);
	}
	
	@When("clearing the $field field")
	@Then("clear the $field field")
	public void clearField(@Named("field") String field) {
		clearField(null, field);
	}
	
	@When("clearing the $section fields: $fields")
	@Then("clear the $section fields: $fields")
	public void clearFields(@Named("section") String section, @Named("fields") List<String> fields) {
		Live.Section s = section(section);
		if ((fields != null) && !fields.isEmpty()) {
			for (String f : fields) {
				setFieldValue(s, f, null);
			}
		}
	}
	
	@When("clearing the fields: $fields")
	@Then("clear the fields: $fields")
	public void clearFields(@Named("fields") List<String> fields) {
		clearFields(null, fields);
	}
	
	@When("clearing all the fields")
	@Then("clear all the fields")
	public void clearAllFields() {
	}
	
	@Then("add a note with the content [$note]")
	public void addNote(@Named("note") String note) {
	
	}
	*/
}