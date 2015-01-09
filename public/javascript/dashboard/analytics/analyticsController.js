'use strict';

dashboard
.controller('AnalyticsController', [
  '$scope',
  '$rootScope',
  'ApplicationStateService',
  'GetKPIService',
  'DateModel',
  'DetailedKpiModel',
  function (
    $scope,
    $rootScope,
    ApplicationStateService,
    GetKPIService,
    DateModel,
    DetailedKpiModel
  ) {

    $scope.updateOnChangedDate = function(context, KpiId, label) {
      updateChartData(context, KpiId, label);
      updateTotalValues(context, KpiId);
    };

    var updateChartData = function(context, KpiId, label) {
      GetKPIService.getDetailedKPIData(
        ApplicationStateService.getCompanyName(),
        ApplicationStateService.getApplicationName(),
        DateModel.formatDate(context.beginDate),
        DateModel.formatDate(context.endDate),
        KpiId,
        ApplicationStateService.selectedPlatforms
      ).then(function(results) {
        kpiDataSuccessHandler(results, context, label);
      },function(err) {console.log(err);}
      );
    };

    var updateTotalValues = function(context, KpiId) {
      GetKPIService.getTotalKpiData(
        ApplicationStateService.getCompanyName(),
        ApplicationStateService.getApplicationName(),
        DateModel.formatDate(context.beginDate),
        DateModel.formatDate(context.endDate),
        KpiId,
        ApplicationStateService.selectedPlatforms
      ).then(function(results) {
        totalValueHandler(context, results);
      },function(err) {console.log(err);}
      );
    };

    var totalValueHandler = function(context, data) {
      context.model.updateKpiValue(data.data);
    };

    var kpiDataSuccessHandler = function(data, context, label) {
      context.updateChartData(data, ApplicationStateService.selectedPlatforms);
    };

    $scope.updateChart = function(name, context){
      $scope.options = context.chart.options;
      $scope.data = context.chart.data;
    };
 }]);

