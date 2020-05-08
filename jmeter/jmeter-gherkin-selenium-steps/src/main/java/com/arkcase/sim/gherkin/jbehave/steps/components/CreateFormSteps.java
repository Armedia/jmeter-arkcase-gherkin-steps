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

import java.util.Map;

import org.jbehave.core.annotations.Alias;
import org.jbehave.core.annotations.BeforeStory;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Named;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.jbehave.core.model.ExamplesTable;
import org.openqa.selenium.WebElement;

public class CreateFormSteps extends CreateFormData {

	private String currentTab = null;

	@BeforeStory
	protected void resetState() {
		this.currentTab = null;
	}

	@Given("the $tab tab is active")
	public void checkTabIsActive(@Named("tab") String tab) {
		// TODO:
	}

	@When("activating the $tab tab")
	@Then("activate the $tab tab")
	public void activateTab(@Named("tab") String tab) {
		// TODO:
	}

	private void setFieldValue(String sectionName, WebElement section, String label, String value) {
		WebElement field = findField(section, label);
		if (field == null) {
			throw new IllegalArgumentException(
				"No field named [" + label + "] was found in section [" + sectionName + "]");
		}

		// What fieldType of field is it? Set the value accordingly
		// TODO: Convert the value to the field's required format, and set it
		// TODO: Explode if the value is not convertable
	}

	@Then("populate the $section section with: $values")
	public void fillInFields(@Named("section") String section, @Named("values") ExamplesTable values) {
		WebElement container = findAndActivateSection(this.currentTab, section);
		final int rows = values.getRowCount();
		for (int i = 0; i < rows; i++) {
			Map<String, String> row = values.getRow(i);
			setFieldValue(section, container, row.get("name"), row.get("value"));
		}
	}

	@Then("set the $section field $field to $value")
	@Alias("set the $section field $field to [$value]")
	public void fillInField(@Named("section") String section, @Named("field") String field,
		@Named("value") String value) {
		setFieldValue(section, findSection(this.currentTab, section), field, value);
	}
}