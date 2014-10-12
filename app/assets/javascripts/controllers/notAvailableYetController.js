application.controller('NotAvailableYetController', [
  '$scope',
  '$rootScope',
  'ApplicationStateService',
  function(
    $scope,
    $rootScope,
    ApplicationStateService
    ) {
    ApplicationStateService.setPath("Not available yet :(");

  }]);
