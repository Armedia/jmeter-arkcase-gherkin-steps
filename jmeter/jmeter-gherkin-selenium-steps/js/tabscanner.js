(function(document) {
    document.scanAcmTabs = function(root, tabs) {
    	if (!tabs) tabs = {};

    	// Root is the outermost container, so find div.tab-pane and ng-form within
    	$(root).find("div.tab-pane ng-form").each(function (tabIndex) {
    		var tabName = this.getAttribute("name");

    		var tabObj = tabs[tabName];
    		if (!tabObj) tabObj = {};

    		tabObj["name"] = tabName;
    		tabObj["body"] = `ng-form[name="${tabName}"]`;
    		tabObj["title"] = `li[active="tabs.${tabName}TabActive"]`;

    		var sections = tabObj["sections"];
    		if (!sections) {
    			sections = {};
        		tabObj["sections"] = sections;
    		}

    		var normalize = function(str) {
    			if (!str) return null;
    			return str.trim().replace(/\s+/g, " ");
    		};

    		var findLabelTag = function(ref) {
    			var labelTag = null;
	            if (!labelTag) labelTag = document.xpath("preceding-sibling::label", ref);
	            if (!labelTag) labelTag = document.xpath("../preceding-sibling::label", ref);
	            return (labelTag ? labelTag[0] : null);
    		};

    		$(this).find('panel-view div.panel-body').each(function (formIndex) {
    			var sectionName = $(panelView).find("div.panel div.panel-heading").text();
    			var sectionObj = sections[sectionName];
    			if (!sectionObj) sectionObj = {};


    			var panelView = document.xpath("ancestor::panel-view", this)[0];
    			var div = $(this).parents("div[ng-include]");
    			sectionObj["source"] = eval(div.attr("ng-include"));
    			sectionObj["body"] = `panel-view[header="${sectionName}"] div.panel-body form`;
    			sectionObj["title"] = `panel-view[header="${sectionName}"] div.panel-heading`;
    			sectionObj["name"] = sectionName;

    			var fields = sectionObj["fields"];
    			if (!fields) {
    				fields = {};
        			sectionObj["fields"] = fields;
    			}

    			$(this).find("input, select, textarea").each(function (fieldIndex) {
    				// Store the ng-model attribute
    				var model = this.getAttribute("ng-model");

    				// First: what tag did we find?
    				var tag = this.tagName.toLowerCase();

    				// Next: what type of field is it? (only applicable for input)
    				var type = tag;
    				if (tag == "input") {
    					type = this.getAttribute("type").toLowerCase();
    				}

    				// Skip the ones we don't want to mess with...
    				switch (type) {
    					case "file":
    					case "image":
    					case "reset":
    					case "button":
    					case "submit":
    					case "hidden":
    						return;
    				}

    				var label = model;
    	            var labelTag = findLabelTag(this);
    	            if (labelTag) label = normalize(labelTag.textContent);

    				var locator = `${tag}[ng-model="${model}"]`;
    				var locatorType = "css";
    				var value = "";
    				var options = null;
    				if (type == "radio") {
    					// Radio buttons are selected by value
    					value = this.getAttribute("value");
    					locator = `${locator}[value="${value}"]`;
    					label = normalize(this.parentNode.textContent);
    				} else if (type == "checkbox") {
    					label = this.parentNode.textContent.trim().replace(/\s+/g, " ");
    					locator = `.//label[normalize-space(text())='${label}']/input[@type='checkbox']`;
    					locatorType = "xpath";
    				} else
    				if (tag == "select") {
    	                // Find the options, get the values and labels
    	                options = [];
    	                $(this).find("option").each(function (optionIndex) {
    						var label = this.textContent;
    						if (!label) label = this.getAttribute("label");
    						if (!label) label = this.getAttribute("value");
    						if (label) options.push(label);
    					});
    					// We avoid select fields with no options
    					if (options.length < 1) return;
    				}

    				// Remove the "required" asterisk
    				if (label.endsWith(" *")) label = label.replace(/ \*$/g, "");

    				// Finally: if we couldn't find a human-friendly label, let
    				// someone know!
    				if (label == model) label = `FIXME::${label}`;

    				var field = {
    					"name" : label,
    					"type" : type,
    					"locator" : locator,
    					"locatorType" : locatorType
    				};
    				if (value) field["value"] = value;
    				if (options) field["options"] = options;
    				fields[label] = field;
    			});
    			if (!jQuery.isEmptyObject(fields)) {
    				sections[sectionName] = sectionObj;
    			}
    		});
    		if (!jQuery.isEmptyObject(sections)) {
    			tabs[tabName] = tabObj;
    		}
    	});
    	return tabs;
    }
})(document);