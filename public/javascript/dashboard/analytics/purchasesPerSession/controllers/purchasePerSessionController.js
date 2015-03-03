'use strict';

dashboard
.controller('PurchasePerSessionController', [
  '$scope',
  '$rootScope',
  'ApplicationStateService',
  'DateModel',
  'DetailedKpiModel',
  'PurchaseDateChanged',
  'PurchaseDatePlatformsChanged',
  function (
    $scope,
    $rootScope,
    ApplicationStateService,
    DateModel,
    DetailedKpiModel,
    PurchaseDateChanged,
    PurchaseDatePlatformsChanged
  ) {

    var title = "Purchases";
    var KpiId = "purchasesPerSession";

    ApplicationStateService.setPath(title);
    $scope.buildContext(new DetailedKpiModel(DateModel.startDate, DateModel.endDate, title));

    $scope.updateData($scope.context, KpiId, title);

    $scope.$on(PurchaseDateChanged, function(ev, data) {
      $scope.context.beginDate = DateModel.startDate;
      $scope.context.endDate = DateModel.endDate;
      $scope.updateData($scope.context, KpiId, title);
    });

    $scope.$on(PurchaseDatePlatformsChanged, function(ev, data) {
      $scope.updateData($scope.context, KpiId, title);
    });
  }]);

