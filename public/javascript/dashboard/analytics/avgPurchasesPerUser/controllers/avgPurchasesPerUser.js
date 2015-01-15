'use strict';

dashboard
.controller('AvgPurchasesPerUserController', [
  '$scope',
  '$rootScope',
  'ApplicationStateService',
  'DateModel',
  'DetailedKpiModel',
  'PurchasesPerUserChanged',
  function (
    $scope,
    $rootScope,
    ApplicationStateService,
    DateModel,
    DetailedKpiModel,
    PurchasesPerUserChanged
  ) {

    var title = "Avg Purchases Per User";
    var KpiId = "avgPurchasesUser";

    ApplicationStateService.setPath(title);
    $scope.context = new DetailedKpiModel(DateModel.startDate, DateModel.endDate, title);

    $scope.updateChart(title, $scope.context);
    $scope.updateData($scope.context, KpiId, title);

    $scope.$on(PurchasesPerUserChanged, function(ev, data) {
      $scope.context.beginDate = DateModel.startDate;
      $scope.context.endDate = DateModel.endDate;
      $scope.updateData($scope.context, KpiId, title);
    });

}]);
