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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Function;

import org.codehaus.plexus.util.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import com.arkcase.sim.components.WebDriverHelper.WaitType;
import com.arkcase.sim.components.html.WaitHelper;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

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
	private static boolean isTrue(String str) {
		return AbstractFormData.TRUE.contains(StringUtils.lowerCase(StringUtils.trim(str)));
	}

	protected static boolean selectItem(WebElement element, String string) {
		if (AbstractFormData.isTrue(string)) {
			element.click();
		}
		return true;
	}

	protected static boolean selectOption(WebElement element, String option) {
		new Select(element).selectByVisibleText(option);
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
		EMAIL(AbstractFormData::applyKeystrokes), //

		// These are applied via "setSelected()"
		RADIO(AbstractFormData::selectItem), //
		CHECKBOX(AbstractFormData::selectItem), //

		// Find the child "option" with the correct name, then click() it
		SELECT(AbstractFormData::selectOption), //

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

		@JsonValue
		public final String jsonValue() {
			return name().toLowerCase();
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

		public static final FieldType parse(String type) {
			if (type == null) { return null; }
			return FieldType.valueOf(StringUtils.upperCase(type));
		}
	}

	public enum LocatorType {
		//
		CLASS(By::className), //
		CSS(By::cssSelector), //
		ID(By::id), //
		LINKTEXT(By::linkText), //
		NAME(By::name), //
		PARTIALLINKTEXT(By::partialLinkText), //
		TAGNAME(By::tagName), //
		XPATH(By::xpath), //
		;

		@JsonValue
		public final String jsonValue() {
			return name().toLowerCase();
		}

		private final Function<String, By> builder;

		public final By build(String str) {
			return this.builder.apply(str);
		}

		private LocatorType(Function<String, By> builder) {
			this.builder = Objects.requireNonNull(builder, "Must provide a builder function");
		}

		public static final LocatorType parse(String type) {
			if (type == null) { return null; }
			return LocatorType.valueOf(StringUtils.upperCase(type));
		}
	}

	protected static class Container {

		public final String name;
		public final By body;
		public final By title;

		protected Container(String name, String body, String title) {
			this.name = name;
			this.body = By.cssSelector(body);
			this.title = By.cssSelector(title);
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
		@JsonProperty("name")
		public final String label;

		@JsonProperty("type")
		public final FieldType fieldType;

		@JsonProperty("locatorType")
		public final LocatorType locatorType;

		@JsonProperty("locator")
		private final String locatorStr;

		@JsonIgnore
		public final By locator;

		@JsonProperty("value")
		public final String value;

		@JsonProperty("options")
		public final Set<String> options;

		@JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
		public Field( //
			@JsonProperty("name") String name, //
			@JsonProperty("type") String type, //
			@JsonProperty("locator") String locator, //
			@JsonProperty("locatorType") String locatorType, //
			@JsonProperty("value") String value, //
			@JsonProperty("options") Collection<String> options //
		) {
			this.label = name;
			this.fieldType = FieldType.parse(type);
			this.locatorStr = locator;
			this.locatorType = LocatorType.parse(locatorType);
			this.locator = this.locatorType.builder.apply(this.locatorStr);
			this.value = value;
			if ((options != null) && !options.isEmpty()) {
				this.options = Collections.unmodifiableSet(new LinkedHashSet<>(options));
			} else {
				this.options = Collections.emptySet();
			}
		}
	}

	public static class FormSection extends Container {

		@JsonProperty("fields")
		private final Map<String, Field> fields;

		public FormSection( //
			@JsonProperty("name") String name, //
			@JsonProperty("body") String body, //
			@JsonProperty("title") String title, //
			@JsonProperty("source") String source, //
			@JsonProperty("fields") Map<String, Field> fields //
		) {
			super(name, body, title);
			this.fields = Collections.unmodifiableMap(fields);
		}

		public boolean hasField(String name) {
			return this.fields.containsKey(name);
		}

		public Field getField(String name) {
			return this.fields.get(name);
		}

		public Set<String> getFieldNames() {
			return this.fields.keySet();
		}

		public int getFieldCount() {
			return this.fields.size();
		}
	}

	public static class FormTab extends Container {
		@JsonProperty("sections")
		private final Map<String, FormSection> sections;

		public FormTab( //
			@JsonProperty("name") String name, //
			@JsonProperty("body") String body, //
			@JsonProperty("title") String title, //
			@JsonProperty("forms") Map<String, FormSection> forms //
		) {
			super(name, body, title);
			if ((forms != null) && !forms.isEmpty()) {
				this.sections = Collections.unmodifiableMap(new LinkedHashMap<>(forms));
			} else {
				this.sections = Collections.emptyMap();
			}
		}

		public boolean hasSection(String name) {
			return this.sections.containsKey(name);
		}

		public FormSection getSection(String section) {
			return this.sections.get(section);
		}

		public Set<String> getSectionNames() {
			return this.sections.keySet();
		}

		public int getSectionCount() {
			return this.sections.size();
		}
	}
}
