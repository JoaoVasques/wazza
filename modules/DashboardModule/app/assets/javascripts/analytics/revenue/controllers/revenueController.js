'use strict';

dashboard.value("LineChartConfiguration", {
  fillColor : "rgba(151,187,205,0.5)",
  strokeColor : "rgba(151,187,205,1)",
  pointColor : "rgba(151,187,205,1)",
  pointStrokeColor : "#fff",
  data: []
});

dashboard
.controller('RevenueController', [
  '$scope',
  '$location',
  '$rootScope',
  'FetchItemsService',
  'BootstrapDashboardService',
  'DeleteItemService',
  'ApplicationStateService',
  'ItemSearchService',
  'TopbarService',
  'GetMainKPIsService',
  'DateModel',
  'DetailedKpiModel',
  'LineChartConfiguration',
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
        GetKPIService,
        GetMainKPIsService,
        DateModel,
        DetailedKpiModel,
        LineChartConfiguration
  ) {

        TopbarService.setName("Revenue - Details");
        $scope.context = new DetailedKpiModel(DateModel.startDate, DateModel.endDate);

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

        GetKPIService.execute($scope.companyName, $scope.applicationName, "2014-07-22", "2014-07-22", "revenue")
            .then(function(results) {
                console.log(results);
                //$scope.data = results;
        });


}]);
