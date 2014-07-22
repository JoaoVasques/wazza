'use strict';

dashboard
.controller('ChurnController', [
  '$scope',
  '$location',
  '$rootScope',
  'FetchItemsService',
  'BootstrapDashboardService',
  'DeleteItemService',
  'ApplicationStateService',
  'ItemSearchService',
  'TopbarService',
  'ChurnService',
  function (
        $scope,
        $location,
        $rootScope,
        FetchItemsService,
        BootstrapDashboardService,
        DeleteItemService,
        ApplicationStateService,
        ItemSearchService,
        TopbarService,
        ChurnService
    ) {

        TopbarService.setName("Churn - Details");

        $scope.options = {
          axes: {
            x: {key: 'x', labelFunction: function(value) {return value;}, type: 'linear'},
            y: {type: 'linear', min: 0, max: 100},
            y2: {type: 'linear', min: 0, max: 100}
          },
          series: [
            {y: 'value', color: 'steelblue', thickness: '2px', type: 'area', striped: true, label: 'Pouet'},
            {y: 'otherValue', axis: 'y2', color: 'lightsteelblue', visible: false, drawDots: true}
          ],
          lineMode: 'linear',
          tension: 0.7,
          tooltip: {mode: 'scrubber', formatter: function(x, y, series) {return 'pouet';}},
          drawLegend: true,
          drawDots: true,
          columnsHGap: 5
        }

        $scope.data = [
          {x: 0, value: 4, otherValue: 14},
          {x: 1, value: 8, otherValue: 1},
          {x: 2, value: 15, otherValue: 11},
          {x: 3, value: 16, otherValue: 147},
          {x: 4, value: 23, otherValue: 87},
          {x: 5, value: 42, otherValue: 45}
        ];

        ChurnService.execute($scope.companyName, $scope.applicationName, "2014-07-22", "2014-07-22")
            .then(function(results) {
                console.log(results);
                //$scope.data = results;
        });


}])

.factory('ChurnService', ['$http', '$q',
    function($http, $q) {
      var service = {};

      service.execute = function(companyName, applicationName, startDate, endDate) {
        var totalUrl = '/analytics/churn/total/' + companyName + '/' + applicationName + '/'+ startDate +'/' + endDate;
        var totalChurn = $http({
            url: totalUrl,
            method: 'GET'
        });

        var detailUrl = '/analytics/churn/detail/' + companyName + '/' + applicationName + '/'+ startDate +'/' + endDate;
        var detailChurn = $http({
            url: detailUrl,
            method: 'GET'
        });

        return $q.all([totalChurn, detailChurn]);
      };

      return service;
}])