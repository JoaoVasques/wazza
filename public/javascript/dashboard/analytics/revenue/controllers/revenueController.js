'use strict';

dashboard
.controller('RevenueController', [
  '$scope',
  '$rootScope',
  'ApplicationStateService',
  'DateModel',
  'DetailedKpiModel',
  'RevenueDateChanged',
  'RevenuePlatformsChanged',
  function (
    $scope,
    $rootScope,
    ApplicationStateService,
    DateModel,
    DetailedKpiModel,
    RevenueDateChanged,
    RevenuePlatformsChanged
  ) {

    var title = "Total Revenue";
    var KpiId = "revenue";

    ApplicationStateService.setPath(title);
    
    $scope.buildContext(new DetailedKpiModel(DateModel.startDate, DateModel.endDate, title));

    $scope.updateData($scope.context, KpiId, title);

    $scope.$on(RevenueDateChanged, function(ev, data) {
      $scope.context.beginDate = DateModel.startDate;
      $scope.context.endDate = DateModel.endDate;
      $scope.updateData($scope.context, KpiId, title);
    });

    $scope.$on(RevenuePlatformsChanged, function(ev, data) {
      $scope.updateData($scope.context, KpiId, title);
      if(!data.value) {
        $scope.updateChart(title, $scope.context);
      } else {
        $scope.updateChart(title, $scope.context);
      }
    });
}]);

