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
		service.pagename = "";

		service.getPath = function () {
			return service.pagename;
		};

		service.setPath = function(value) {
			service.pagename = value;
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

		service.updateCompanyName = function(newName) {
			service.companyName = newName;
			$rootScope.$broadcast("COMPANY_NAME_UPDATED");
		};

		service.updateApplicationsList = function (newList) {
			service.applicationsList = newList.slice(0);
			$rootScope.$broadcast("APPLICATIONS_LIST_UPDATED");
		};

		service.updateUserInfo = function (newInfo) {
			service.userInfo = newInfo;
			$rootScope.$broadcast("USER_INFO_UPDATED");
		};

		return service;
	}]);
