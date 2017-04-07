AJS.toInit(init);

function init() {
	var dialog = AJS.dialog2("#edit-dialog");

	AJS.$(".edit-button")
			.click(
					function(e) {
						AJS.$("#edit-job-type-name").val(
								AJS.$(e.target).data("jobTypeName"));
						var url = AJS.$(e.target).data("jobTypeUrl");
						AJS.$("#edit-job-type-url").val(url);
						AJS.$("#edit-job-type-method").val(
								AJS.$(e.target).data("jobTypeMethod"));
						AJS.$("#edit-job-type-id").val(
								AJS.$(e.target).data("jobTypeId"));
						AJS.$("#current-username").attr("data-username",
								AJS.$(e.target).data("jobTypeUsername"));
						var parameterString = AJS.$(e.target).data(
								"jobTypeParameters");
						var parameters = parameterString.trim().split(/\s+/);
						var nonPathParameters = "";
						for (i in parameters) {
							var parameter = parameters[i];
							if (url.indexOf("{" + parameter + "}") === -1) {
								nonPathParameters += parameter + "\n";
							}
						}
						if (nonPathParameters.length !== 0) {
							nonPathParameters = nonPathParameters.substring(0,
									nonPathParameters.length - 1);
						}

						AJS.$("#edit-job-type-parameters").val(
								nonPathParameters);
						AJS.$(".method-option").each(
								function(i) {
									var currentOption = $(this);

									if (currentOption.val() == AJS.$(e.target)
											.data("jobTypeMethod")) {
										currentOption.attr("selected",
												"selected");
										currentOption.parent().parent().find(
												".select2-chosen").html(
												currentOption.html());
									}
								});

						var isAuthenticationRequired = AJS.$(e.target).attr(
								"data-job-type-authentication");
						if (isAuthenticationRequired == "true") {
							AJS.$("#edit-job-type-authentication").attr(
									"checked", "checked");
							insertCredentialsFields("edit");
							AJS.$("#edit-username")
									.val(
											AJS.$("#current-username").data(
													"username"));
						} else {
							AJS.$("#edit-job-type-authentication").removeAttr(
									"checked");
						}
						AJS.$("#edit-job-type-name").attr("data-current-name",
								AJS.$(e.target).data("jobTypeName"));
						// hack to trigger validation
						AJS.$("#edit-job-type-name").change();
						AJS.$("#edit-job-type-url").change();
						AJS.$("#edit-job-type-id").change();
						dialog.show();
					});

	AJS
			.$(".delete-job-type-button")
			.click(
					function(e) {
						var jobTypeId = getJobTypeId(e);
						var url = AJS.contextPath()
								+ "/plugins/cron-for-space-admins/DeleteJobType.action?id="
								+ jobTypeId;
						window.location.replace(url);
					})

	AJS.$(".delete-job-button").click(
			function(e) {
				var jobId = getJobId(e);
				var spaceKey = getSpaceKey(e);
				var url = AJS.contextPath()
						+ "/plugins/cron-for-space-admins/DeleteJob.action?id="
						+ jobId + "&spacekey=" + spaceKey
						+ "&fromJobTypeAdminPage=true";
				window.location.replace(url);
			})

	AJS.$("#toggle-notification-enabled").on("change", function(e) {
		var actionUrl;
		AJS.$(e.target).parent()[0].busy = true;
		if (isNotificationEnabled()) {
			AJS.$(e.target).parent()[0].busy = true;
			actionUrl = "/plugins/cron-for-space-admins/DeleteJobType.action";
			window.location.replace(AJS.contextPath()
				+ actionUrl
				+ "?id="
				+ AJS.$("#notification-status").attr("job-type-id"));
		} else {
			AJS.$("#notification-form").submit();	
		}
	});
	
	AJS.$(".notification-credentials").on("input", function(event) {
		if (areCredentialsFilledIn() && toggleDisabled()) {
			AJS.$("#toggle-notification-enabled").removeAttr("disabled");
		} else if (!areCredentialsFilledIn() && !toggleDisabled()) {
			AJS.$("#toggle-notification-enabled").attr("disabled", "disabled");
		}
	});
	
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

	AJS.$("#close-dialog").click(function() {
		dialog.hide()
	});
	AJS.$("#submit-dialog").click(function() {
		AJS.$("#edit-form").submit();
	});

	AJS
			.$(".toggle-status")
			.click(
					function(e) {
						AJS.$(e.target).parent()[0].busy = true;
						var actionUrl;
						if (AJS.$(e.target).parent().attr("data-is-enabled") === "true") {
							actionUrl = "/plugins/cron-for-space-admins/UnregisterJob.action";
						} else {
							actionUrl = "/plugins/cron-for-space-admins/RegisterJob.action";
						}
						window.location.replace(AJS.contextPath() + actionUrl
								+ "?id="
								+ AJS.$(e.target).parent().attr("data-job-id")
								+ "&spacekey=jobTypeAdmin");
					});

	AJS.$("#create-job-type-authentication").on(
			"change",
			function() {
				var authenticationRequiredCheckBox = AJS
						.$("#create-job-type-authentication");
				if (authenticationRequiredCheckBox.is(":checked")) {
					insertCredentialsFields("create");
				} else {
					emptyCredentialsDiv("create");
				}
			});

	AJS.$("#edit-job-type-authentication").on(
			"change",
			function() {
				var authenticationRequiredCheckBox = AJS
						.$("#edit-job-type-authentication");
				if (authenticationRequiredCheckBox.is(":checked")) {
					insertCredentialsFields("edit");
					AJS.$("#edit-username").val(
							AJS.$("#current-username").data("username"));
					// AJS.$("#edit-username").val(AJS.$(e.target).data("jobTypeUsername"));
				} else {
					emptyCredentialsDiv("edit");
				}
			});

	function insertCredentialsFields(mode) {
		var credentialsDiv = AJS.$("#" + mode + "-credentials");
		credentialsDiv.empty();
		var inputUsername = "<div class=\"field-group top-label\">";
		inputUsername += "<label for=\""
				+ mode
				+ "-username\">Username<span class=\"aui-icon aui-icon-required\">Required</span></label>";
		inputUsername += "<input id=\""
				+ mode
				+ "-username\" data-aui-validation-field data-aui-validation-required=\"required\" data-aui-validation-required-msg=\"You must provide a username\" name=\"username\" type=\"text\" class=\"text\" />";
		inputUsername += "</div>";
		credentialsDiv.append(inputUsername);
		var inputPassword = "<div class=\"field-group top-label\">";
		inputPassword += "<label for=\""
				+ mode
				+ "-password\">Password<span class=\"aui-icon aui-icon-required\">Required</span></label>";
		inputPassword += "<input id=\""
				+ mode
				+ "-password\" data-aui-validation-field data-aui-validation-required=\"required\" data-aui-validation-required-msg=\"You must provide a password\" name=\"password\" type=\"password\" class=\"text\" />";
		inputPassword += "</div>";
		credentialsDiv.append(inputPassword);
	}

	function emptyCredentialsDiv(mode) {
		var credentialsDiv = AJS.$("#" + mode + "-credentials");
		credentialsDiv.empty();
	}

	AJS.formValidation.register([ 'job-type-name' ], function(field) {
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
			if (jobTypes[jobType].trim() === field.$el.val().trim()) {
				valid = false;
			}
		}

		if (valid === true) {
			field.validate();
		} else {
			field.invalidate('A job type by that name already exists!');
		}
	});
}