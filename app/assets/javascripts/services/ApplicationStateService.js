service.factory('ApplicationStateService', ['$rootScope',
	function ($rootScope) {
		var service = {};
		service.applicationName = "";
		service.companyName = "";
		service.applicationsList = [];
		service.userInfo = {
			name: "",
			email: ""
		};
		service.path = "";

		service.getPath = function () {
			return service.path;
		};

		service.setPath = function(value) {
			service.path = value;
			$rootScope.page = value;
			$rootScope.$broadcast("PAGE_UPDATED");
		};

		service.getApplicationName = function () {
			return service.applicationName;
		}

		service.updateApplicationName = function (newName) {
			service.applicationName = newName;
			$rootScope.applicationName = newName;
			$rootScope.$broadcast("APPLICATION_NAME_UPDATED"); 
		};

		service.getCompanyName = function(newName) {
			return service.companyName;
		};

		service.updateCompanyName = function(newName) {
			service.companyName = newName;
			$rootScope.$broadcast("COMPANY_NAME_UPDATED");
		};

		service.getApplicationsList = function (newList) {
			return service.applicationsList;
		};

		service.updateApplicationsList = function (newList) {
			service.applicationsList = newList.slice(0);
			$rootScope.$broadcast("APPLICATIONS_LIST_UPDATED");
		};

		service.getUserInfo = function (newInfo) {
			return service.userInfo;
		};

		service.updateUserInfo = function (newInfo) {
			service.userInfo = newInfo;
			$rootScope.$broadcast("USER_INFO_UPDATED");
		};

		return service;
	}
]);
