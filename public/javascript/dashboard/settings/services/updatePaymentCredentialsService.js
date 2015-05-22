settingsServices.factory('UpdatePaymentCredentialsService', [
  '$resource',
  'ApplicationStateService',
  function (
    $resource,
    ApplicationStateService
  ) {
    this.execute = function (paymentCredentials, appName, successCallback, errorCallback) {
      var UpdatePaymentsCredentials = $resource("/dashboard/settings/updatePaymentsCredentials/:c/:a",
        {c: ApplicationStateService.companyName, a: appName}
      );
      console.log(ApplicationStateService);
      return UpdatePaymentsCredentials.save(
        paymentCredentials,
        function(res) {successCallback(res)},
        function(err) {
          errorCallback(err)
        }
      )
    };
      
    return this;
  }
]);
