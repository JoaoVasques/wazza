service.factory('DateModel', function() {
	//hack to save previous date range
	this.min = "";
	this.max = "";
	this.refresh = false;

	var model = function() {
		this.startDate = new Date();
		this.endDate = new Date();
	};

	model.initDateInterval = function() {
		this.startDate = new Date(moment().subtract(7, 'days'));
		this.endDate = new Date();
	};

	model.formatDate = function(date) {
		return moment(date).format('DD-MM-YYYY');
	};

	return model;
});
