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
        $scope.context = new DetailedKpiModel(DateModel.startDate, DateModel.endDate, "Revenue - Details");

    var KpiId = "revenue";

    $scope.updateChart = function(name) {
      $scope.chart = {
        "data": {
          "labels": $scope.context.labels,
          "datasets": [
            {
              fillColor : "rgba(151,187,205,0.5)",
              strokeColor : "rgba(151,187,205,1)",
              pointColor : "rgba(151,187,205,1)",
              pointStrokeColor : "#fff",
              data : $scope.context.values
            }
          ]
        },
        "options": {"width": 800}
      };
    };

    $scope.updateChart("Revenue - Details");

    $scope.updateOnChangedDate = function() {
      updateChartData();
      updateTotalValues();
    };

    var updateChartData = function() {
      GetMainKPIsService.getDetailedKPIData(
        ApplicationStateService.companyName,
        ApplicationStateService.applicationName,
        DateModel.formatDate($scope.beginDate),
        DateModel.formatDate($scope.endDate),
        KpiId
      ).then(function(results) {
        kpiDataSuccessHandler(results);
      },function(err) {console.log(err);}
      );
    };

    var updateTotalValues = function() {
      GetMainKPIsService.getTotalKpiData(
        ApplicationStateService.companyName,
        ApplicationStateService.applicationName,
        DateModel.formatDate($scope.beginDate),
        DateModel.formatDate($scope.endDate),
        KpiId
      ).then(function(results) {
        totalValueHandler(results);
      },function(err) {console.log(err);}
      );
    };

    updateChartData();
    updateTotalValues();

    var totalValueHandler = function(data) {
      $scope.context.model.updateKpiValue(data.data.value, data.data.delta);
    };

    var kpiDataSuccessHandler = function(data) {
      $scope.context.updateChartData(data);
      $scope.updateChart("Revenue - Details");
    };

}]);
