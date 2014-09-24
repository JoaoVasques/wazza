'use strict';

dashboard
.controller('ArpuController', [
  '$scope',
  '$location',
  '$rootScope',
  'ApplicationStateService',
  'TopbarService',
  'GetMainKPIsService',
  'LineChartConfiguration',
  'DateModel',
  'DetailedKpiModel',
  'ArpuDateChanged',
  function (
    $scope,
    $location,
    $rootScope,
    ApplicationStateService,
    TopbarService,
    GetMainKPIsService,
    LineChartConfiguration,
    DateModel,
    DetailedKpiModel,
    ArpuDateChanged
  ) {

    TopbarService.setName("Average Revenue Per User");
    $scope.context = new DetailedKpiModel(DateModel.startDate, DateModel.endDate, "Average Revenue Per User");

    var KpiId = "arpu";

    $scope.updateChart("Average Revenue Per User", $scope.context);
    $scope.updateOnChangedDate($scope.context, KpiId, "Average Revenue Per User");

    $scope.$on(ArpuDateChanged, function(ev, data) {
      $scope.context.beginDate = DateModel.startDate;
      $scope.context.endDate = DateModel.endDate;
      $scope.updateOnChangedDate($scope.context, KpiId, "Average Revenue Per User");
    });

}]);
