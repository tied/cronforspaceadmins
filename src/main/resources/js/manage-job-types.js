AJS.toInit(init);

function init() {
	console.log("initializing...");
	
	AJS.formValidation.register(['job-type-name'], function(field) {
		var jobTypesString = field.args('job-type-name');
		var jobTypes= [];
		if (jobTypesString) {
			jobTypes = jobTypesString.split('|');
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