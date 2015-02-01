'use strict';

dashboard
.controller('AvgRevenueSessionController', [
  '$scope',
  '$rootScope',
  'ApplicationStateService',
  'DateModel',
  'DetailedKpiModel',
  'AvgRevenueSessionDateChanged',
  'AvgRevenueSessionPlatformsChanged',
  function (
    $scope,
    $rootScope,
    ApplicationStateService,
    DateModel,
    DetailedKpiModel,
    AvgRevenueSessionDateChanged,
    AvgRevenueSessionPlatformsChanged
  ) {

    var title = "Average Revenue Per Session";
    var KpiId = "avgRevenueSession";

    ApplicationStateService.setPath(title);
    $scope.context = new DetailedKpiModel(DateModel.startDate, DateModel.endDate, title);

    $scope.updateChart(title, $scope.context);
    $scope.updateData($scope.context, KpiId, title);

    $scope.$on(AvgRevenueSessionDateChanged, function(ev, data) {
      $scope.context.beginDate = DateModel.startDate;
      $scope.context.endDate = DateModel.endDate;
      $scope.updateData($scope.context, KpiId, title);
    });

    $scope.$on(AvgRevenueSessionPlatformsChanged, function(ev, data) {
      $scope.updateData($scope.context, KpiId, title);
      if(!data.value) {
        $scope.context.removeSerieFromChart(data.platform);
        $scope.updateChart(title, $scope.context);
      } else {
        $scope.updateChart(title, $scope.context);
      }
    });

}]);
