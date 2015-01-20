'use strict';

dashboard
.controller('LifeTimeValueController', [
  '$scope',
  '$rootScope',
  'ApplicationStateService',
  'DateModel',
  'DetailedKpiModel',
  'LtvDateChanged',
  'LtvPlatformsChanged',
  function (
    $scope,
    $rootScope,
    ApplicationStateService,
    DateModel,
    DetailedKpiModel,
    LtvDateChanged,
    LtvPlatformsChanged
  ) {

    var title = "Lifetime Value";
    var KpiId = "ltv";

    ApplicationStateService.setPath(title);
    $scope.context = new DetailedKpiModel(DateModel.startDate, DateModel.endDate, title);

    $scope.updateChart(title, $scope.context);
    $scope.updateData($scope.context, KpiId, title);

    $scope.$on(LtvDateChanged, function(ev, data) {
      $scope.context.beginDate = DateModel.startDate;
      $scope.context.endDate = DateModel.endDate;
      $scope.updateData($scope.context, KpiId, title);
    });

    $scope.$on(LtvPlatformsChanged, function(ev, data) {
      $scope.updateData($scope.context, KpiId, title);
      if(!data.value) {
        $scope.context.removeSerieFromChart(data.platform);
        $scope.updateChart(title, $scope.context);
      } else {
        $scope.updateChart(title, $scope.context);
      }
    });
}]);
