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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.jbehave.core.annotations.Alias;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Named;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.pagefactory.ByChained;

import com.arkcase.sim.components.WebDriverHelper.WaitType;
import com.arkcase.sim.components.html.WaitHelper;

public class QueueSteps extends ComponentSteps {

	private static final By ACTIVE_QUEUE = By
		.cssSelector("div.module-content div.left-sidebar-sm ul.nav-pills li.active a");
	private static final By QUEUE_CONTAINER = By.xpath("./ancestor::li");
	private static final By QUEUE_LIST = By.cssSelector("div.module-content div.left-sidebar-sm ul.nav-pills");
	private static final Map<String, By> QUEUES;
	static {
		String[] queueNames = {
			"Transcribe", "Fulfill", "Quality Control", "Billing", "Review", "Pending Resolution"
		};

		Map<String, By> queues = new HashMap<>();
		for (String queue : queueNames) {
			By link = new ByChained(QueueSteps.QUEUE_LIST, By.cssSelector(String.format("a[tooltip=\"%s\"]", queue)));
			queues.put(StringUtils.lowerCase(queue), link);
		}
		QUEUES = Collections.unmodifiableMap(queues);
	}

	private By getQueueLocator(String name) {
		By by = QueueSteps.QUEUES.get(StringUtils.lowerCase(name));
		if (by == null) {
			throw new IllegalArgumentException(
				String.format("No navigation choice with the alias [%s] was found", name));
		}
		return by;
	}

	private WebElement getQueueContainer(String name) {
		return getQueueContainer(name, true);
	}

	private WebElement getQueueContainer(String name, boolean required) {
		try {
			return getQueueLink(name).findElement(QueueSteps.QUEUE_CONTAINER);
		} catch (NoSuchElementException e) {
			if (required) { throw e; }
			return null;
		}
	}

	private WebElement getQueueLink(String name) {
		return getQueueLink(name, true);
	}

	private WebElement getQueueLink(String name, boolean required) {
		By by = getQueueLocator(name);
		try {
			return getWaitHelper().waitForElement(by, WaitType.CLICKABLE);
		} catch (final NoSuchElementException e) {
			if (required) { throw e; }
			return null;
		}
	}

	private static final By SORT_MENU = By
		.cssSelector("div.ui-grid-menu ul.ui-grid-menu-items button.ui-grid-menu-item");

	private enum Order {
		//
		DESCENDING("desc", "down"), //
		ASCENDING("asc", "up"), //
		//
		;

		private final String alias;
		private final By locator;

		private Order(String alias, String s) {
			this.alias = alias;
			this.locator = new ByChained(QueueSteps.SORT_MENU,
				By.cssSelector(String.format("i.ui-grid-icon-sort-alt-%s", s)));
		}

		private static final Map<String, Order> DECODER;
		static {
			Map<String, Order> m = new TreeMap<>();
			for (Order o : Order.values()) {
				Order o2 = m.put(o.name().toLowerCase(), o);
				if (o2 != null) {
					throw new RuntimeException(String.format("Duplicate order mapping for %s: %s and %s matched",
						o.name(), o.name(), o2.name()));
				}
				if (o.alias != null) {
					o2 = m.put(o.alias.toLowerCase(), o);
					if (o2 != null) {
						throw new RuntimeException(
							String.format("Duplicate order alias mapping for %s: %s and %s (%s) matched", o.name(),
								o.alias, o2.name()));
					}
				}
			}
			DECODER = Collections.unmodifiableMap(new LinkedHashMap<>(m));
		}

		public static Order decode(String order) {
			Objects.requireNonNull(order, "Must provide an order string to decode");
			Order o = Order.DECODER.get(order.toLowerCase());
			if (o != null) { return o; }
			throw new IllegalArgumentException(String.format("Unsupported order spec [%s]", order));
		}
	}

	@Given("the queue list is ready")
	@When("the queue list is ready")
	public void queueListIsReady() {
		getWaitHelper().waitForElement(QueueSteps.QUEUE_LIST, WaitType.VISIBLE);
	}

	private String getActiveQueueName() {
		WebElement activeQueue = getWaitHelper().waitForElement(QueueSteps.ACTIVE_QUEUE, WaitType.VISIBLE);
		return activeQueue.getAttribute("tooltip");
	}

	@Then("select the $queue queue")
	@When("selecting the $queue queue")
	public void selectQueue(@Named("queue") String queue) {
		// If it's already the queue we want, we simply move on
		if (StringUtils.equals(queue, getActiveQueueName())) { return; }
		// Not the right queue, click the right one...
		getQueueLink(queue).click();
	}

	private boolean isQueueActive(@Named("queue") String queue) {
		return getCssClasses(getQueueContainer(queue)).contains("active");
	}

	@Given("the $queue queue is active")
	@Alias("the $queue queue is selected")
	public void givenQueueIsActive(@Named("queue") String queue) {
		if (!isQueueActive(queue)) {
			throw new IllegalStateException(String.format("The [%s] queue is not selected when it should be", queue));
		}
	}

	@Given("the $queue queue is not active")
	@Alias("the $queue queue is not selected")
	public void givenQueueIsNotActive(@Named("queue") String queue) {
		if (isQueueActive(queue)) {
			throw new IllegalStateException(String.format("The [%s] queue is selected when it shouldn't be", queue));
		}
	}

	private static final By getSortColumnSelector(String columnName) {
		String format = "div.ui-grid-header div.ui-grid-header-cell div[role=\"button\"][title=\"%s\"] + div[role=\"button\"] i.ui-grid-icon-angle-down";
		return By.cssSelector(String.format(format, columnName.replaceAll("\"", "\\\"")));
	}

	@Then("sort by $title in $order order")
	public void sortTable(@Named("title") String title, @Named("order") String order) {
		Order o = Order.decode(order);
		By headerSelector = QueueSteps.getSortColumnSelector(title);
		WaitHelper wh = getWaitHelper();
		wh.waitForElement(headerSelector, WaitType.CLICKABLE).click();
		wh.waitForElement(o.locator, WaitType.CLICKABLE).click();
	}
}