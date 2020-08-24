// Old code, keep it around just in case...
window.__disable_beforeunload_events__ = true;
try {
	$(getEventListeners(window)["beforeunload"]).each(function (i, v) {
	    if (v.listener.name == "attemptReleaseOrderLock") {
			removeEventListener("beforeunload", v.listener);
		}
	});
} catch (e) {
	// Ignore... maybe getEventListeners() is not defined...
}