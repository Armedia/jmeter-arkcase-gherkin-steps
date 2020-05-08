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
package com.arkcase.sim.components;

import java.time.Duration;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;

public class AngularHelper extends WebDriverHelper {

	private static final ThreadLocal<Boolean> ENABLED = ThreadLocal.withInitial(() -> Boolean.TRUE);

	public static void reset() {
		AngularHelper.ENABLED.remove();
	}

	private static final String IS_PRESENT_JS = //
		"return (window.angular !== undefined) || (window.getAllAngularTestabilities !== undefined);";

	private static final ExpectedCondition<Boolean> IS_PRESENT = AngularHelper::isAngularPresent;

	private static final String IS_STABLE_JS = //
		"" + //
			"if (window.getAllAngularTestabilities !== undefined) {" + //
			// If this is a new version of AngularJS...
			"	 var testabilities = window.getAllAngularTestabilities();" + //
			"	 var index = 0;" + // How to get the correct index when there's more than 1?
			"	 return testabilities[index].isStable();" + //
			"} else if (window.angular !== undefined) {" + //
			// If this is an older version of AngularJS...
			"	 var injector = window.angular.element('body').injector();" + //
			"	 var $http = injector.get('$http');" + //
			"	 return ($http.pendingRequests.length === 0);" + //
			"} else {" + //
			// If there is no known AngularJS present...
			"    return null;" + //
			"}";

	private static final ExpectedCondition<Boolean> IS_STABLE = (d) -> {
		Boolean ret = AngularHelper.isAngularStable(d);
		if (ret != null) { return ret; }
		throw new RuntimeException("AngularJS is not present");
	};

	public static final boolean isAngularPresent(WebDriver d) {
		return Boolean.class.cast(WebDriverHelper.runJavaScript(d, AngularHelper.IS_PRESENT_JS));
	}

	public static final Boolean isAngularStable(WebDriver d) {
		Object ret = WebDriverHelper.runJavaScript(d, AngularHelper.IS_STABLE_JS);
		if (ret == null) { return null; }
		if (Boolean.class.isInstance(ret)) { return Boolean.class.cast(ret); }
		return Boolean.valueOf(ret.toString());
	}

	public AngularHelper(WebDriver browser) {
		super(browser);
	}

	public static ExpectedCondition<Boolean> angularIsPresent() {
		return AngularHelper.IS_PRESENT;
	}

	public static ExpectedCondition<Boolean> angularIsStable() {
		return AngularHelper.IS_STABLE;
	}

	public final void waitForAngular() {
		waitForAngular(null, null);
	}

	public final void waitForAngular(long waitSecs) {
		waitForAngular(Duration.ofSeconds(waitSecs), null);
	}

	public final void waitForAngular(Duration wait) {
		waitForAngular(wait, null);
	}

	public final void waitForAngular(long waitSecs, long pollMs) {
		waitForAngular(Duration.ofSeconds(waitSecs), Duration.ofMillis(pollMs));
	}

	public final void waitForAngular(Duration wait, long pollMs) {
		waitForAngular(wait, Duration.ofMillis(pollMs));
	}

	public final void waitForAngular(long waitSecs, Duration pollTime) {
		waitForAngular(Duration.ofSeconds(waitSecs), pollTime);
	}

	public final void waitForAngular(Duration wait, Duration pollTime) {
		if (AngularHelper.ENABLED.get()) {
			waitUntil(AngularHelper.angularIsStable(), wait, pollTime, "Angular has not yet fully loaded");
		}
	}

	public final boolean isAngularPresent() {
		return AngularHelper.isAngularPresent(this.browser);
	}

	public final Boolean isAngularStable() {
		return AngularHelper.isAngularStable(this.browser);
	}

	public final void waitForAngularEnabled(boolean enabled) {
		AngularHelper.ENABLED.set(enabled ? Boolean.TRUE : Boolean.FALSE);
	}
}
