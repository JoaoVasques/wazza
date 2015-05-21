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
  "UpdatePaymentCredentialsService",
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
    CurrencyChanges,
    UpdatePaymentCredentialsService
  ) {

    var bootstrapSuccessCallback = function (data) {
      $scope.credentials = data.data.credentials;
      /** PayPal Credentials **/
      if(data.data.hasOwnProperty('payPalCredentials')) {
        $scope.payPalCredentials = data.data.payPalCredentials;
      }

      $scope.applications = ApplicationStateService.getApplicationsList();
      ApplicationStateService.setPath("Settings");
    };

    var bootstrapFailureCallback = function (errorData) {
      console.log(errorData);
    };

    $scope.appName = ApplicationStateService.applicationName;
      
    BootstrapSettingsService.execute()
      .then(
      bootstrapSuccessCallback,
      bootstrapFailureCallback
    );

    $scope.currencies = CurrencyService.getCurrencies();
    $scope.currentCurrency = ApplicationStateService.currency.name;
    $scope.changeCurrency = function(newCurrency) {
      $scope.currentCurrency = newCurrency;
      ApplicationStateService.changeCurrency(CurrencyService.getCurrency($scope.currentCurrency));
    };

    $scope.savePaymentChanges = function() {
      $scope.payPalCredentials.paymentSystem = 2;
      UpdatePaymentCredentialsService.execute(
        $scope.payPalCredentials,
        ApplicationStateService.applicationName,
        function(data) {
          swal("Success", "Changes were saved","success");
        },
        function(err) {
          swal("Error", "There was an error. Please try again", "error");
        }
      )
    };
  }]);

