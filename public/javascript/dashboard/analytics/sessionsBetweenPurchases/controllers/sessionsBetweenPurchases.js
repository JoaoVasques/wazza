'use strict';

dashboard
.controller('NumberSessionsBetweenPurchaseController', [
  '$scope',
  '$rootScope',
  'ApplicationStateService',
  'DateModel',
  'DetailedKpiModel',
  'ATBPDateChanged',
  'ATBPPlatformsChanged',
  function (
    $scope,
    $rootScope,
    ApplicationStateService,
    DateModel,
    DetailedKpiModel,
    ATBPDateChanged,
    ATBPPlatformsChanged
  ) {

    var title = "Number of Sessions Between Purchases";
    var KpiId = "sessionsBetweenPurchases";

    ApplicationStateService.setPath(title);
    $scope.buildContext(new DetailedKpiModel(DateModel.startDate, DateModel.endDate, title));

    $scope.updateData($scope.context, KpiId, title);

    $scope.$on(ATBPDateChanged, function(ev, data) {
      $scope.context.beginDate = DateModel.startDate;
      $scope.context.endDate = DateModel.endDate;
      $scope.updateData($scope.context, KpiId, title);
    });
    
    $scope.$on(ATBPPlatformsChanged, function(ev, data) {
      $scope.updateData($scope.context, KpiId, title);
    });
}]);

