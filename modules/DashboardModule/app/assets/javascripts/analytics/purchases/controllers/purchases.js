'use strict';

dashboard
.controller('PurchaseController', [
  '$scope',
  '$location',
  '$rootScope',
  'ApplicationStateService',
  'DateModel',
  'DetailedKpiModel',
  'PurchaseDateChanged',
  function (
    $scope,
    $location,
    $rootScope,
    ApplicationStateService,
    DateModel,
    DetailedKpiModel,
    PurchaseDateChanged
  ) {

    var title = "Purchase";
    var KpiId = "purchase";

    ApplicationStateService.setPath(title);
    $scope.context = new DetailedKpiModel(DateModel.startDate, DateModel.endDate, title);

    $scope.updateChart(title, $scope.context);
    $scope.updateOnChangedDate($scope.context, KpiId, title);

    $scope.$on(PurchaseDateChanged, function(ev, data) {
      $scope.context.beginDate = DateModel.startDate;
      $scope.context.endDate = DateModel.endDate;
      $scope.updateOnChangedDate($scope.context, KpiId, title);
    });

}]);
