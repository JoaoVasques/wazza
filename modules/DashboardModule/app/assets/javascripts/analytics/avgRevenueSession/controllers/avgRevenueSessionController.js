'use strict';

dashboard
.controller('AvgRevenueSessionController', [
  '$scope',
  '$location',
  '$rootScope',
  'ApplicationStateService',
  'TopbarService',
  'DateModel',
  'DetailedKpiModel',
  'AvgRevenueSessionDateChanged',
  function (
    $scope,
    $location,
    $rootScope,
    ApplicationStateService,
    TopbarService,
    DateModel,
    DetailedKpiModel,
    AvgRevenueSessionDateChanged
  ) {

    TopbarService.setName("Average Revenue Per Session");
    $scope.context = new DetailedKpiModel(DateModel.startDate, DateModel.endDate, "Average Revenue Per Session");

    var KpiId = "avgRevenueSession";

    $scope.updateChart("Average Revenue Per Session", $scope.context);
    $scope.updateOnChangedDate($scope.context, KpiId, "Average Revenue Per Session");

    $scope.$on(AvgRevenueSessionDateChanged, function(ev, data) {
      $scope.context.beginDate = DateModel.startDate;
      $scope.context.endDate = DateModel.endDate;
      $scope.updateOnChangedDate($scope.context, KpiId, "Average Revenue Per Session");
    });

}]);
