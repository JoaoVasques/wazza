'use strict';

dashboard
.controller('RevenueController', [
  '$scope',
  '$rootScope',
  'ApplicationStateService',
  'DateModel',
  'DetailedKpiModel',
  'RevenueDateChanged',
  'RevenuePlatformsChanged',
  function (
    $scope,
    $rootScope,
    ApplicationStateService,
    DateModel,
    DetailedKpiModel,
    RevenueDateChanged,
    RevenuePlatformsChanged
  ) {

    var title = "Total Revenue";
    var KpiId = "revenue";

    var seriesExist = function(key, arr) {
      if(_.isEmpty(arr)) {
          return false;
      } else {
        var result = _.find(arr, function(el){
          return el.key == key;
        });
          return result === undefined ? false : true;
      }
    };

    var removeSeries = function(arr, k) {
      if(seriesExist(k, arr)) {
        var element = _.findWhere(arr, {key: k});
        var arr = _.without(arr, element);
        return _.without(arr, _.findWhere(arr, {key: k}));
      }
    };
      
    ApplicationStateService.setPath(title);
    $scope.context = new DetailedKpiModel(DateModel.startDate, DateModel.endDate, title);

    $scope.updateChart(title, $scope.context);
    $scope.updateData($scope.context, KpiId, title);

    $scope.$on(RevenueDateChanged, function(ev, data) {
      $scope.context.beginDate = DateModel.startDate;
      $scope.context.endDate = DateModel.endDate;
      $scope.updateData($scope.context, KpiId, title);
    });

    $scope.$on(RevenuePlatformsChanged, function(ev, data) {
      $scope.updateData($scope.context, KpiId, title);
      console.log(data);
      if(!data.value) {
        $scope.context.removeSerieFromChart(data.platform);
        if(seriesExist(data.platform, $scope.data)) {
          $scope.data = removeSeries($scope.data, data.platform);
        }
      }
    })
}]);

