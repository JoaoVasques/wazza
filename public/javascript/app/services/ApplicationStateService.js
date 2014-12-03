service.factory('ApplicationStateService', ['$rootScope', 'localStorageService',
	function ($rootScope, localStorageService) {
		var service = {};
		service.applicationName = "";
		service.companyName = "";
		service.applicationsList = [];
		service.userInfo = {};
		service.path = "";
		service.applicationOverview = "";

		//current view
		service.getPath = function () {
			return service.path;
		};

		service.setPath = function(value) {
			service.path = value;
			$rootScope.page = value;
			$rootScope.$broadcast("PAGE_UPDATED");
		};

		//current selected app
		service.getApplicationName = function () {
			return service.applicationName;
		}

		service.updateApplicationName = function (newName) {
			service.applicationName = newName;
			$rootScope.applicationName = newName;
			$rootScope.$broadcast("APPLICATION_NAME_UPDATED"); 
		};

		//currently logged company
		service.getCompanyName = function(newName) {
			return service.companyName;
		};

		service.updateCompanyName = function(newName) {
			service.companyName = newName;
			$rootScope.$broadcast("COMPANY_NAME_UPDATED");
		};

		//applications of logged user
		service.getApplicationsList = function (newList) {
			return service.applicationsList;
		};

		service.updateApplicationsList = function (newList) {
			service.applicationsList = newList.slice(0);
			$rootScope.$broadcast("APPLICATIONS_LIST_UPDATED");
		};

		//applications information (overview view)
		service.getApplicationsOverview = function () {
			return service.applicationOverview;
		}

		service.updateApplicationsOverview = function (apps) {
			service.applicationOverview = apps;
		};

		//user info: name & mail
		service.getUserInfo = function (newInfo) {
			service.userInfo = localStorageService.get("userInfo");
			return service.userInfo;
		};

		service.updateUserInfo = function (newInfo) {
			service.userInfo = newInfo;
			localStorageService.set("userInfo", newInfo);
			$rootScope.$broadcast("USER_INFO_UPDATED");
		};

		//hack to mantain initial clean state
		service.cleanup = function () {
			service.applicationName = "";
			service.companyName = "";
			service.applicationsList = [];
			service.userInfo = {
				name: "",
				email: ""
			};
			service.path = "";
			service.applicationOverview = "";
		};

		return service;
	}
]);
