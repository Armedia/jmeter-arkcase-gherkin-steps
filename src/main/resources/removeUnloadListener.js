if (getEventListeners) {
	$(getEventListeners(window)["beforeunload"]).each(function (i, v) {
	    if (v.listener.name == "attemptReleaseOrderLock") {
			removeEventListener("beforeunload", v.listener);
		}
	});
}