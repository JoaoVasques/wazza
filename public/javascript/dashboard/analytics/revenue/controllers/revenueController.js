'use strict';

dashboard
.controller('RevenueController', [
  '$scope',
  '$rootScope',
  'ApplicationStateService',
  'DateModel',
  'DetailedKpiModel',
  'RevenueDateChanged',
  function (
    $scope,
    $rootScope,
    ApplicationStateService,
    DateModel,
    DetailedKpiModel,
    RevenueDateChanged
  ) {

    var title = "Total Revenue";
    var KpiId = "revenue";

    ApplicationStateService.setPath(title);
    $scope.context = new DetailedKpiModel(DateModel.startDate, DateModel.endDate, title);

    $scope.updateChart(title, $scope.context);
    $scope.updateOnChangedDate($scope.context, KpiId, title);

    // $scope.$on(RevenueDateChanged, function(ev, data) {
    //   $scope.context.beginDate = DateModel.startDate;
    //   $scope.context.endDate = DateModel.endDate;
    //   $scope.updateOnChangedDate($scope.context, KpiId, title);
    // });
}]);

