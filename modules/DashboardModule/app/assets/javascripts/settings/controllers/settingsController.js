dashboard.controller('SettingsController', [
    '$scope',
    '$location',
    '$rootScope',
    "$anchorScroll",
    "$state",
    "$document",
    'BootstrapSettingsService',
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
        BootstrapSettingsService,
        ApplicationStateService,
        GetKPIService,
        DateModel,
        KpiModel,
        $q
        ) {

        var bootstrapSuccessCallback = function (data) {

            $scope.credentials = data.data.credentials;

            $scope.userInfo = data.data.userInfo
            $scope.userInfo.companyName = ApplicationStateService.getCompanyName();
            ApplicationStateService.updateUserInfo($scope.userInfo);

            $scope.applications = ApplicationStateService.getApplicationsList();

            ApplicationStateService.setPath("Settings");
            ApplicationStateService.updateApplicationName("");

        };

        var bootstrapFailureCallback = function (errorData) {
            console.log(errorData);
        };

        BootstrapSettingsService.execute()
        .then(
            bootstrapSuccessCallback,
            bootstrapFailureCallback);

}]);
