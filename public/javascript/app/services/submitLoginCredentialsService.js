service.factory('submitLoginCredentialsService', ['$http', '$q', function($http, $q) {
	var service = {};

	service.execute = function(loginData){
		var request = $http.post("/login", loginData);
		var deferred = $q.defer();
		deferred.resolve(request);
		return deferred.promise;
	};

	return service;
}]);
