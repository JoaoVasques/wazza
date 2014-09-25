'use strict';

dashboard
.controller('ArpuController', [
  '$scope',
  '$location',
  '$rootScope',
  'ApplicationStateService',
  'TopbarService',
  'DateModel',
  'DetailedKpiModel',
  'ArpuDateChanged',
  function (
    $scope,
    $location,
    $rootScope,
    ApplicationStateService,
    TopbarService,
    DateModel,
    DetailedKpiModel,
    ArpuDateChanged
  ) {

    var title = "Average Revenue Per User";
    var KpiId = "arpu";

    TopbarService.setName(title);
    $scope.context = new DetailedKpiModel(DateModel.startDate, DateModel.endDate, title);

    $scope.updateChart(title, $scope.context);
    $scope.updateOnChangedDate($scope.context, KpiId, title);

    $scope.$on(ArpuDateChanged, function(ev, data) {
      $scope.context.beginDate = DateModel.startDate;
      $scope.context.endDate = DateModel.endDate;
      $scope.updateOnChangedDate($scope.context, KpiId, title);
    });

}]);
