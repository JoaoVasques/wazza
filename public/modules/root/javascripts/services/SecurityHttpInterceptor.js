service.factory('SecurityHttpInterceptor', function($q) {
	return function (promise) {
		return promise.then(function (response) {
			return response;
		},
		function (response) {
			if (response.status === 500) {
				window.location = "error";
				return;
			}
			return $q.reject(response);
		});
	};
});
