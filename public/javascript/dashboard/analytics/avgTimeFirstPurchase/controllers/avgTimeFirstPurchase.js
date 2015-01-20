'use strict';

dashboard
.controller('AverageTimeFirstPurchaseController', [
  '$scope',
  '$rootScope',
  'ApplicationStateService',
  'DateModel',
  'DetailedKpiModel',
  'AT1PDateChanged',
  'AT1PPlatformsChanged',
  function (
    $scope,
    $rootScope,
    ApplicationStateService,
    DateModel,
    DetailedKpiModel,
    AT1PDateChanged,
    AT1PPlatformsChanged
  ) {

    var title = "Average Time to First Purchase";
    var KpiId = "avgTime1stPurchase";

    ApplicationStateService.setPath(title);
    $scope.context = new DetailedKpiModel(DateModel.startDate, DateModel.endDate, title);

    $scope.updateChart(title, $scope.context);
    $scope.updateData($scope.context, KpiId, title);

    $scope.$on(AT1PDateChanged, function(ev, data) {
      $scope.context.beginDate = DateModel.startDate;
      $scope.context.endDate = DateModel.endDate;
      $scope.updateData($scope.context, KpiId, title);
    });

    $scope.$on(AT1PPlatformsChanged, function(ev, data) {
      $scope.updateData($scope.context, KpiId, title);
      if(!data.value) {
        $scope.context.removeSerieFromChart(data.platform);
        $scope.updateChart(title, $scope.context);
      } else {
        $scope.updateChart(title, $scope.context);
      }
    });


}]);
