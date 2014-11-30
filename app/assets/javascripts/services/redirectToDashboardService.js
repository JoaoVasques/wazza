service.factory('redirectToDashboardService', ['$http', '$q', function($http, $q) {
	var service = {};

	service.execute = function() {
		var request = $http.get("/dashboard");
		var deferred = $q.defer();
		deferred.resolve(request);
		return deferred.promise;
	};

	return service;
}]);
