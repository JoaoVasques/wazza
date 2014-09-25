'use strict';

dashboard
.controller('ChurnController', [
  '$scope',
  '$location',
  '$rootScope',
  'ApplicationStateService',
  'TopbarService',
  'DateModel',
  'DetailedKpiModel',
  'ChurnDateChanged',
  function (
    $scope,
    $location,
    $rootScope,
    ApplicationStateService,
    TopbarService,
    DateModel,
    DetailedKpiModel,
    ChurnDateChanged
  ) {

    var title = "Churn Rate";
    var KpiId = "churn";

    TopbarService.setName(title);
    $scope.context = new DetailedKpiModel(DateModel.startDate, DateModel.endDate, title);

    $scope.updateChart(title, $scope.context);
    $scope.updateOnChangedDate($scope.context, KpiId, title);

    $scope.$on(ChurnDateChanged, function(ev, data) {
      $scope.context.beginDate = DateModel.startDate;
      $scope.context.endDate = DateModel.endDate;
      $scope.updateOnChangedDate($scope.context, KpiId, title);
    });

}]);
