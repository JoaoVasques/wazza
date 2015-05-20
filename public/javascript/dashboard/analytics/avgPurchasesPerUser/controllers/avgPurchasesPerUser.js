'use strict';

dashboard
.controller('AvgPurchasesPerUserController', [
  '$scope',
  '$rootScope',
  'ApplicationStateService',
  'DateModel',
  'DetailedKpiModel',
  'PurchasesPerUserChanged',
  'PurchasesPerUserPlatformsChanged',
  function (
    $scope,
    $rootScope,
    ApplicationStateService,
    DateModel,
    DetailedKpiModel,
    PurchasesPerUserChanged,
    PurchasesPerUserPlatformsChanged
  ) {

    var title = "Average Purchases per User";
    var KpiId = "avgPurchasesUser";

    ApplicationStateService.setPath(title);
    $scope.buildContext(new DetailedKpiModel(DateModel.startDate, DateModel.endDate, title));

    $scope.updateData($scope.context, KpiId, title);

    $scope.$on(PurchasesPerUserChanged, function(ev, data) {
      $scope.context.beginDate = DateModel.startDate;
      $scope.context.endDate = DateModel.endDate;
      $scope.updateData($scope.context, KpiId, title);
    });

    $scope.$on(PurchasesPerUserPlatformsChanged, function(ev, data) {
      $scope.updateData($scope.context, KpiId, title);
    });
}]);

