'use strict';

dashboard
.controller('PayingUsersController', [
  '$scope',
  '$location',
  '$rootScope',
  'ApplicationStateService',
  'DateModel',
  'DetailedKpiModel',
  'PayingUsersDateChanged',
  function (
    $scope,
    $location,
    $rootScope,
    ApplicationStateService,
    DateModel,
    DetailedKpiModel,
    PayingUsersDateChanged
  ) {

    var title = "Paying Users";
    var KpiId = "payingUsers";

    ApplicationStateService.setPath(title);
    $scope.context = new DetailedKpiModel(DateModel.startDate, DateModel.endDate, title);

    $scope.updateChart(title, $scope.context);
    $scope.updateOnChangedDate($scope.context, KpiId, title);

    $scope.$on(PayingUsersDateChanged, function(ev, data) {
      $scope.context.beginDate = DateModel.startDate;
      $scope.context.endDate = DateModel.endDate;
      $scope.updateOnChangedDate($scope.context, KpiId, title);
    });

}]);
