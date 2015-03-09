service.factory('ApplicationStateService', [
  '$rootScope',
  'localStorageService',
  'SelectedPlatformsChange',
  'CurrencyService',
  'CurrencyChanges',
	function (
    $rootScope,
    localStorageService,
    SelectedPlatformsChange,
    CurrencyService,
    CurrencyChanges
  ) {
      
    function AppInfo(name, platforms) {
      this.name = name;
      this.platforms = platforms;
    };
          
		var service = {};
		service.applicationName = "";
		service.companyName = "";
		service.applicationsList = [];
		service.userInfo = {};
		service.path = "";
	  service.applicationOverview = "";
    service.selectedPlatforms = [];
    service.apps = [];
    service.currency = CurrencyService.getDefaultCurrency();

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

    service.updateApps = function(apps) {
      service.apps = [];
      _.each(apps, function(appInfo) {
        service.apps.push(new AppInfo(appInfo.name, appInfo.platforms));
      });
    }

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
		service.getUserInfo = function () {
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

    // Platform operations: add and remove platforms that are presented

    service.resetPlatforms = function() {
      service.selectedPlatforms = [];
    };

    service.addPlatforms = function(platform) {
      if(!_.contains(service.selectedPlatforms, platform)) {
        service.selectedPlatforms.push(platform);
      }
      $rootScope.$broadcast(SelectedPlatformsChange);
    };

    service.removePlatform = function(platform) {
      service.selectedPlatforms = _.without(service.selectedPlatforms, platform);
      $rootScope.$broadcast(SelectedPlatformsChange);
    };

    service.changeCurrency = function(newCurrency) {
      service.currency = newCurrency;
      $rootScope.$broadcast(CurrencyChanges);
    }
      
		return service;
	}
]);

