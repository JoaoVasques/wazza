'use strict';

dashboard
.controller('AnalyticsController', [
  '$scope',
  '$rootScope',
  'ApplicationStateService',
  'GetMainKPIsService',
  'LineChartConfiguration',
  'DateModel',
  'DetailedKpiModel',
  function (
    $scope,
    $rootScope,
    ApplicationStateService,
    GetMainKPIsService,
    LineChartConfiguration,
    DateModel,
    DetailedKpiModel
  ) {

    $scope.updateChart = function(name, context) {
      $scope.chart = {
        "data": {
          "labels": context.labels,
          "datasets": [
            {
              fillColor : "rgba(151,187,205,0.5)",
              strokeColor : "rgba(151,187,205,1)",
              pointColor : "rgba(151,187,205,1)",
              pointStrokeColor : "#fff",
              data : context.values
            }
          ]
        },
        "options": {"width": 800}
      };
    };

    $scope.updateOnChangedDate = function(context, KpiId, label) {
      updateChartData(context, KpiId, label);
      updateTotalValues(context, KpiId);
    };

    var updateChartData = function(context, KpiId, label) {
      GetMainKPIsService.getDetailedKPIData(
        ApplicationStateService.getCompanyName(),
        ApplicationStateService.getApplicationName(),
        DateModel.formatDate(context.beginDate),
        DateModel.formatDate(context.endDate),
        KpiId
      ).then(function(results) {
        kpiDataSuccessHandler(results, context, label);
      },function(err) {console.log(err);}
      );
    };

    var updateTotalValues = function(context, KpiId) {
      GetMainKPIsService.getTotalKpiData(
        ApplicationStateService.getCompanyName(),
        ApplicationStateService.getApplicationName(),
        DateModel.formatDate(context.beginDate),
        DateModel.formatDate(context.endDate),
        KpiId
      ).then(function(results) {
        totalValueHandler(context, results);
      },function(err) {console.log(err);}
      );
    };

    var totalValueHandler = function(context, data) {
      context.model.updateKpiValue(data.data.value, data.data.delta);
    };

    var kpiDataSuccessHandler = function(data, context, label) {
      context.updateChartData(data);
      $scope.updateChart(label, context);
    };
      
}]);
