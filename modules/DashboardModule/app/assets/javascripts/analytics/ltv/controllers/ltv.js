'use strict';

dashboard
.controller('LifeTimeValueController', [
  '$scope',
  '$location',
  '$rootScope',
  'ApplicationStateService',
  'TopbarService',
  'DateModel',
  'DetailedKpiModel',
  'LtvDateChanged',
  function (
    $scope,
    $location,
    $rootScope,
    ApplicationStateService,
    TopbarService,
    DateModel,
    DetailedKpiModel,
    LtvDateChanged
  ) {

    var title = "Lifetime Value";
    var KpiId = "ltv";

    TopbarService.setName(title);
    $scope.context = new DetailedKpiModel(DateModel.startDate, DateModel.endDate, title);

    $scope.updateChart(title, $scope.context);
    $scope.updateOnChangedDate($scope.context, KpiId, title);

    $scope.$on(LtvDateChanged, function(ev, data) {
      $scope.context.beginDate = DateModel.startDate;
      $scope.context.endDate = DateModel.endDate;
      $scope.updateOnChangedDate($scope.context, KpiId, title);
    });

}]);
