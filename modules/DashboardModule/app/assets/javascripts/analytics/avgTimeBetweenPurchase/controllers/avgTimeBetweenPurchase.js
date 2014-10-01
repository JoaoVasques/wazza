'use strict';

dashboard
.controller('AverageTimeBetweenPurchaseController', [
  '$scope',
  '$location',
  '$rootScope',
  'ApplicationStateService',
  'DateModel',
  'DetailedKpiModel',
  'ATBPDateChanged',
  function (
    $scope,
    $location,
    $rootScope,
    ApplicationStateService,
    DateModel,
    DetailedKpiModel,
    ATBPDateChanged
  ) {

    var title = "Average Time Between Purchases";
    var KpiId = "avgTimeBetweenPurchases";

    ApplicationStateService.setPath(title);
    $scope.context = new DetailedKpiModel(DateModel.startDate, DateModel.endDate, title);

    $scope.updateChart(title, $scope.context);
    $scope.updateOnChangedDate($scope.context, KpiId, title);

    $scope.$on(ATBPDateChanged, function(ev, data) {
      $scope.context.beginDate = DateModel.startDate;
      $scope.context.endDate = DateModel.endDate;
      $scope.updateOnChangedDate($scope.context, KpiId, title);
    });

}]);
