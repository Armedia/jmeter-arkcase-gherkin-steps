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

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiPredicate;

import org.codehaus.plexus.util.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.arkcase.sim.components.WebDriverHelper.WaitType;
import com.arkcase.sim.components.html.WaitHelper;

public class AbstractFormData extends ComponentSteps {

	private static final Set<String> TRUE;
	static {
		Set<String> t = new HashSet<>();
		String[] s = {
			"active", //
			"marked", //
			"on", //
			"selected", //
			"set", //
			"true", //
			"yes", //
		};
		for (String S : s) {
			S = StringUtils.lowerCase(StringUtils.trim(S));
			if (!StringUtils.isEmpty(S)) {
				t.add(S);
			}
		}
		TRUE = Collections.unmodifiableSet(t);
	}

	/**
	 * <p>
	 * Returns {@code true} if the string is any of the following (case-insensitive):
	 * </p>
	 * <ul>
	 * <li>active</li>
	 * <li>marked</li>
	 * <li>on</li>
	 * <li>selected</li>
	 * <li>set</li>
	 * <li>true</li>
	 * <li>yes</li>
	 * </ul>
	 *
	 * @param str
	 * @return
	 */
	private static boolean isTrueEquivalent(String str) {
		return AbstractFormData.TRUE.contains(StringUtils.lowerCase(StringUtils.trim(str)));
	}

	protected static boolean selectItem(WebElement element, String string) {
		if (AbstractFormData.isTrueEquivalent(string)) {
			element.click();
		}
		return true;
	}

	protected static boolean applyKeystrokes(WebElement element, String string) {
		element.sendKeys(string);
		return true;
	}

	public enum FieldType {
		//
		// These are applied via setText()
		TEXT(AbstractFormData::applyKeystrokes), //
		PASSWORD(AbstractFormData::applyKeystrokes), //
		TEXTAREA(AbstractFormData::applyKeystrokes), //

		// These are applied via "setSelected()"
		RADIO(AbstractFormData::selectItem), //
		CHECKBOX(AbstractFormData::selectItem), //

		// Find the child "option" with the correct name, then click() it
		SELECT() {
		}, //

		// These will be ignored (or error out?)
		FILE, //
		IMAGE, //
		RESET, //
		BUTTON, //
		SUBMIT, //
		HIDDEN, //
		//
		;

		private final BiPredicate<WebElement, String> impl;

		private FieldType() {
			this(null);
		}

		private FieldType(BiPredicate<WebElement, String> impl) {
			this.impl = impl;
		}

		public final boolean apply(WebElement element, String value) {
			Objects.requireNonNull(element, "Must provide a WebElement to apply the value to");
			if (this.impl == null) {
				throw new UnsupportedOperationException(String
					.format("Can't apply the value [%s] to an element of fieldType %s (%s)", value, name(), element));
			}
			if (!element.isEnabled()) { return false; }
			return this.impl.test(element, value);
		}
	}

	protected static class Container {

		public final String name;
		public final By title;
		public final By body;

		protected Container(String name, By title, By body) {
			this.name = name;
			this.title = title;
			this.body = body;
		}

		private WebElement getElement(WaitHelper wh, By by, WaitType wait) {
			return wh.waitForElement(by, wait);
		}

		public WebElement activate(WaitHelper wh) {
			getTitle(wh, WaitType.CLICKABLE).click();
			return getBody(wh, WaitType.VISIBLE);
		}

		public WebElement getTitle(WaitHelper wh) {
			return getTitle(wh, null);
		}

		public WebElement getTitle(WaitHelper wh, WaitType wait) {
			return getElement(wh, this.title, wait);
		}

		public WebElement getBody(WaitHelper wh) {
			return getBody(wh, null);
		}

		public WebElement getBody(WaitHelper wh, WaitType wait) {
			return getElement(wh, this.body, wait);
		}
	}

	public static class Field {

		public final String label;
		public final By selector;
		public final FieldType fieldType;

		public Field(String label, By selector, FieldType fieldType) {
			this.label = label;
			this.selector = selector;
			this.fieldType = fieldType;
		}
	}

	public static class FieldGroup extends Container {
		private final Map<String, Field> fields;

		public FieldGroup(String name, By title, By body, Map<String, Field> fields) {
			super(name, title, body);
			this.fields = fields;
		}

		public boolean hasField(String name) {
			return this.fields.containsKey(name);
		}

		public Field getField(String name) {
			return this.fields.get(name);
		}

		public int getFieldCount() {
			return this.fields.size();
		}
	}

	public static class FieldGroupContainer extends Container {
		public final By expandButton;
		private final Map<String, FieldGroup> fieldGroups;

		public FieldGroupContainer(String name, By title, By body, By expandButton,
			Map<String, FieldGroup> fieldGroups) {
			super(name, title, body);
			this.expandButton = expandButton;
			this.fieldGroups = fieldGroups;
		}

		public boolean hasSection(String name) {
			return this.fieldGroups.containsKey(name);
		}

		public FieldGroup getSection(String section) {
			return this.fieldGroups.get(section);
		}

		public int getSectionCount() {
			return this.fieldGroups.size();
		}
	}
}