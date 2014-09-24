'use strict';

dashboard
.controller('RevenueController', [
  '$scope',
  '$location',
  '$rootScope',
  'ApplicationStateService',
  'TopbarService',
  'DateModel',
  'DetailedKpiModel',
  'RevenueDateChanged',
  function (
    $scope,
    $location,
    $rootScope,
    ApplicationStateService,
    TopbarService,
    DateModel,
    DetailedKpiModel,
    RevenueDateChanged
  ) {

    TopbarService.setName("Total Revenue");
    $scope.context = new DetailedKpiModel(DateModel.startDate, DateModel.endDate, "Total Revenue");
      
    var KpiId = "revenue";

    $scope.updateChart("Total Revenue", $scope.context);
    $scope.updateOnChangedDate($scope.context, KpiId, "Total Revenue");

    $scope.$on(RevenueDateChanged, function(ev, data) {
      $scope.context.beginDate = DateModel.startDate;
      $scope.context.endDate = DateModel.endDate;
      $scope.updateOnChangedDate($scope.context, KpiId, "Total Revenue");
    });

}]);
