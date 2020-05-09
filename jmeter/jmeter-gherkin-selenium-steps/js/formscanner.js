(function(document) {
    document.scanAcmForm = function(root, targetName) {
    	var fieldData = {};
    	$(root).each(function (tabIndex) {
    		var tabObj = {};
    		var tabName = this.getAttribute("name");
    		tabObj["body"] = `ng-form[name="${tabName}"]`;
    		tabObj["title"] = `li[active="tabs.${tabName}TabActive"]`;

    		var forms = {};
    		tabObj["forms"] = forms;

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

    		$(this).find('panel-view div.panel-body form').each(function (formIndex) {
    			var formObj = {};
    			var panelView = document.xpath("ancestor::panel-view", this)[0];
    			var formName = $(panelView).find("div.panel div.panel-heading").text();
    			var div = $(this).parents("div[ng-include]");
    			formObj["source"] = eval(div.attr("ng-include"));
    			formObj["body"] = `panel-view[header="${formName}"] div.panel-body form`;
    			formObj["title"] = `panel-view[header="${formName}"] div.panel-heading`;
    			var fields = {};
    			formObj["fields"] = fields;
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

    				// Finally: what's the field's label?
    				// $(this).find( ... );

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
    						var value = this.getAttribute("value");
    						var label = this.textContent;
    						if (!label) label = this.getAttribute("label");
    						if (!label) label = value;

    						var o = {
    							label: label,
    							value: value
    						};
    						options.push(o);
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
    					"type" : type,
    					"locator" : locator,
    					"locatorType" : locatorType
    				};
    				if (value) field["value"] = value;
    				if (options) field["options"] = options;
    				fields[label] = field;
    			});
    			if (!jQuery.isEmptyObject(fields)) {
    				forms[formName] = formObj;
    			}
    		});
    		if (!jQuery.isEmptyObject(forms)) {
    			fieldData[tabName] = tabObj;
    		}
    	});
    	if (!jQuery.isEmptyObject(fieldData)) {
    		if (!targetName) targetName = "fields.json";
    		console.save(fieldData, targetName);
    	}
    }
})(document);