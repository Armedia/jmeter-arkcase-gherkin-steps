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
package com.arkcase.sim.components.misc;

import org.apache.commons.lang3.StringUtils;

import com.arkcase.sim.tools.TextTools;

public class ValidationHelper {

	public static enum Type {
		//
		field("Field"), //
		dropDown("Drop down"), //
		page("Page"), //
		button("Button"), //
		label("Label"), //
		image("Image"), //
		window("Window"), //
		notification("Notification"), //
		grid("Grid"), //
		menu("Menu"), //
		link("Link"), //
		//
		;

		private final String str;

		private Type(String label) {
			this.str = label;
		}

		@Override
		public String toString() {
			return this.str;
		}
	}

	public static String getOnlyOneRecordShouldBeDisplayed(String type, String title) {
		return TextTools.interpolate("There should only be 1 record displayed in ${0} with title ${1}", type, title);
	}

	public static String getFieldShouldHaveValueValidation(String fieldLabel, String value, String name) {
		return ValidationHelper.getFieldValueValidation(fieldLabel, value, name);
	}

	public static String getFieldShouldNotHaveValueValidation(String fieldLabel, String value) {
		return ValidationHelper.getFieldValueValidation(fieldLabel, value, "not");
	}

	public static String getFieldValueValidation(String fieldLabel, String value, String status) {
		return TextTools.interpolate("${0} ${1} should ${3} have value as ${2}", Type.field, fieldLabel, status, value);
	}

	public static String getNoOptionDisplayed(String fieldLabel) {
		return TextTools.interpolate("${0} ${1} should not have any option displayed", Type.dropDown, fieldLabel);
	}

	public static String getOptionDisplayed(String fieldLabel, String optionLabel) {
		return TextTools.interpolate("${0} ${1} should display option with text ${2}", Type.dropDown, fieldLabel,
			optionLabel);
	}

	public static String getPageDisplayedValidation(String name) {
		return TextTools.interpolate("${0} ${1}", Type.page, ValidationHelper.getDisplayedValidation(name));
	}

	public static String getFieldDisplayedValidation(String name) {
		return TextTools.interpolate("${0} ${1}", Type.field, ValidationHelper.getDisplayedValidation(name));
	}

	public static String getButtonDisplayedValidation(String name) {
		return TextTools.interpolate("${0} ${1}", Type.button, ValidationHelper.getDisplayedValidation(name));
	}

	public static String getButtonDisabledValidation(String name) {
		return TextTools.interpolate("${0} ${1}", Type.button, ValidationHelper.getDisabledValidation(name));
	}

	public static String getMenuDisplayedValidation(String name) {
		return TextTools.interpolate("${0} ${1}", Type.menu, ValidationHelper.getDisplayedValidation(name));
	}

	public static String getMenuShouldNotBeDisplayedValidation(String name) {
		return TextTools.interpolate("${0} ${1}", Type.menu, ValidationHelper.getNotDisplayedValidation(name));
	}

	public static String getMenuExpandedValidation(String name) {
		return TextTools.interpolate("${0} ${1} should be expanded", Type.menu, name);
	}

	public static String getMenuCollapsedValidation(String name) {
		return TextTools.interpolate("${0} ${1} should be shrinked", Type.menu, name);
	}

	public static String getMenuShouldNotHaveChildValidation(String name) {
		return TextTools.interpolate("${0} \"${1}\" should not have children", Type.menu, name);
	}

	public static String getLabelDisplayedValidation(String name) {
		return TextTools.interpolate("${0} '${1}'", Type.label, ValidationHelper.getDisplayedValidation(name));
	}

	public static String getImageDisplayedValidation(String name) {
		return TextTools.interpolate("${0} '${1}'", Type.image, ValidationHelper.getDisplayedValidation(name));
	}

	public static String getGridDisplayedValidation(String name) {
		return TextTools.interpolate("${0} ${1}", Type.grid, ValidationHelper.getDisplayedValidation(name));
	}

	public static String getDeletionConfirmationDisplayedValidation(String recordText) {
		return TextTools.interpolate("Confirmation box for deletion of record which contains ${0}",
			ValidationHelper.getDisplayedValidation(recordText));
	}

	public static String getRecordCreatedValidation(String[] recordText) {
		return ValidationHelper
			.getRecordContainsMessage(ValidationHelper.getDisplayedValidation(StringUtils.join(recordText, ',')));
	}

	public static String getRecordDeletedValidation(String recordText) {
		return ValidationHelper.getRecordContainsMessage(TextTools.interpolate("${0} has been deleted", recordText));
	}

	public static String getRecordContainsMessage(String message) {
		return TextTools.interpolate("Record which contains ${0}", message);
	}

	public static String getDisplayedValidation(String name) {
		return TextTools.interpolate("${0} should be displayed", name);
	}

	public static String getSortedValidation(String order, String name) {
		return TextTools.interpolate("Column ${0} must be sorted in ${1} order", name, order);
	}

	public static String getAscendingSortedValidation(String name) {
		return ValidationHelper.getSortedValidation(name, "ascending");
	}

	public static String getDescendingSortedValidation(String name) {
		return ValidationHelper.getSortedValidation(name, "descending");
	}

	public static String getDisabledValidation(String name) {
		return TextTools.interpolate("${0} should be disabled", name);
	}

	public static String getEnabledValidation(String name) {
		return TextTools.interpolate("${0} should be enabled", name);
	}

	public static String getEnabledButtonValidation(String name) {
		return TextTools.interpolate("${0} should be enabled", name);
	}

	public static String getNotEnabledButtonValidation(String name) {
		return TextTools.interpolate("${0} should not be enabled", name);
	}

	public static String getDisabledButtonValidation(String name) {
		return TextTools.interpolate("${0} should be disabled", name);
	}

	public static String getErrorDisplayedValidation(String error) {
		return TextTools.interpolate("Error ${0}", ValidationHelper.getDisplayedValidation(error));
	}

	public static String getErrorDisplayedValidationForField(String field, String error) {
		return TextTools.interpolate("Error ${0} for field ${1}", ValidationHelper.getDisplayedValidation(error),
			field);
	}

	public static String getWindowShouldNotBeDisplayedValidation(String name) {
		return TextTools.interpolate("${0} ${1}", Type.window, ValidationHelper.getNotDisplayedValidation(name));
	}

	public static String getNotificationDisplayedValidation(String name) {
		return TextTools.interpolate("${0} ${1}", Type.notification, ValidationHelper.getDisplayedValidation(name));
	}

	public static String getHttpStatusCodeValidation(Object statusCode) {
		return TextTools.interpolate("Http response code should be ${0}", statusCode);
	}

	public static String getHttpResponseBodyValidation(String content) {
		return TextTools.interpolate("Http response body should contain ${0}", content);
	}

	public static String getNotDisplayedValidation(String name) {
		return TextTools.interpolate("${0} should not be displayed", name);
	}

	public static String getOnlyOneRecordShouldBeDisplayedInGrid(String name) {
		return ValidationHelper.getOnlyOneRecordShouldBeDisplayed(Type.dropDown.toString(), name);
	}

	public static String getOnlyOneRecordShouldBeDisplayedInDropDown(String name) {
		return ValidationHelper.getOnlyOneRecordShouldBeDisplayed(Type.grid.toString(), name);
	}

	public static String getMessageDisplayedValidation(String msg) {
		return TextTools.interpolate("Message ${0}", ValidationHelper.getDisplayedValidation(msg));
	}

	public static String getLinkDisplayedValidation(String name) {
		return TextTools.interpolate("${0} ${1}", Type.link, ValidationHelper.getDisplayedValidation(name));
	}

	public static String getLinkNotDisplayedValidation(String name) {
		return TextTools.interpolate("${0} ${1}", Type.link, ValidationHelper.getNotDisplayedValidation(name));
	}

	public static String getCheckedValidation(String name) {
		return TextTools.interpolate("${0} should be checked", name);
	}

	public static String getElementDisplayedValidation(String name) {
		return TextTools.interpolate("${0} element should be displayed", name);
	}

	public static String getIconDisplayedValidation(String name) {
		return TextTools.interpolate("Icon ${0}", ValidationHelper.getDisplayedValidation(name));
	}

	public static String getIconNotDisplayedValidation(String name) {
		return TextTools.interpolate("Icon ${0}", ValidationHelper.getNotDisplayedValidation(name));
	}

	public static String getFieldHasValueValidation(String fieldLabel, String value) {
		return TextTools.interpolate("Field ${0} has value as ${1}", fieldLabel, value);
	}

	public static String getFieldDoesNotHaveValueValidation(String fieldLabel, String value) {
		return TextTools.interpolate("Field ${0} does not have value as ${1}", fieldLabel, value);
	}

	public static String getAlertHasMessage(String message) {
		return TextTools.interpolate("Alert box has message ${0}", message);
	}

	public static String getPresentValidation(String name) {
		return TextTools.interpolate("${0} should be present", name);
	}

	public static String getNotPresentValidation(String name) {
		return TextTools.interpolate("${0} should not be present", name);
	}

	public static String getSelectedValidation(String name) {
		return TextTools.interpolate("${0} should be selected", name);
	}

	public static String getUnSelectedValidation(String name) {
		return TextTools.interpolate("${0} should be unselected", name);
	}

	public static String getGreaterThanValidation(Number actualValue, Number expectedValue, String elementName) {
		return TextTools.interpolate("Field name - ${0} : ${1} should be grater than ${2}", elementName, actualValue,
			expectedValue);
	}

	public static String getLessThanOrEqualToValidation(Number actualValue, Number expectedValue, String elementName) {
		return TextTools.interpolate("Field name - ${0} : ${1} should be less than or equal ${2}", elementName,
			actualValue, expectedValue);
	}

	public static String getGreaterThanOrEqualToValidation(Number actualValue, Number expectedValue,
		String elementName) {
		return TextTools.interpolate("Field name - ${0} : ${actualValue} should be greater than or equal ${2}",
			elementName, actualValue, expectedValue);
	}

	public static String getEqualityValidation(String actualValue, String expectedValue, String elementName) {
		return TextTools.interpolate("Field name - ${0} : ${1} should be equal to ${2}", elementName, actualValue,
			expectedValue);
	}

	public static String getInequalityValidation(String actualValue, String expectedValue, String elementName) {
		return TextTools.interpolate("Field name - ${0} : ${1} should be not be equal to ${2}", elementName,
			actualValue, expectedValue);
	}

	public static String getContainsValidation(String actualValue, String expectedValue, String elementName) {
		return TextTools.interpolate("Field name - ${0} : ${1} should contain ${2}", elementName, actualValue,
			expectedValue);
	}

	public static String getNotContainsValidation(String actualValue, String expectedValue, String elementName) {
		return TextTools.interpolate("Field name - ${0} : ${1} should not contain ${2}", elementName, actualValue,
			expectedValue);
	}

}
