(function(document) {
    document.scanAcmButtons = function(root, buttons) {
    	if (!buttons) buttons = {};

        var normalize = function(str) {
            if (!str) return null;
            return str.trim().replace(/\s+/g, " ");
        };

        // To track possible duplicate labels
        var labels = buttons["labels"];
        if (!labels) {
            labels = {};
            buttons["labels"] = labels;
        }

        // To track duplicates
        var duplicates = buttons["duplicates"];
        if (!duplicates) {
            duplicates = [];
            buttons["duplicates"] = duplicates;
        }

        // To track buttons that need manual identification
        var manual = buttons["manual"];
        if (!manual) {
            manual = [];
            buttons["manual"] = manual;
        }

        if (!root) root = document;

        $(root).find("button").each(function (idx) {

            var newButton = {};

            var locator = "title";
            var label = normalize(this.getAttribute("title"));
            if (!label) {
                locator = "text";
                label = normalize(this.textContent);
            }

            if (label) {
                var key = `${locator}::${label}`;
                // Has a good label! Is it a duplicate?
                if (labels[key]) {
                    // Duplicate!! Track it!
                    duplicates.push({
                        label : label,
                        locator : locator,
                        class : this.getAttribute("class"),
                        button : this
                    });
                } else {
                    // Non duplicate, mark it!
                    labels[key] = {
                        label : label,
                        locator : locator,
                        class : this.getAttribute("class"),
                        button : this
                    };
                }
            } else {
                // Ok so we have no label, we have to identify
                // this button manually and assign it a name
                manual.push({
                    class : this.getAttribute("class"),
                    button : this
                });
            }
        });
    	return buttons;
    }
})(document);