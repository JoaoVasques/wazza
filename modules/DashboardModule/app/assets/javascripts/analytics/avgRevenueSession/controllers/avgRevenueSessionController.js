'use strict';

dashboard
.controller('AvgRevenueSessionController', [
  '$scope',
  '$location',
  '$rootScope',
  'ApplicationStateService',
  'TopbarService',
  'GetMainKPIsService',
  'DateModel',
  'DetailedKpiModel',
  'AvgRevenueSessionDateChanged',
  function (
    $scope,
    $location,
    $rootScope,
    ApplicationStateService,
    TopbarService,
    GetMainKPIsService,
    DateModel,
    DetailedKpiModel,
    AvgRevenueSessionDateChanged
  ) {
    TopbarService.setName("Average Revenue Per Session");
    $scope.context = new DetailedKpiModel(DateModel.startDate, DateModel.endDate, "Average Revenue Per Session");

    var KpiId = "avgRevenueSession";
      
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

    $scope.updateChart("Average Revenue Per Session");

    $scope.updateOnChangedDate = function() {
      updateChartData();
      updateTotalValues();
    };

    $scope.$on(AvgRevenueSessionDateChanged, function(ev, data) {
      $scope.context.beginDate = DateModel.startDate;
      $scope.context.endDate = DateModel.endDate;
      updateTotalValues();
      updateChartData();
    });

    var updateChartData = function() {
      GetMainKPIsService.getDetailedKPIData(
        ApplicationStateService.companyName,
        ApplicationStateService.applicationName,
        DateModel.formatDate($scope.context.beginDate),
        DateModel.formatDate($scope.context.endDate),
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
        DateModel.formatDate($scope.context.beginDate),
        DateModel.formatDate($scope.context.endDate),
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
      $scope.updateChart("Average Revenue Per Session");
    };
}]);
