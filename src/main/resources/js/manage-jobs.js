AJS.toInit(init);

function init() {
	console.log("initializing...");
	
	AJS.$("#create-job-type").auiSelect2();
	AJS.$("#edit-job-type").auiSelect2();
	var dialog = AJS.dialog2("#edit-dialog");
	AJS.$(".edit-button").on("click", function(e) {
		console.log(AJS.$(e.target).data("jobName"));
		console.log(AJS.$(e.target).attr("data-job-name"));
		console.log(AJS.$(e.target).parent().data("jobName"));
		console.log(AJS.$(e.target).parent().attr("data-job-name"));
		
		AJS.$("#edit-name").val(AJS.$(e.target).data("jobName"));
		AJS.$(".jobtype-option").each(function(i) {
			var currentOption = $(this);
			var parent = currentOption.parent();
			if (currentOption.val() == AJS.$(e.target).data("jobType")) {
				console.log("type in button: " + AJS.$(e.target).data("jobType"));
				console.log("option " + i +":" + $(this).val());
				console.log("equal? " + ($(this).val() == AJS.$(e.target).data("jobType")));
				currentOption.attr("selected", "selected");
				currentOption.parent().parent().find(".select2-chosen").html(currentOption.html());
				insertEditParameterFields(currentOption.val());
			} else {
				console.log("type in button: " + AJS.$(e.target).data("jobType"));
				console.log("option " + i +":" + $(this).val());
				console.log("equal? " + ($(this).val() == AJS.$(e.target).data("jobType")));				
			}
		});
		AJS.$("#edit-cron-expression").val(AJS.$(e.target).data("cronExpression"));		
		AJS.$("#edit-job-id").val(AJS.$(e.target).data("jobId"));
		AJS.$("#edit-name").attr("data-current-name", AJS.$(e.target).data("jobName"));
		AJS.$("#parameter-string").attr("data-parameters", AJS.$(e.target).data("parameters"));
		dialog.show();
	});
	AJS.$(".delete-button").click(function(e) {
		var url = AJS.contextPath() + "/plugins/cron-for-space-admins/DeleteJob.action?id=" + AJS.$(e.target).parent().attr("data-job-id") + "&spacekey=" + AJS.$(e.target).parent().attr("data-space-key");
		window.location.replace(url);		
	})
	AJS.$("#close-dialog").click(function() {
		console.log("close button clicked");
		dialog.hide();
	});
	AJS.$("#submit-dialog").click(function() {
		AJS.$("#edit-form").submit();
	});
	AJS.$(".toggle-status").click(function(e) {
		var actionUrl;
		if (AJS.$(e.target).parent().attr("data-is-enabled") === "true") {
			actionUrl = "/plugins/cron-for-space-admins/UnregisterJob.action";
		} else {
			actionUrl = "/plugins/cron-for-space-admins/RegisterJob.action";
		}
		window.location.replace(AJS.contextPath() + actionUrl + "?id=" + AJS.$(e.target).parent().attr("data-job-id") + "&spacekey=" + AJS.$(e.target).parent().attr("data-space-key"));
	});
	
	AJS.$(".cron-tooltip").each(function() {
		$(this).tooltip();
	})
	
	AJS.$("#create-job-type").on("change", function() {
		var jobTypeId = AJS.$("#create-job-type").val();
		AJS.$.get(AJS.contextPath() + "/rest/jobtype/1.0/parameters/" + jobTypeId)
			.done(function(data) {
				var parametersDiv = AJS.$("#create-job-parameters");
				parametersDiv.empty();
				insertParameterFields(data, parametersDiv);
			});
	});
	
	AJS.$("#edit-job-type").on("change", function() {
		var jobTypeId = AJS.$("#edit-job-type").val();
		insertEditParameterFields(jobTypeId);
	});
	
	function insertEditParameterFields(jobTypeId) {
		AJS.$.get(AJS.contextPath() + "/rest/jobtype/1.0/parameters/" + jobTypeId)
			.done(function(data) {
				var parametersDiv = AJS.$("#edit-job-parameters");
				parametersDiv.empty();
				insertParameterFields(data, parametersDiv);
				prefillParameterFields();
			});		
	}
	
	function insertParameterFields(data, div) {
		console.log(data);
		parameters = data.split("\n");
		console.log(parameters);
		for (index in parameters) {
			var parameter = parameters[index].trim();
			if (parameter) {
				div.append(getParameterFieldGroup(parameter));
			}
		}
	}

	function prefillParameterFields() {
		console.log("all is well (1)");
		var parameterString = AJS.$("#parameter-string").data("parameters");
		console.log("all is well (2)");
		var keyValuePairs = parameterString.split("&");
		console.log("all is well (3)");
		console.log("kyeValuePairs: " + keyValuePairs);
		for (index in keyValuePairs) {
			console.log("all is well (4)");
			var keyValuePair = keyValuePairs[index];
			console.log("keyValuePair: " + keyValuePair);
			var name = keyValuePair.split("=")[0];
			console.log("name: " + name);
			var value = keyValuePair.split("=")[1];
			console.log("value: " + value);
			AJS.$("#parameter-" + name).val(value);
		}
		console.log("all is well (5)");
	}
	
	function getParameterFieldGroup(parameter) {
		var field = "<div class=\"field-group top-label\">";
		field += "<label for=\"parameter-" + parameter + "\">'" + parameter + "' parameter<span class=\"aui-icon aui-icon-required\">Required</span></label>";
		field += "<input id=\"parameter-" + parameter + "\" name=\"parameter-" + parameter + "\" class=\"text\" data-aui-validation-field data-aui-validation-required data-aui-validation-required-msg=\"You must provide a value for all parameters\" pattern=\"^[a-zA-Z0-9]+$\" pattern-msg=\"Only alphanumeric characters are allowed for parameters\"/>";
		field += "</div>";
		return field;
	}

	AJS.formValidation.register(['job-name'], function(field) {
			console.log("validator called");
			var jobsString = field.args('job-name');
			var jobs= [];
			if (jobsString) {
				jobs = jobsString.split('|');
			}
			console.log("all forbidden names:");
			for (name in jobs) {
				console.log(jobs[name]);
			}
			if (field.$el.attr("data-is-edit") == "true") {
				console.log("this is an edit");
				var indexOfCurrentName = jobs.indexOf(field.$el.attr("data-current-name"));
				if (indexOfCurrentName > -1) {
					jobs.splice(indexOfCurrentName, 1);
				}
				console.log("all forbidden names after removal:");
				for (name in jobs) {
					console.log(jobs[name]);
				}				
			} else {
				console.log("this is not an edit");
			}
			var valid = true;
			for (job in jobs) {
				if (field.$el.val() === "")
					continue;
				if (jobs[job].trim() === field.$el.val().trim()) {
					valid = false;
					field.invalidate('A job by that name already exists!');
				}
			}
			if (valid === true) {
				field.validate();					
			}
	});
	
	
	AJS.formValidation.register(['cron-expression'], function(field) {
		
		console.log(field.$el.val());
		console.log(field.$el.val().trim());
		console.log(field.$el.val().trim().split(' '));
		var elements = field.$el.val().trim().split(' ');
		if ((elements.length !== 6) && (elements.length !== 7)) {
			field.invalidate('Invalid cron expression: must consist of five or six elements.');
			return;
		}
	
		var seconds = elements[0];
		var minutes = elements[1];
		var hours = elements[2];
		var dayOfMonth = elements[3];
		var month = elements[4];
		var dayOfWeek = elements[5];
		
		var year = "";
		if (elements.length === 7) {
			year = elements[6];
		}
		
		console.log("seconds: " + seconds);	
		if (!validate(seconds, 0, 59)) {
			field.invalidate('Invalid cron expression: ' + seconds + ' is not a valid value for the seconds.');
			return;			
		}
		
		if (!validate(minutes, 0, 59)) {
			field.invalidate('Invalid cron expression: ' + minutes + ' is not a valid value for the minutes.');
			return;			
		}
				
		if (!validate(hours, 0, 23)) {
			field.invalidate('Invalid cron expression: ' + hours + ' is not a valid value for the hours.');
			return;			
		}

		if (dayOfWeek === "?" && dayOfMonth === "?") {
			field.invalidate('Invalid cron expression: "?" is only allowed for either the day of the month or the day of the week not both.');
			return;			
		} else {
			if (dayOfWeek !== "?" && !validate(dayOfWeek, 1, 7)) {
				field.invalidate('Invalid cron expression: ' + dayOfWeek + ' is not a valid value for the day of the week.');
				return;			
			}		
	
			if (dayOfMonth !== "?" && !validate(dayOfMonth, 1, 31)) {
				field.invalidate('Invalid cron expression: ' + dayOfMonth + ' is not a valid value for the day of the month.');
				return;			
			}
		}
		
		if (!validate(month, 1, 12)) {
			field.invalidate('Invalid cron expression: ' + month + ' is not a valid value for the month.');
			return;			
		}
	
		
		if (year && !validate(year, 1970, 2099)) {
			field.invalidate('Invalid cron expression: ' + year + ' is not a valid value for the year.');
			return;			
		}
		
		function validate(value, lowerBound, upperBound) {
			if (value === "*") {
				return true;
			}
			
			var values = value.split(",");
			
			for (value in values) {
				if (!isValidSingleValue(values[value], lowerBound, upperBound)) {
					return false;
				};
			}	
			
			return true;
		}
		
		field.validate();
		
	});
	
	function isValidSingleValue(value, lowerBound, upperBound) {
		
		console.log("called: isValidSingleValue(" + value + ", " + lowerBound + ", " + upperBound + ")");
		
		if (!value) {
			console.log("no value, returning false");
			return false;
		}
		
		if (value.indexOf("-") !== -1 && value.indexOf("/") !== -1) {
			console.log("there cannot be both a '-' and a '/' in a single value, returning false");
			return false;
		} else if (value.indexOf("-") !== -1) {
			return isValidRange(value, lowerBound, upperBound);
		} else if (value.indexOf("/") !== -1) {			
			return isValidIntervall(value, lowerBound, upperBound);
		} else {
 			return (!isNaN(value) && inRange(value, lowerBound, upperBound));
		}
		
	}
	
	function isValidRange(value, lowerBound, upperBound) {
		var values = value.split("-");
		
		if (values.length > 2) {
			console.log("too many " + separator + " separators, returning false");
			return false;
		}
		
		if (isNaN(values[0]) || isNaN(values[1]) || values[0] === "" || values[1] === "") {
			console.log("at least one of " + values[0] + " and " + values[1] + " is not a number, returning false");
			return false;
		}
		
		var start;
		var end;

		if (values[0] === "0") {
			start = 0;
		} else {
			start = Number(values[0]);
		}
		
		if (values[1] === "0") {
			end = 0;
		} else {
			end = Number(values[1]);
		}
		
		if (start >= end) {
			console.log("start is NOT lower than end, returning false");
			return false;
		}
		
		return (inRange(start, lowerBound, upperBound)
				&& inRange(end, lowerBound, upperBound));	
	}
	
	function isValidIntervall(value, lowerBound, upperBound) {
		var values = value.split("/");
		
		if (values.length > 2) {
			console.log("too many " + separator + " separators, returning false");
			return false;
		}
		
		if (isNaN(values[0]) || isNaN(values[1]) || values[0] === "" || values[1] === "") {
			console.log("at least one of " + values[0] + " and " + values[1] + " is not a number, returning false");
			return false;
		}
		
		var start;
		var increment;

		if (values[0] === "0") {
			start = 0;
		} else {
			start = Number(values[0]);
		}
		
		if (values[1] === "0") {
			increment = 0;
		} else {
			increment = Number(values[1]);
		}
		
		
		if (!inRange(increment, 1, upperBound - start)) {
			console.log("increment either too large or < 1, returning false");
			console.log("increment: " + increment);
			console.log("max allowed value for increment: " + (upperBound - start));
			return false;
		}

		return (inRange(start, lowerBound, upperBound));
	}
		
	
	function inRange(number, lowerBound, upperBound) {
		console.log("number: " + number);
		console.log("lowerBound: " + lowerBound);
		console.log("upperBound: " + upperBound);
		console.log("in range? " + (number >= lowerBound) && (number <= upperBound));
		return ((number >= lowerBound) && (number <= upperBound));
	}	
}