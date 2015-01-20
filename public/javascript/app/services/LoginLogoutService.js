service.factory('LoginLogoutService', ['$rootScope', '$http', function ($rootScope, $http) {
	var service = {};

	service.logout = function(logoutData){
		var handleLogoutSuccess = function(logoutData){
			$rootScope.$broadcast("LOGOUT_SUCCESS", {value: logoutData.data});
		};
		var handleLogoutFailure = function(data){
			$rootScope.$broadcast("LOGOUT_ERROR", {value: data});
		};

		$http.post("/logout")
		.then(handleLogoutSuccess, handleLogoutFailure);
	};

	return service;
}]);
