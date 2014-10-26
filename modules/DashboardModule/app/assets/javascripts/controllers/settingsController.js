dashboard.controller('SettingsController', [
    '$scope',
    '$location',
    '$rootScope',
    "$anchorScroll",
    "$state",
    "$document",
    'BootstrapDashboardService',
    'ApplicationStateService',
    'GetKPIService',
    "DateModel",
    "KpiModel",
    "$q",
    function (
        $scope,
        $location,
        $rootScope,
        $anchorScroll,
        $state,
        $document,
        BootstrapDashboardService,
        ApplicationStateService,
        GetKPIService,
        DateModel,
        KpiModel,
        $q
        ) {

        $scope.updateKPIs = function(){
          var companyName = ApplicationStateService.getCompanyName();
          var app = ApplicationStateService.getApplicationName();
		}

        $scope.switchDetailedView = function(state) {
            $state.go(state);
            $document.scrollTop(-50, 500); //hack
        };

        var bootstrapSuccessCallback = function (data) {

            angular.extend($scope.credentials, data.data.credentials);
            ApplicationStateService.updateUserInfo(data.data.userInfo);
            ApplicationStateService.setPath("Settings");

        };

        var bootstrapFailureCallback = function (errorData) {
            console.log(errorData);
        };

        $scope.credentials = {};

        BootstrapDashboardService.execute()
            .then(
                bootstrapSuccessCallback,
                bootstrapFailureCallback);

	    bootstrap = function(){
	      if(ApplicationStateService.getCompanyName() === "")
	        fetchCompanyName();

	      if(ApplicationStateService.getApplicationsList().length === 0)
	        fetchApplications();
	      else
	        $scope.applications = ApplicationStateService.getApplicationsOverview();

	      fetchKPIs();
	    }

	    bootstrap();

}]);
