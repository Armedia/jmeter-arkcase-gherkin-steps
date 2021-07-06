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

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.jbehave.core.annotations.Alias;
import org.jbehave.core.annotations.BeforeStory;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Named;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import com.arkcase.sim.components.AngularHelper;
import com.arkcase.sim.components.WebDriverHelper.WaitType;
import com.arkcase.sim.components.html.WaitHelper;
import com.arkcase.sim.tools.CssMatcher;

public class QueueSteps extends ComponentSteps {

	private static final By QUEUE_LIST = By.cssSelector(
		"div.module-content div.left-sidebar-sm div[ng-controller=\"Queues.QueuesListController\"] ul.nav-pills");
	private static final By QUEUE_ENTRIES = By.tagName("li");
	private static final By QUEUE_ENTRY_LINK = By.tagName("a");
	private static final CssMatcher QUEUE_ACTIVE = new CssMatcher.ClassName("active");

	private static final By QUEUE_CONTENTS = By.cssSelector(
		"div.module-content div.content-body div[ng-controller=\"Queues.OrdersListController\"] div.panel-body");

	private class RequestQueue {
		private final String name;
		private final WebElement entry;
		private final WebElement link;

		public RequestQueue(WebElement entry, WebElement link) {
			this.entry = entry;
			this.link = link;
			this.name = normalize(link.getAttribute("tooltip"));
		}

		public boolean isActive() {
			return QueueSteps.QUEUE_ACTIVE.test(this.entry);
		}

		public void click() {
			getWaitHelper().scrollTo(this.link);
			this.link.click();
		}
	}

	private WebElement queueList = null;
	private Map<String, RequestQueue> queues = null;
	private AngularTable table = null;

	@BeforeStory
	protected void beforeStory() {
		this.queues = null;
		this.table = null;
	}

	private WebElement queueList() {
		return queueList(null);
	}

	private WebElement queueList(WaitType wait) {
		if (this.queueList == null) {
			WaitHelper wh = getWaitHelper();
			if (wait != null) {
				this.queueList = wh.waitForElement(QueueSteps.QUEUE_LIST, wait);
			} else {
				this.queueList = wh.findElement(QueueSteps.QUEUE_LIST);
			}
		}
		return this.queueList;
	}

	private RequestQueue queue(String name) {
		return queue(name, null);
	}

	private RequestQueue queue(String name, WaitType wait) {
		if (this.queues == null) {
			Map<String, RequestQueue> queues = new LinkedHashMap<>();
			for (WebElement entry : queueList(wait).findElements(QueueSteps.QUEUE_ENTRIES)) {
				WebElement link = entry.findElement(QueueSteps.QUEUE_ENTRY_LINK);
				RequestQueue queue = new RequestQueue(entry, link);
				queues.put(queue.name, queue);
			}
			this.queues = queues;
		}
		RequestQueue queue = this.queues.get(normalize(name));
		if (queue == null) {
			throw new NoSuchElementException(
				"No queue named [" + name + "] was found (valid = " + this.queues.keySet() + ")");
		}
		return queue;
	}

	private String normalize(String str) {
		if (str == null) { return str; }
		str = StringUtils.strip(StringUtils.lowerCase(str));
		str = str.replaceAll("\\s+", " ");
		return str;
	}

	@Given("the queue list is ready")
	public void queueListIsReady() {
		if (!queueList().isDisplayed()) { throw new IllegalStateException("The queue list is not ready"); }
	}

	@Then("wait for the queue list")
	public void waitForQueueList() {
		queueList(WaitType.VISIBLE);
	}

	@Then("select the $queue queue")
	@When("selecting the $queue queue")
	public void selectQueue(@Named("queue") String queue) {
		RequestQueue q = queue(queue);
		// If it's already the queue we want, we simply move on
		if (q.isActive()) { return; }
		// Not the right queue, click the right one...
		q.click();
	}

	@Given("the $queue queue is active")
	@Alias("the $queue queue is selected")
	public void givenQueueIsActive(@Named("queue") String queue) {
		if (!queue(queue).isActive()) {
			throw new IllegalStateException(String.format("The [%s] queue is not selected when it should be", queue));
		}
	}

	@Given("the $queue queue is not active")
	@Alias("the $queue queue is not selected")
	public void givenQueueIsNotActive(@Named("queue") String queue) {
		if (queue(queue).isActive()) {
			throw new IllegalStateException(String.format("The [%s] queue is selected when it shouldn't be", queue));
		}
	}

	@Then("wait for the queue to be ready")
	@Alias("wait for the queue to load")
	public void waitForQueue() {
		if (this.table == null) {
			AngularHelper wh = getAngularHelper();
			this.table = new AngularTable(wh, wh.waitForElement(QueueSteps.QUEUE_CONTENTS, WaitType.VISIBLE));
		}
		this.table.waitUntilVisible();
	}

	@Then("sort by $title in $order order")
	public void sortTable(@Named("title") String title, @Named("order") String order) {
		Boolean o = null;
		order = StringUtils.lowerCase(StringUtils.strip(order));
		if (order != null) {
			if ("ascending".startsWith(order)) {
				o = Boolean.TRUE;
			} else if ("descending".startsWith(order)) {
				o = Boolean.FALSE;
			} else {
				// BAD VALUE!
				throw new IllegalArgumentException(String.format(
					"Unknown sort order [%s] - only ASCENDING and DESCENDING (case-insensitive) are allowed", order));
			}
		}

		this.table.sortByColumn(title, o);
	}

	@Then("remove the sort by $title")
	@Alias("clear the sort by $title")
	public void clearTableSort(@Named("title") String title) {
		sortTable(title, null);
	}
}