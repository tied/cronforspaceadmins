AJS.toInit(init);

function init() {
	console.log("initializing...");
	var dialog = AJS.dialog2("#edit-dialog");
	
	AJS.$(".edit-button").click(function(e) {
		AJS.$("#edit-job-type-name").val(AJS.$(e.target).data("jobTypeName"));
		AJS.$("#edit-job-type-url").val(AJS.$(e.target).data("jobTypeUrl"));
		AJS.$("#edit-job-type-method").val(AJS.$(e.target).data("jobTypeMethod"));
		AJS.$("#edit-job-type-id").val(AJS.$(e.target).data("jobTypeId"));
		
		AJS.$(".method-option").each(function(i) {
			
			if ($(this).val() == AJS.$(e.target).data("jobTypeMethod")) {
				console.log("method in button: " + AJS.$(e.target).data("jobTypeMethod"));
				console.log("option " + i +":" + $(this).val());
				console.log("equal? " + ($(this).val() == AJS.$(e.target).data("jobTypeMethod")));
				$(this).attr("selected", "selected");
			} else {
				console.log("method in button: " + AJS.$(e.target).data("jobTypeMethod"));
				console.log("option " + i +":" + $(this).val());
				console.log("equal? " + ($(this).val() == AJS.$(e.target).data("jobTypeMethod")));				
			}
		});		
		
		AJS.$("#edit-job-type-name").attr("data-current-name", AJS.$(e.target).data("jobTypeName"));
		dialog.show();
	});
	AJS.$(".delete-button").click(function(e) {
		var url = AJS.contextPath() + "/plugins/cron-for-space-admins/DeleteJobType.action?id=" + AJS.$(e.target).attr("data-job-type-id");
		window.location.replace(url);		
	})	
	AJS.$("#close-dialog").click(function() {
		console.log("close button clicked");
		dialog.hide()
	});
	AJS.$("#submit-dialog").click(function() {
		AJS.$("#edit-form").submit();
	});	
	
	AJS.formValidation.register(['job-type-name'], function(field) {
		var jobTypesString = field.args('job-type-name');
		var jobTypes= [];
		if (jobTypesString) {
			jobTypes = jobTypesString.split('|');
		}
		
		console.log("all forbidden names:");
		for (name in jobTypes) {
			console.log(jobTypes[name]);
		}
		if (field.$el.attr("data-is-edit") == "true") {
			console.log("this is an edit");
			var indexOfCurrentName = jobTypes.indexOf(field.$el.attr("data-current-name"));
			if (indexOfCurrentName > -1) {
				jobTypes.splice(indexOfCurrentName, 1);
			}
			console.log("all forbidden names after removal:");
			for (name in jobTypes) {
				console.log(jobTypes[name]);
			}				
		} else {
			console.log("this is not an edit");
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