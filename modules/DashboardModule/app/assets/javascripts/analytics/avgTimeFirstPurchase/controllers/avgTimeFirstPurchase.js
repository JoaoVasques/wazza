'use strict';

dashboard
.controller('AverageTimeFirstPurchaseController', [
  '$scope',
  '$location',
  '$rootScope',
  'ApplicationStateService',
  'DateModel',
  'DetailedKpiModel',
  'AT1PDateChanged',
  function (
    $scope,
    $location,
    $rootScope,
    ApplicationStateService,
    DateModel,
    DetailedKpiModel,
    AT1PDateChanged
  ) {

    var title = "Average Time to First Purchases";
    var KpiId = "avgTime1stPurchase";

    ApplicationStateService.setPath(title);
    $scope.context = new DetailedKpiModel(DateModel.startDate, DateModel.endDate, title);

    $scope.updateChart(title, $scope.context);
    $scope.updateOnChangedDate($scope.context, KpiId, title);

    $scope.$on(AT1PDateChanged, function(ev, data) {
      $scope.context.beginDate = DateModel.startDate;
      $scope.context.endDate = DateModel.endDate;
      $scope.updateOnChangedDate($scope.context, KpiId, title);
    });

}]);
