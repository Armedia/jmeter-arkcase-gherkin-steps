(function(document) {
    document.scanAcmForm = function(form) {
    	var fieldData = {};
    	$(form).each(function (tabIndex) {
    		var tabObj = {};
    		var tabName = this.getAttribute("name");
    		tabObj["locator"] = `<all-tabs-container> ng-form[name="${tabName}"]`;

    		var forms = {};
    		tabObj["forms"] = forms;

    		var normalize = function(str) {
    			if (!str) return null;
    			return str.trim().replace(/\s+/g, " ");
    		};

    		$(this).find('panel-view div.panel-body form').each(function (formIndex) {
    			var formObj = {};
    			var formName = document.xpath("ancestor::div[@ng-if='collapsible']/div[contains(@class, 'panel-heading')]", this)[0].textContent;
    			var div = $(this).parents("div[ng-include]");
    			formObj["source"] = eval(div.attr("ng-include"));
    			formObj["locator"] = `<parent-tab> form[name="${formName}"`;
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
    	            var labelTag = document.xpath("preceding-sibling::label", this);
    	            if (labelTag) {
    	            	label = normalize(labelTag[0].textContent);
    	            } else {
    	            	labelTag = document.xpath("../preceding-sibling::label", this);
    	            	if (labelTag) label = normalize(labelTag[0].textContent);
    	            }

    				var locator = `<from-parent-form-css> ${tag}[ng-model="${model}"]`;
    				var value = "";
    				var options = null;
    				if (type == "radio") {
    					// Radio buttons are selected by value
    					value = this.getAttribute("value");
    					locator = `${locator}[value="${value}"]`;
    					label = normalize(this.parentNode.textContent);
    				} else if (type == "checkbox") {
    					label = this.parentNode.textContent.trim().replace(/\s+/g, " ");
    					locator = `<from-parent-form-xpath> .//label[normalize-space(text())='${label}']/input[@type='checkbox']`;
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
    				if (label == model) label = `FIXME::${label}`;

    				var field = {
    					"type" : type,
    					"locator" : locator
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
    	console.save(fieldData, "fieldData.json");
    }
})(document);