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

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.jbehave.core.annotations.Alias;
import org.jbehave.core.annotations.Aliases;
import org.jbehave.core.annotations.BeforeStory;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Named;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.jbehave.core.model.ExamplesTable;

public class CreateFormSteps extends AbstractFormData {

	private static final String CREATE_FORM_DEFINITIONS = "createNewOrderForm.json";

	// We do it like this so we only load the data once...
	private static final Map<String, Persistent.Tab> TABS;
	static {
		try {
			TABS = AbstractFormData.loadTabs(CreateFormSteps.CREATE_FORM_DEFINITIONS);
		} catch (IOException e) {
			throw new RuntimeException(
				"Failed to load the form definitions from [" + CreateFormSteps.CREATE_FORM_DEFINITIONS + "]", e);
		}
	}

	private Live.Tab currentTab = null;
	private Live.Section currentSection = null;

	public CreateFormSteps() {
		super(CreateFormSteps.TABS);
	}

	private Live.Tab tab() {
		return tab(null);
	}

	private Live.Tab tab(String name) {
		if (name != null) {
			Live.Tab tab = getTab(name);
			if (tab == null) { throw new RuntimeException("No tab named [" + name + "] was found"); }
			this.currentTab = tab;
		}
		if (this.currentTab == null) { throw new RuntimeException("No tab is currently selected for work!"); }
		return this.currentTab;
	}

	private Live.Section section() {
		return section(null);
	}

	private Live.Section section(String name) {
		if (name != null) {
			Live.Section section = tab().getSection(name);
			if (section == null) { throw new RuntimeException("No section named [" + name + "] was found"); }
			this.currentSection = section;
		}
		if (this.currentSection == null) { throw new RuntimeException("No section is currently selected for work!"); }
		return this.currentSection;
	}

	@BeforeStory
	protected void resetState() {
		this.currentTab = null;
		this.currentSection = null;
	}

	@Given("the $tab tab is active")
	@Alias("the $tab tab is selected")
	public void checkTabIsActive(@Named("tab") String tab) {
		if (!tab(tab).isSelected()) { throw new RuntimeException("The [" + tab + "] tab is not active"); }
	}

	@When("activating the $tab tab")
	@Alias("selecting the $tab tab")
	public void activateTab(@Named("tab") String tab) {
		tab(tab).select();
	}

	@Then("activate the $tab tab")
	@Alias("select the $tab tab")
	public void activateTab2(@Named("tab") String tab) {
		activateTab(tab);
	}

	@Given("the $section section is expanded")
	@Aliases(values = {
		"the $section section is visible", //
		"the $section section is shown", //
	})
	public void checkSectionIsExpanded(@Named("section") String section) {
		if (!section(section).isExpanded()) {
			throw new RuntimeException("The [" + section + "] section is not expanded");
		}
	}

	@Given("the section is expanded")
	@Aliases(values = {
		"the section is visible", //
		"the section is shown", //
	})
	public void checkSectionIsExpanded() {
		checkSectionIsExpanded(null);
	}

	@Given("the $section section is collapsed")
	@Aliases(values = {
		"the $section section is invisible", //
		"the $section section is hidden", //
	})
	public void checkSectionIsCollapsed(@Named("section") String section) {
		if (section(section).isExpanded()) { throw new RuntimeException("The section [" + section + "] is expanded"); }
	}

	@Given("the section is collapsed")
	@Aliases(values = {
		"the section is invisible", //
		"the section is hidden", //
	})
	public void checkSectionIsCollapsed() {
		checkSectionIsCollapsed(null);
	}

	@When("expanding the $section section")
	@Aliases(values = {
		"activating the $section section", //
		"showing the $section section", //
	})
	public void expandSection(@Named("section") String section) {
		section(section).expand();
	}

	@Then("expand the $section section")
	@Aliases(values = {
		"activate the $section section", //
		"show the $section section", //
	})
	public void expandSection2(@Named("section") String section) {
		expandSection(section);
	}

	@Then("expand the section")
	@Aliases(values = {
		"activate the section", //
		"show the section", //
	})
	public void expandSection3() {
		section().expand();
	}

	@When("collapsing the $section section")
	@Alias("hiding the $section section")
	public void collapseSection(@Named("section") String section) {
		section(section).collapse();
	}

	@Then("collapse the $section section")
	@Alias("hide the $section section")
	public void collapseSection2(@Named("section") String section) {
		collapseSection(section);
	}

	@Then("collapse the section")
	@Alias("hide the section")
	public void collapseSection3() {
		section().collapse();
	}

	@When("toggling the $section section")
	@Then("toggle the $section section")
	public void toggleSection(@Named("section") String section) {
		section(section).toggle();
	}

	@When("toggling the section")
	@Then("toggle the section")
	public void toggleSection() {
		section().toggle();
	}

	private void setFieldValue(Live.Section section, String name, String value) {
		Live.Field field = section.getField(name);
		if (field == null) {
			throw new RuntimeException("No field named [" + name + "] in section [" + section.getName() + "] from tab ["
				+ section.getTab().getName() + "]");
		}
		field.setValue(value);
	}

	private void setFieldValues(Live.Section section, ExamplesTable values) {
		final int rows = values.getRowCount();
		for (int i = 0; i < rows; i++) {
			Map<String, String> row = values.getRow(rows);
			String field = row.get("name");
			if (field == null) {
				// No field name... warn and skip? Or explode?
				throw new RuntimeException("No field name given for row # " + i + " = " + row);
			}
			String value = row.get("value");
			setFieldValue(section, field, value);
		}
	}

	@When("populating the section with: $values")
	@Aliases(values = {
		"filling the section with: $values", //
		"filling in the section with: $values", //
	})
	public void fillInFields(@Named("values") ExamplesTable values) {
		setFieldValues(section(), values);
	}

	@Then("populate the section with: $values")
	@Aliases(values = {
		"fill the section with: $values", //
		"fill in the section with: $values", //
	})
	public void fillInFields2(@Named("values") ExamplesTable values) {
		fillInFields(values);
	}

	@When("setting the field $field to $value")
	@Alias("setting the field $field to [$value]")
	public void fillInField(@Named("field") String field, @Named("value") String value) {
		setFieldValue(section(), field, value);
	}

	@Then("set the field $field to $value")
	@Alias("set the field $field to [$value]")
	public void fillInField2(@Named("field") String field, @Named("value") String value) {
		fillInField(field, value);
	}

	@When("populating the $section section with: $values")
	@Aliases(values = {
		"filling the $section section with: $values", //
		"filling in the $section section with: $values", //
	})
	public void fillInFields(@Named("section") String section, @Named("values") ExamplesTable values) {
		setFieldValues(section(section), values);
	}

	@Then("populate the $section section with: $values")
	@Aliases(values = {
		"fill the $section section with: $values", //
		"fill in the $section section with: $values", //
	})
	public void fillInFields2(@Named("section") String section, @Named("values") ExamplesTable values) {
		fillInFields(section, values);
	}

	@When("setting the $section field $field to $value")
	@Alias("setting the $section field $field to [$value]")
	public void fillInField(@Named("section") String section, @Named("field") String field,
		@Named("value") String value) {
		setFieldValue(section(section), field, value);
	}

	@Then("set the $section field $field to $value")
	@Alias("set the $section field $field to [$value]")
	public void fillInField2(@Named("section") String section, @Named("field") String field,
		@Named("value") String value) {
		fillInField(section, field, value);
	}

	@When("clearing the $section field $field")
	@Then("clear the $section field $field")
	public void clearField(@Named("section") String section, @Named("field") String field) {
		setFieldValue(section(section), field, null);
	}

	@When("clearing the field $field")
	@Then("clear the field $field")
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

	@When("clearing all the fields in section $section")
	@Then("clear all the fields in section $section")
	public void clearAllFields(@Named("section") String section) {
		Live.Section s = section(section);
		for (String f : s.getFieldNames()) {
			setFieldValue(s, f, null);
		}
	}

	@When("clearing all the fields")
	@Then("clear all the fields")
	public void clearAllFields() {
		clearAllFields(null);
	}
}