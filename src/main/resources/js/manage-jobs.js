AJS.toInit(init);

function init() {
	console.log("initializing...");
	AJS.$("#select-job-type").auiSelect2();
	
	AJS.formValidation.register(['job-name'], function(field) {
			console.log("validator called");
			var jobsString = field.args('job-name');
			var jobs= [];
			if (jobsString) {
				jobs = jobsString.split('|');
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
		
		// debugging
		field.validate();
		return;
		
		var elements =  field.$el.val().trim().split(' ');
		if ((elements.length !== 6) && (elements.length !== 7)) {
			field.invalidate('Invalid cron expression: must consist of five or six elements.');
			return;
		}
		if (elements[0] !== "*") {
			var sec = Number(elements[0]);
			if (!sec || (!inRange(sec, 0, 59))) {
				field.invalidate('Invalid cron expression: ' + sec + ' is not a valid value for the second.');
				return;
			}
		}		
		if (elements[1] !== "*") {
			var min = Number(elements[1]);
			if (!min || (!inRange(min, 0, 59))) {
				field.invalidate('Invalid cron expression: ' + min + ' is not a valid value for the minute.');
				return;
			}
		}
		if (elements[2] !== "*") {
			var hour = Number(elements[2]);
			if (!hour || (!inRange(hour, 0, 23))) {
				field.invalidate('Invalid cron expression: ' + hour + ' is not a valid value for the hour.');
				return;
			}
		}
		if (elements[3] !== "*") {
			var dom = Number(elements[3]);
			if (!dom || (!inRange(dom, 0, 31))) {
				field.invalidate('Invalid cron expression: ' + dom + ' is not a valid value for the day of the month.');
				return;
			}
		}
		if (elements[4] !== "*") {
			var month = Number(elements[4]);
			if (!month || (!inRange(month, 1, 12))) {
				field.invalidate('Invalid cron expression: ' + month + ' is not a valid value for the month.');
				return;
			}
		}
		if (elements[5] !== "*" && elements[5] !== "?") {
			var dow = Number(elements[5]);
			if (!dow || (!inRange(dow, 1, 7))) {
				field.invalidate('Invalid cron expression: ' + dow + ' is not a valid value for the day of the week.');
				return;
			}
		}
		if (elements.length === 7) {
			if (elements[6] !== "*") {
				var year = Number(elements[6]);
				if (!year || (!inRange(year, 1970, 2099))) {
					field.invalidate('Invalid cron expression: ' + year + ' is not a valid value for the year.');
					return;
				}
			}
		}			
		
		field.validate();
		
	});
	
	
	function inRange(number, lowerBound, upperBound) {
		return ((number >= lowerBound) && (number <= upperBound));
	}
	
}