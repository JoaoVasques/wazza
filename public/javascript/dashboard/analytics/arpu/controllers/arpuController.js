'use strict';

dashboard
.controller('ArpuController', [
  '$scope',
  '$rootScope',
  'ApplicationStateService',
  'DateModel',
  'DetailedKpiModel',
  'ArpuDateChanged',
  'ArpuPlatformsChanged',
  function (
    $scope,
    $rootScope,
    ApplicationStateService,
    DateModel,
    DetailedKpiModel,
    ArpuDateChanged,
    ArpuPlatformsChanged
  ) {

    var title = "Average Revenue Per User";
    var KpiId = "arpu";

    ApplicationStateService.setPath(title);
    $scope.context = new DetailedKpiModel(DateModel.startDate, DateModel.endDate, title);

    $scope.updateChart(title, $scope.context);
    $scope.updateData($scope.context, KpiId, title);

    $scope.$on(ArpuDateChanged, function(ev, data) {
      $scope.context.beginDate = DateModel.startDate;
      $scope.context.endDate = DateModel.endDate;
      $scope.updateData($scope.context, KpiId, title);
    });

    $scope.$on(ArpuPlatformsChanged, function(ev, data) {
      $scope.updateData($scope.context, KpiId, title);
      if(!data.value) {
        $scope.context.removeSerieFromChart(data.platform);
        $scope.updateChart(title, $scope.context);
      } else {
        $scope.updateChart(title, $scope.context);
      }
    });

}]);
