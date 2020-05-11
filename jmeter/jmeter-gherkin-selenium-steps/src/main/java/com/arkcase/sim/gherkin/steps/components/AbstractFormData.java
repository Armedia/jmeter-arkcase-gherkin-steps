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
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import org.codehaus.plexus.util.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.pagefactory.ByChained;
import org.openqa.selenium.support.ui.Select;

import com.arkcase.sim.components.WebDriverHelper.WaitType;
import com.arkcase.sim.components.html.WaitHelper;
import com.arkcase.sim.gherkin.steps.components.AbstractFormData.Persistent.Tab;
import com.arkcase.sim.tools.CssClassMatcher;
import com.arkcase.sim.tools.JSON;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;

public class AbstractFormData extends ComponentSteps {

	private static final Set<String> TRUE;
	static {
		Set<String> t = new HashSet<>();
		String[] s = {
			"active", //
			"checked", //
			"marked", //
			"on", //
			"selected", //
			"set", //
			"ticked", //
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

	@FunctionalInterface
	private interface FieldValueSetter {
		public boolean set(WebElement element, Persistent.Field fieldDef, String value);
	}

	/**
	 * <p>
	 * Returns {@code true} if the string is any of the following (case-insensitive):
	 * </p>
	 * <ul>
	 * <li>active</li>
	 * <li>checked</li>
	 * <li>marked</li>
	 * <li>on</li>
	 * <li>selected</li>
	 * <li>set</li>
	 * <li>ticked</li>
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

	protected static boolean selectItem(WebElement element, Persistent.Field field, String string) {
		if (AbstractFormData.isTrue(string)) {
			element.sendKeys(" ");
		}
		return true;
	}

	protected static boolean checkItem(WebElement element, Persistent.Field field, String string) {
		if (AbstractFormData.isTrue(string) != element.isSelected()) {
			element.sendKeys(" ");
		}
		return true;
	}

	protected static boolean selectOption(WebElement element, Persistent.Field field, String option) {
		new Select(element).selectByVisibleText(option);
		return true;
	}

	protected static boolean setString(WebElement element, Persistent.Field field, String string) {
		element.clear();
		if (StringUtils.isNotEmpty(string)) {
			element.sendKeys(string);
		}
		return true;
	}

	public enum FieldType {
		//
		// These are applied via setText()
		TEXT(AbstractFormData::setString), //
		PASSWORD(AbstractFormData::setString), //
		TEXTAREA(AbstractFormData::setString), //
		EMAIL(AbstractFormData::setString), //

		// These are applied via "setSelected()"
		RADIO(AbstractFormData::selectItem), //
		CHECKBOX(AbstractFormData::checkItem), //

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

		private final FieldValueSetter setter;

		private FieldType() {
			this(null);
		}

		private FieldType(FieldValueSetter setter) {
			this.setter = setter;
		}

		@JsonValue
		public final String jsonValue() {
			return name().toLowerCase();
		}

		public final boolean apply(WebElement element, Persistent.Field field, String value) {
			Objects.requireNonNull(element, "Must provide a WebElement to apply the value to");
			Objects.requireNonNull(field, "Must provide the Field definition");
			if (this.setter == null) {
				throw new UnsupportedOperationException(String
					.format("Can't apply the value [%s] to an element of fieldType %s (%s)", value, name(), element));
			}
			if (!element.isEnabled()) { return false; }
			return this.setter.set(element, field, value);
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

	public static class Persistent {

		public static abstract class Container {

			public final String name;
			public final By body;
			public final By title;

			private Container(String name, String body, String title) {
				this.name = name;
				this.body = By.cssSelector(body);
				this.title = By.cssSelector(title);
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

		public static class Section extends Container {
			@JsonProperty("fields")
			private final Map<String, Field> fields;

			public Section( //
				@JsonProperty("name") String name, //
				@JsonProperty("body") String body, //
				@JsonProperty("title") String title, //
				@JsonProperty("source") String source, //
				@JsonProperty("fields") Map<String, Field> fields //
			) {
				super(name, body, title);
				this.fields = Collections.unmodifiableMap(fields);
			}
		}

		public static class Tab extends Container {
			@JsonProperty("sections")
			private final Map<String, Section> sections;

			public Tab( //
				@JsonProperty("name") String name, //
				@JsonProperty("body") String body, //
				@JsonProperty("title") String title, //
				@JsonProperty("sections") Map<String, Section> sections //
			) {
				super(name, body, title);
				if ((sections != null) && !sections.isEmpty()) {
					this.sections = Collections.unmodifiableMap(new LinkedHashMap<>(sections));
				} else {
					this.sections = Collections.emptyMap();
				}
			}
		}
	}

	private static MapType buildMapType(ObjectMapper mapper) {
		return mapper.getTypeFactory().constructMapType(LinkedHashMap.class, String.class, Tab.class);
	}

	protected static Map<String, Tab> loadTabs(String resource) throws IOException {
		final Map<String, Tab> tabs = JSON.unmarshal(AbstractFormData::buildMapType, resource);
		if ((tabs == null) || tabs.isEmpty()) { return Collections.emptyMap(); }
		return Collections.unmodifiableMap(tabs);
	}

	protected static Map<String, Tab> loadTabs(Reader r) throws IOException {
		final Map<String, Tab> tabs = JSON.unmarshal(AbstractFormData::buildMapType, r);
		if ((tabs == null) || tabs.isEmpty()) { return Collections.emptyMap(); }
		return Collections.unmodifiableMap(tabs);
	}

	protected static Map<String, Tab> loadTabs(InputStream in) throws IOException {
		return AbstractFormData.loadTabs(in, null);
	}

	protected static Map<String, Tab> loadTabs(InputStream in, Charset c) throws IOException {
		final Map<String, Tab> tabs = JSON.unmarshal(AbstractFormData::buildMapType, in, c);
		if ((tabs == null) || tabs.isEmpty()) { return Collections.emptyMap(); }
		return Collections.unmodifiableMap(tabs);
	}

	public static class Live {
		private static class Element {
			protected final WaitHelper helper;

			private Element(WaitHelper helper) {
				this.helper = Objects.requireNonNull(helper, "Must provide a WaitHelper instance");
			}

			private Element(Element element) {
				this.helper = Objects.requireNonNull(element,
					"Must provide a non-null Element from which to extract the WaitHelper").helper;
			}

			public WaitHelper getHelper() {
				return this.helper;
			}

			protected final WebElement getElement(By by) {
				return getElement(by, null);
			}

			protected final WebElement getElement(By by, WaitType wait) {
				return (wait != null ? this.helper.waitForElement(by, wait) : this.helper.findElement(by));
			}
		}

		public static final class Field extends Element {
			private final Section section;
			private final Persistent.Field field;
			private final WebElement element;

			private Field(Section section, Persistent.Field field) {
				super(section);
				this.field = Objects.requireNonNull(field, "Must provide a Persistent.Field to wrap around");
				this.section = section;

				this.element = section.body.findElement(field.locator);
			}

			public Section getSection() {
				return this.section;
			}

			public String getName() {
				return this.field.label;
			}

			public FieldType getType() {
				return this.field.fieldType;
			}

			public WebElement getElement() {
				return this.element;
			}

			public boolean waitUntil(WaitType type) {
				return this.helper.waitForElement(this.element, type);
			}

			public FieldType getFieldType() {
				return this.field.fieldType;
			}

			public LocatorType getLocatorType() {
				return this.field.locatorType;
			}

			public String getValue() {
				return this.field.value;
			}

			public Set<String> getOptions() {
				return this.field.options;
			}

			public void setValue(String value) {
				// Wait until the field is visible and enabled
				this.helper.scrollTo(this.element);
				waitUntil(WaitType.ENABLED);
				this.field.fieldType.apply(this.element, this.field, value);
			}
		}

		public static final class Section extends Element {
			private static final CssClassMatcher COLLAPSED = new CssClassMatcher("collapse");

			private final Tab tab;
			private final Persistent.Section section;
			private final WebElement title;
			private final WebElement body;

			private final Map<String, Live.Field> fields = new HashMap<>();

			private Section(Tab tab, Persistent.Section section) {
				super(tab);
				this.section = Objects.requireNonNull(section, "Must provide a Persistent.Section to wrap around");
				this.tab = tab;

				this.title = tab.body.findElement(section.title);
				this.body = tab.body.findElement(section.body);
			}

			public Live.Tab getTab() {
				return this.tab;
			}

			public String getName() {
				return this.section.name;
			}

			public WebElement getTitle() {
				return this.title;
			}

			public boolean waitUntilTitle(WaitType type) {
				return this.helper.waitForElement(this.title, type);
			}

			public WebElement getBody() {
				return this.body;
			}

			public boolean waitUntilBody(WaitType type) {
				return this.helper.waitForElement(this.body, type);
			}

			public boolean isExpanded() {
				return !Section.COLLAPSED.test(this.body);
			}

			public boolean isCollapsed() {
				return !isExpanded();
			}

			public void expand() {
				if (isExpanded()) { return; }
				this.helper.scrollTo(this.title);
				waitUntilTitle(WaitType.CLICKABLE);
				this.title.click();
				waitUntilBody(WaitType.VISIBLE);
			}

			public void collapse() {
				if (isCollapsed()) { return; }
				this.helper.scrollTo(this.title);
				waitUntilTitle(WaitType.CLICKABLE);
				this.title.click();
				waitUntilBody(WaitType.HIDDEN);
			}

			public boolean toggle() {
				boolean state = isExpanded();
				if (state) {
					collapse();
				} else {
					expand();
				}
				return !state;
			}

			public boolean hasField(String field) {
				return this.section.fields.containsKey(field);
			}

			public Live.Field getField(String field) {
				return this.fields.computeIfAbsent(field, (f) -> {
					Persistent.Field pf = this.section.fields.get(f);
					if (pf == null) { return null; }
					return new Live.Field(this, pf);
				});
			}

			public Set<String> getFieldNames() {
				return this.section.fields.keySet();
			}

			public int getFieldCount() {
				return this.section.fields.size();
			}
		}

		public static final class Tab extends Element {
			private static final CssClassMatcher SELECTED = new CssClassMatcher("active");
			private static final By PRECEDING_SIBLING = By.xpath("preceding-sibling::div");
			private static final By BTN_EXPAND = new ByChained( //
				Tab.PRECEDING_SIBLING, //
				By.cssSelector("i.fa.fa-expand") //
			);
			private static final By BTN_COMPRESS = new ByChained( //
				Tab.PRECEDING_SIBLING, //
				By.cssSelector("i.fa.fa-compress") //
			);

			private final Persistent.Tab tab;
			private final WebElement expand;
			private final WebElement collapse;
			private final WebElement title;
			private final WebElement body;

			private final Map<String, Live.Section> sections = new HashMap<>();

			private Tab(WaitHelper helper, Persistent.Tab tab) {
				this(helper, null, tab);
			}

			private Tab(WaitHelper helper, WebElement root, Persistent.Tab tab) {
				super(helper);
				this.tab = Objects.requireNonNull(tab, "Must provide a Persistent.Tab to wrap around");
				if (root != null) {
					this.title = root.findElement(tab.title);
					this.body = root.findElement(tab.body);
				} else {
					this.title = getElement(tab.title, WaitType.PRESENT);
					this.body = getElement(tab.body, WaitType.PRESENT);
				}
				this.expand = this.body.findElement(Tab.BTN_EXPAND);
				this.collapse = this.body.findElement(Tab.BTN_COMPRESS);
			}

			public String getName() {
				return this.tab.name;
			}

			public WebElement getTitle() {
				return this.title;
			}

			public boolean waitUntilTitle(WaitType type) {
				return this.helper.waitForElement(this.title, type);
			}

			public WebElement getBody() {
				return this.body;
			}

			public boolean waitUntilBody(WaitType type) {
				return this.helper.waitForElement(this.body, type);
			}

			public void expandAll() {
				this.expand.click();
			}

			public void collapseAll() {
				this.collapse.click();
			}

			public void select() {
				if (isSelected()) { return; }
				this.helper.scrollTo(this.title);
				this.helper.waitForElement(this.title, WaitType.CLICKABLE);
				this.title.click();
				this.helper.waitForElement(this.body, WaitType.VISIBLE);
			}

			public boolean isSelected() {
				return Tab.SELECTED.test(this.title);
			}

			public boolean hasSection(String section) {
				return this.tab.sections.containsKey(section);
			}

			public Live.Section getSection(String section) {
				return this.sections.computeIfAbsent(section, (s) -> {
					Persistent.Section ps = this.tab.sections.get(section);
					if (ps == null) { return null; }
					return new Live.Section(this, ps);
				});
			}

			public Set<String> getSectionNames() {
				return this.tab.sections.keySet();
			}

			public int getSectionCount() {
				return this.tab.sections.size();
			}
		}
	}

	private final Map<String, Persistent.Tab> tabs;

	protected AbstractFormData(Map<String, Persistent.Tab> tabs) {
		this.tabs = Objects.requireNonNull(tabs, "Must provide the tabs' structure");
	}

	protected AbstractFormData(String resource) throws IOException {
		this.tabs = AbstractFormData.loadTabs(resource);
	}

	protected AbstractFormData(InputStream resource) throws IOException {
		this(resource, null);
	}

	protected AbstractFormData(InputStream resource, Charset charset) throws IOException {
		this.tabs = AbstractFormData.loadTabs(resource, charset);
	}

	protected AbstractFormData(Reader resource) throws IOException {
		this.tabs = AbstractFormData.loadTabs(resource);
	}

	protected final Live.Tab getTab(String name) {
		return getTab(name, null);
	}

	protected final Live.Tab getTab(String name, WebElement container) {
		Persistent.Tab tab = this.tabs.get(name);
		if (tab == null) { return null; }
		return new Live.Tab(getWaitHelper(), container, tab);
	}
}