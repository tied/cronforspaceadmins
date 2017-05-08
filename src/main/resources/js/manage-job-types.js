AJS.toInit(init);

var dialog;
var createParameterCount = 0;
var editParameterCount = 0;

function init() {

	dialog = AJS.dialog2("#edit-dialog");
	AJS.$("#notification-tooltip").tooltip();
	AJS.$(".url-tooltip").tooltip();
	AJS.$(".job-disabled-tooltip").tooltip();
	AJS.$("#notification-username-tooltip").tooltip()
	AJS.$(".edit-button").click(clickEditJobTypeHandler);
	AJS.$(".delete-job-type-button").click(
			function(e) {
				var jobTypeId = getJobTypeId(e);
				var url = AJS.contextPath()
						+ "/plugins/cron-for-space-admins/DeleteJobType.action?id="
						+ jobTypeId;
				window.location.replace(url);
			});
	AJS.$(".delete-job-button").click(deleteJobHandler);
	AJS.$("#toggle-notification-enabled").on("change",
			toggleNotificationHandler);
	AJS.$(".notification-credentials").on("input",
			notificationCredentialsInputHandler);
	AJS.$("#close-dialog").click(function() {
		dialog.hide()
	});
	AJS.$("#submit-dialog").click(function() {
		AJS.$("#edit-form").submit();
	});
	AJS.$(".toggle-status").click(toggleJobStatusHandler);
	AJS.$("#create-add-parameter").click(function() {
		AJS.$("#create-job-type-parameters").append(
				getParameterMarkup(createParameterCount++, "create", false));
				AJS.$(".friendly-name-tooltip").tooltip();
			});
	AJS.$("#edit-add-parameter").click(function() {
		AJS.$("#edit-job-type-parameters").append(
			getParameterMarkup(editParameterCount++, "edit"));
		AJS.$(".friendly-name-tooltip").tooltip();
	});
	AJS.$("#edit-job-type-parameters").on("click", ".delete-parameter-button",
			deleteEditParametersHandler);
	AJS.$("#create-job-type-authentication").on("change", function() {
		insertOrRemoveCredentials("create");
	});
	AJS.$("#edit-job-type-authentication").on("change", function() {
		insertOrRemoveCredentials("edit");
	});
	AJS.$("#create-job-type-parameters").on("click",
			".delete-parameter-button", deleteCreateParametersHandler);
	AJS.$("#create-job-type-url").on("change", urlChangeHandler);
	AJS.$("#edit-job-type-url").on("change", editUrlChangeHandler);

	AJS.formValidation
			.register(
					[ 'job-type-name' ],
					function(field) {
						var jobTypesString = field.args('job-type-name');
						var jobTypes = [];
						if (jobTypesString) {
							jobTypes = jobTypesString.split('|');
						}

						if (field.$el.attr("data-is-edit") == "true") {
							var indexOfCurrentName = jobTypes.indexOf(field.$el
									.attr("data-current-name"));
							if (indexOfCurrentName > -1) {
								jobTypes.splice(indexOfCurrentName, 1);
							}
						}

						var valid = true;
						for (jobType in jobTypes) {
							if (field.$el.val() === "")
								continue;
							if (jobTypes[jobType].trim() === field.$el.val()
									.trim()) {
								valid = false;
							}
						}

						if (valid === true) {
							field.validate();
						} else {
							field.invalidate(AJS.I18n.getText("de.iteconomics.confluence.plugins.job-type-name-exists"));
						}
					});
}

function urlChangeHandler(e) {
	var url = AJS.$(e.target).val();
	var pathParamRegex = /{\w+}/g;
	var pathParametersFromUrl = [];
	var nextMatch = pathParamRegex.exec(url);
	while (nextMatch != null) {
		var parameter = nextMatch[0];
		pathParametersFromUrl
				.push(parameter.substring(1, parameter.length - 1));
		nextMatch = pathParamRegex.exec(url);
	}

	var pathParametersFromForm = getPathParametersFromForm("create");

	addNewParametersToForm(pathParametersFromUrl, pathParametersFromForm,
			"create");
	removeObsoleteParametersFromForm(pathParametersFromUrl,
			pathParametersFromForm, "create");

}

function editUrlChangeHandler(e) {
	var url = AJS.$(e.target).val();
	var pathParamRegex = /{\w+}/g;
	var pathParametersFromUrl = [];
	var nextMatch = pathParamRegex.exec(url);
	while (nextMatch != null) {
		var parameter = nextMatch[0];
		pathParametersFromUrl
				.push(parameter.substring(1, parameter.length - 1));
		nextMatch = pathParamRegex.exec(url);
	}

	var pathParametersFromForm = getPathParametersFromForm("edit");

	addNewParametersToForm(pathParametersFromUrl, pathParametersFromForm,
			"edit");
	removeObsoleteParametersFromForm(pathParametersFromUrl,
			pathParametersFromForm, "edit");

}

function removeObsoleteParametersFromForm(pathParametersFromUrl,
		pathParametersFromForm, mode) {
	for (index in pathParametersFromForm) {
		var parameter = pathParametersFromForm[index];
		if (!pathParametersFromUrl.includes(parameter)) {
			removeFromForm(parameter, mode);
		}
	}
}

function addNewParametersToForm(pathParametersFromUrl, pathParametersFromForm,
		mode) {
	for (index in pathParametersFromUrl) {
		var parameter = pathParametersFromUrl[index];
		if (!pathParametersFromForm.includes(parameter)) {
			addToForm(parameter, mode);
		}
	}
	AJS.$(".friendly-name-tooltip").tooltip();
}

function getPathParametersFromForm(mode) {
	var parameterDivs = AJS.$("#" + mode + "-job-type-path-parameters")
			.children();
	var names = [];
	parameterDivs.each(function() {
		names.push(AJS.$(this).attr("parameter-name"));
	})
	return names;
}

function addToForm(parameterName, mode) {
	parameter = {
		"name" : parameterName,
		"friendlyName" : "",
		"pathParameter" : true,
		"description" : ""
	};
	var parameterMarkup = getParameterMarkup(createParameterCount++, "create",
			parameter);
	console.log(parameterMarkup);
	console.log(mode);
	AJS.$("#" + mode + "-job-type-path-parameters").append(parameterMarkup);
}

function removeFromForm(parameter, mode) {
	var parameterDiv = AJS.$("#" + mode + "-job-type-path-parameters")
			.children("div[parameter-name=\'" + parameter + "\']");
	parameterDiv.each(function() {
		AJS.$(this).remove();
	});
}

function clickEditJobTypeHandler(e) {
	editParameterCount = 0;
	insertParameterFieldsOfJobType(AJS.$(e.target).data("jobTypeId"));
	AJS.$("#edit-job-type-name").val(AJS.$(e.target).data("jobTypeName"));
	AJS.$("#edit-job-type-description").val(
			AJS.$(e.target).data("jobTypeDescription"));
	var url = AJS.$(e.target).data("jobTypeUrl");
	AJS.$("#edit-job-type-url").val(url);
	AJS.$("#edit-job-type-method").val(AJS.$(e.target).data("jobTypeMethod"));
	AJS.$("#edit-job-type-id").val(AJS.$(e.target).data("jobTypeId"));
	AJS.$("#current-username").attr("data-username",
			AJS.$(e.target).data("jobTypeUsername"));

	AJS.$(".method-option").each(
			function(i) {
				var currentOption = $(this);

				if (currentOption.val() == AJS.$(e.target)
						.data("jobTypeMethod")) {
					currentOption.attr("selected", "selected");
					currentOption.parent().parent().find(".select2-chosen")
							.html(currentOption.html());
				}
			});

	var isAuthenticationRequired = AJS.$(e.target).attr(
			"data-job-type-authentication");
	if (isAuthenticationRequired == "true") {
		AJS.$("#edit-job-type-authentication").attr("checked", "checked");
		insertCredentialsFields("edit");
		AJS.$("#edit-username")
				.val(AJS.$("#current-username").data("username"));
	} else {
		AJS.$("#edit-job-type-authentication").removeAttr("checked");
	}
	AJS.$("#edit-job-type-name").attr("data-current-name",
			AJS.$(e.target).data("jobTypeName"));
	// hack to trigger validation
	AJS.$("#edit-job-type-name").change();
	AJS.$("#edit-job-type-url").change();
	AJS.$("#edit-job-type-id").change();
	dialog.show();
}

function insertParameterFieldsOfJobType(jobTypeId) {
	AJS.$.get(
			AJS.contextPath() + "/rest/cronforspaceadmins/1.0/jobtype/parameters/"
					+ jobTypeId).done(function(data) {
		var parametersDiv = AJS.$("#edit-job-type-parameters");
		var pathParametersDiv = AJS.$("#edit-job-type-path-parameters");
		parametersDiv.empty();
		pathParametersDiv.empty();
		insertParameterFields(data, parametersDiv, pathParametersDiv);
	});
}

function insertParameterFields(data, parametersDiv, pathParametersDiv) {
	for (index in data) {
		var parameter = data[index];
		if (parameter.pathParameter) {
			pathParametersDiv.append(getParameterMarkup(index, "edit",
					parameter));
		} else {
			parametersDiv.append(getParameterMarkup(index, "edit", parameter));
		}

	}
	editParameterCount = data.length;
}

function deleteJobHandler(e) {
	var jobId = getJobId(e);
	var spaceKey = getSpaceKey(e);
	var url = AJS.contextPath()
			+ "/plugins/cron-for-space-admins/DeleteJob.action?id=" + jobId
			+ "&spacekey=" + spaceKey + "&fromJobTypeAdminPage=true";
	window.location.replace(url);
}

function toggleNotificationHandler(e) {
	var actionUrl;
	AJS.$(e.target).parent()[0].busy = true;
	if (isNotificationEnabled()) {
		AJS.$(e.target).parent()[0].busy = true;
		actionUrl = "/plugins/cron-for-space-admins/DeleteJobType.action";
		window.location.replace(AJS.contextPath() + actionUrl + "?id="
				+ AJS.$("#notification-status").attr("job-type-id"));
	} else {
		AJS.$("#notification-form").submit();
	}
}

function notificationCredentialsInputHandler(event) {
	if (areCredentialsFilledIn() && toggleDisabled()) {
		AJS.$("#toggle-notification-enabled").removeAttr("disabled");
		AJS.$("#notification-tooltip").hide();
	} else if (!areCredentialsFilledIn() && !toggleDisabled()) {
		AJS.$("#toggle-notification-enabled").attr("disabled", "disabled");
		AJS.$("#notification-tooltip").show();
	}
}

function areCredentialsFilledIn() {
	var username = AJS.$("#notification-username").val();
	var password = AJS.$("#notification-password").val();
	return ((username !== "") && (password !== ""));
}

function toggleDisabled() {
	var attr = $("#toggle-notification-enabled").attr("disabled");
	return (typeof attr !== typeof undefined) && (attr !== false);
}

function isNotificationEnabled() {
	var enabled = AJS.$("#notification-status").attr("enabled");
	return enabled === "true";
}

function getJobId(event) {
	var id = AJS.$(event.target).data("jobId");
	if (!id) {
		id = AJS.$(event.target).parent().data("jobId");
	}
	return id;
}

function getJobTypeId(event) {
	var id = AJS.$(event.target).data("jobTypeId");
	if (!id) {
		id = AJS.$(event.target).parent().data("jobTypeId");
	}
	return id;
}

function getSpaceKey(event) {
	var key = AJS.$(event.target).data("spaceKey");
	if (!key) {
		key = AJS.$(event.target).parent().data("spaceKey");
	}
	return key;
}

function toggleJobStatusHandler(e) {
	if (e.target.hasAttribute("disabled")) {
		return;
	}
	AJS.$(e.target).parent()[0].busy = true;
	var actionUrl;
	if (AJS.$(e.target).parent().attr("data-is-enabled") === "true") {
		actionUrl = "/plugins/cron-for-space-admins/UnregisterJob.action";
	} else {
		actionUrl = "/plugins/cron-for-space-admins/RegisterJob.action";
	}
	window.location.replace(AJS.contextPath() + actionUrl + "?id="
			+ AJS.$(e.target).parent().attr("data-job-id")
			+ "&spacekey=jobTypeAdmin");
}

function insertOrRemoveCredentials(mode) {
	var authenticationRequiredCheckBox = AJS.$("#" + mode
			+ "-job-type-authentication");
	if (authenticationRequiredCheckBox.is(":checked")) {
		insertCredentialsFields(mode);
		if (mode === "edit") {
			AJS.$("#edit-username").val(
					AJS.$("#current-username").data("username"));
		}
	} else {
		emptyCredentialsDiv(mode);
	}
}

function insertCredentialsFields(mode) {
	var credentialsDiv = AJS.$("#" + mode + "-credentials");
	credentialsDiv.empty();
	var inputUsername = "<div class=\"field-group top-label\">";
	inputUsername += "<label for=\"" + mode + "-username\">"
			+ AJS.I18n.getText("de.iteconomics.confluence.plugins.username")
			+ "<span class=\"aui-icon aui-icon-required\">"
			+ AJS.I18n.getText("de.iteconomics.confluence.plugins.required")
			+ "</span>"
			+ "<a class=\"username-tooltip\" title=\""
			+ AJS.I18n.getText("de.iteconomics.confluence.plugins.username-description")
			+ "\">"
			+ "<span class=\"aui-icon aui-icon-small aui-iconfont-info\">"
			+ AJS.I18n.getText("de.iteconomics.confluence.plugins.info")
			+ "</span>"
			+ "</a>"
			+ "</label>";
	inputUsername += "<input id=\""
			+ mode
			+ "-username\""
			+ "data-aui-validation-field data-aui-validation-required=\"required\""
			+ "data-aui-validation-required-msg=\""
			+ AJS.I18n.getText("de.iteconomics.confluence.plugins.username-required")
			+ "\"" + "name=\"username\" type=\"text\" class=\"text\" /></div>";
	credentialsDiv.append(inputUsername);
	AJS.$(".username-tooltip").tooltip();

	var inputPassword = "<div class=\"field-group top-label\">";
	inputPassword += "<label for=\""
			+ mode
			+ "-password\">"
			+ AJS.I18n.getText("de.iteconomics.confluence.plugins.password")
			+ "<span class=\"aui-icon aui-icon-required\">"
			+ AJS.I18n.getText("de.iteconomics.confluence.plugins.required")
			+ "</span></label>"
			+ "<input id=\""
			+ mode
			+ "-password\""
			+ "data-aui-validation-field data-aui-validation-required=\"required\""
			+ "data-aui-validation-required-msg=\""
			+ AJS.I18n.getText("de.iteconomics.confluence.plugins.password-required")
			+ "\" "
			+ "name=\"password\" type=\"password\" class=\"text\" /></div>";
	credentialsDiv.append(inputPassword);
//	AJS.$(".username-tooltip").tooltip();
}

function emptyCredentialsDiv(mode) {
	var credentialsDiv = AJS.$("#" + mode + "-credentials");
	credentialsDiv.empty();
}

function deleteEditParametersHandler(e) {
	var parameterNumber = AJS.$(e.target).attr("parameter-number");
	if (!parameterNumber || parameterNumber === "") {
		parameterNumber = AJS.$(e.target).parent().attr("parameter-number");
	}
	AJS.$("#edit-parameter-fieldgroup-" + parameterNumber).remove();
}

function deleteCreateParametersHandler(e) {
	var parameterNumber = AJS.$(e.target).attr("parameter-number");
	if (!parameterNumber || parameterNumber === "") {
		parameterNumber = AJS.$(e.target).parent().attr("parameter-number");
	}
	AJS.$("#create-parameter-fieldgroup-" + parameterNumber).remove();
}

function getParameterMarkup(index, mode, parameter) {
	var field = "<div id=\"" + mode + "-parameter-fieldgroup-" + index
			+ "\" parameter-name=\"";
	if (parameter) {
		field += parameter.name;
	}
	field += "\" class=\"field-group top-label\" style=\"border: 1px solid #ccc; border-radius: 3px; margin: 10px 0 20px 0; padding: 20px;\">";
	field += "<label for=\""
			+ mode
			+ "-job-type-parameters-"
			+ index
			+ "\">"
			+ AJS.I18n.getText("de.iteconomics.confluence.plugins.parameter-name")
			+ "<span class=\"aui-icon aui-icon-required\">"
			+ AJS.I18n.getText("de.iteconomics.confluence.plugins.required")
			+ "</span></label>"
	if (parameter && parameter.pathParameter) {
		field += "<p>" + parameter.name + "</p>";
	} else {
		field += "<input type=\"text\" id=\""
				+ mode
				+ "-job-type-parameter-"
				+ index
				+ "\" class=\"text\" name=\"parameter-name-"
				+ index
				+ "\" data-aui-validation-field data-aui-validation-required data-aui-validation-required-msg=\""
				+ AJS.I18n.getText("de.iteconomics.confluence.plugins.parameter-required")
				+ "\"";
		if (parameter) {
			field += "value=\"" + parameter.name + "\"";
		}
		field += "/>";
	}
	field += "<label for=\""
			+ mode
			+ "-job-type-parameter-friendly-name-"
			+ index
			+ "\">"
			+ AJS.I18n.getText("de.iteconomics.confluence.plugins.friendly-name")
			+ " <a class=\"friendly-name-tooltip\" title=\""
			+ AJS.I18n.getText("de.iteconomics.confluence.plugins.friendly-name-tooltip-message")
			+ "\">"
			+ "<span class=\"aui-icon aui-icon-small aui-iconfont-info\">$i18n.getText(\"de.iteconomics.confluence.plugins.info\")</span>"
			+ "</a>"			
			+ "</label>"
	field += "<input type=\"text\" id=\"" + mode
			+ "-job-type-parameter-friendly-name-" + index
			+ "\" class=\"text\" name=\"parameter-friendly-name-" + index
			+ "\"";
	if (parameter) {
		field += "value=\"" + parameter.friendlyName + "\"";
	}
	field += "/>";

	field += "<label for=\"" + mode + "-job-type-parameter-description-"
			+ index + "\">"
			+ AJS.I18n.getText("de.iteconomics.confluence.plugins.description")
			+ "</label>"
	field += "<textarea id=\"" + mode + "-job-type-parameter-description-"
			+ index + "\" class=\"aui text\" name=\"parameter-description-"
			+ index + "\" cols=\"10\" rows=\"3\">";
	if (parameter) {
		field += parameter.description;
	}
	field += "</textarea>"

	if (parameter && parameter.pathParameter) {
		field += "<input type=\"hidden\" name=\"parameter-path-parameter-"
				+ index + "\" value=\"true\" />"
		field += "<input type=\"hidden\" name=\"parameter-name-" + index
				+ "\" value=\"" + parameter.name + "\" />";
	} else {
		field += "<br/><br/>";
		field += "<button class=\"delete-parameter-button aui-button\" type=\"button\" parameter-number=\""
				+ index
				+ "\">"
				+ AJS.I18n.getText("de.iteconomics.confluence.plugins.delete-parameter")
				+ "<span class=\"aui-icon aui-icon-small aui-iconfont-delete\" >"
				+ AJS.I18n.getText("de.iteconomics.confluence.plugins.delete")
				+ "</span></button>"
	}
	field += "</div>";
	return field;
}