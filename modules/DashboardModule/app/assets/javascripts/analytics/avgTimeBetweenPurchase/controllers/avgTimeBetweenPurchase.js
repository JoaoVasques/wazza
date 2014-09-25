'use strict';

dashboard
.controller('AverageTimeBetweenPurchase', [
  '$scope',
  '$location',
  '$rootScope',
  'ApplicationStateService',
  'TopbarService',
  'DateModel',
  'DetailedKpiModel',
  'ATBPDateChanged',
  function (
    $scope,
    $location,
    $rootScope,
    ApplicationStateService,
    TopbarService,
    DateModel,
    DetailedKpiModel,
    ATBPDateChanged
  ) {

    TopbarService.setName("Average Time Between Purchases");
    $scope.context = new DetailedKpiModel(DateModel.startDate, DateModel.endDate, "Average Time Between Purchases");

    var KpiId = "avgTimeBetweenPurchase";

    $scope.updateChart("Average Revenue Per User", $scope.context);
    $scope.updateOnChangedDate($scope.context, KpiId, "Average Time Between Purchases");

    $scope.$on(ArpuDateChanged, function(ev, data) {
      $scope.context.beginDate = DateModel.startDate;
      $scope.context.endDate = DateModel.endDate;
      $scope.updateOnChangedDate($scope.context, KpiId, "Average Time Between Purchases");
    });

}]);
