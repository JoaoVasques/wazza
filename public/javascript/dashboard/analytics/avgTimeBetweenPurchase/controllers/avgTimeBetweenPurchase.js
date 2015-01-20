'use strict';

dashboard
.controller('AverageTimeBetweenPurchaseController', [
  '$scope',
  '$rootScope',
  'ApplicationStateService',
  'DateModel',
  'DetailedKpiModel',
  'ATBPDateChanged',
  'ATBPPlatformsChanged',
  function (
    $scope,
    $rootScope,
    ApplicationStateService,
    DateModel,
    DetailedKpiModel,
    ATBPDateChanged,
    ATBPPlatformsChanged
  ) {

    var title = "Average Time Between Purchases";
    var KpiId = "avgTimeBetweenPurchases";

    ApplicationStateService.setPath(title);
    $scope.context = new DetailedKpiModel(DateModel.startDate, DateModel.endDate, title);

    $scope.updateChart(title, $scope.context);
    $scope.updateData($scope.context, KpiId, title);

    $scope.$on(ATBPDateChanged, function(ev, data) {
      $scope.context.beginDate = DateModel.startDate;
      $scope.context.endDate = DateModel.endDate;
      $scope.updateData($scope.context, KpiId, title);
    });
    
    $scope.$on(RevenuePlatformsChanged, function(ev, data) {
      $scope.updateData($scope.context, KpiId, title);
      if(!data.value) {
        $scope.context.removeSerieFromChart(data.platform);
        $scope.updateChart(title, $scope.context);
      } else {
        scope.updateData($scope.context, KpiId, title);
        $scope.updateChart(title, $scope.context);
      }
    });
}]);
