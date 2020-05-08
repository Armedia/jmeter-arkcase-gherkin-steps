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
package com.arkcase.sim.gherkin.jbehave.steps.selenium;

import org.jbehave.core.annotations.Alias;
import org.jbehave.core.annotations.Aliases;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;

public class AngularSteps extends BasicWebDriverSteps {

	@Given("the page is ready")
	@Alias("the page is loaded")
	public void givenAngularIsReady() {
		Boolean result = getWaitHelper().isAngularStable();
		if (result == null) { throw new IllegalStateException("Angular is not present"); }
		if (result != Boolean.TRUE) { throw new IllegalStateException("Angular is not stable"); }
	}

	@Given("the page is not ready")
	@Alias("the page is not loaded")
	public void givenAngularIsNotReady() {
		Boolean result = getWaitHelper().isAngularStable();
		if (result == null) { throw new IllegalStateException("Angular is not present"); }
		if (result != Boolean.FALSE) { throw new IllegalStateException("Angular is already stable"); }
	}

	@When("the page is ready")
	@Alias("the page is loaded")
	public void waitForAngular() {
		getWaitHelper().waitForAngular();
	}

	@Then("wait for the page to be ready")
	@Aliases(values = {
		"wait while the page works", //
		"wait while the page finishes loading", //
		"wait while the page loads", //
	})
	public void waitForAppToIdle() {
		waitForAngular();
	}

	@Given("Angular is present")
	@Aliases(values = {
		"angular is present", //
		"AngularJS is present", //
		"angularjs is present", //
		"Angular is in use", //
		"angular is in use", //
		"AngularJS is in use", //
		"angularjs is in use", //
	})
	public void givenAngularIsPresent() {
		if (!getWaitHelper().isAngularPresent()) { throw new IllegalStateException("Angular is not present"); }
	}

	@Given("Angular is not present")
	@Aliases(values = {
		"angular is not present", //
		"AngularJS is not present", //
		"angularjs is not present", //
		"Angular is not in use", //
		"angular is not in use", //
		"AngularJS is not in use", //
		"angularjs is not in use", //
	})
	public void givenAngularIsNotPresent() {
		if (getWaitHelper().isAngularPresent()) { throw new IllegalStateException("Angular is present"); }
	}
}