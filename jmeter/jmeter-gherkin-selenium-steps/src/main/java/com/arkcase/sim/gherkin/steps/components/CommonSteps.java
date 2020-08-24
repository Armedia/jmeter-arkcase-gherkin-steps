package com.arkcase.sim.gherkin.steps.components;

import org.jbehave.core.annotations.Then;

import com.arkcase.sim.gherkin.steps.BasicWebDriverSteps;

public class CommonSteps extends BasicWebDriverSteps {

	private static final String JS_DISABLE_LOCKING_BEHAVIOR = "window.__disable_beforeunload_events__ = true;";

	@Then("keep the request locked")
	public void keepRequestLocked() {
		getAngularHelper().runJavaScript(CommonSteps.JS_DISABLE_LOCKING_BEHAVIOR);
	}

}