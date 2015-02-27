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
    "CurrencyService",
    "CurrencyChanges",
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
        $q,
        CurrencyService,
      CurrencyChanges
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

        $scope.appName = ApplicationStateService.applicationName;

        BootstrapSettingsService.execute()
        .then(
            bootstrapSuccessCallback,
            bootstrapFailureCallback);

        $scope.currencies = CurrencyService.getCurrencies();
      $scope.currentCurrency = ApplicationStateService.currency.name;
      $scope.changeCurrency = function(newCurrency) {
        $scope.currentCurrency = newCurrency;
        ApplicationStateService.changeCurrency(CurrencyService.getCurrency($scope.currentCurrency));
      }
      
}]);

