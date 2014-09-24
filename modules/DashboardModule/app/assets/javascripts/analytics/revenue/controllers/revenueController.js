'use strict';

dashboard
.controller('RevenueController', [
  '$scope',
  '$location',
  '$rootScope',
  'ApplicationStateService',
  'TopbarService',
  'GetMainKPIsService',
  'LineChartConfiguration',
  'DateModel',
  'DetailedKpiModel',
  'RevenueDateChanged',
  function (
    $scope,
    $location,
    $rootScope,
    ApplicationStateService,
    TopbarService,
    GetMainKPIsService,
    LineChartConfiguration,
    DateModel,
    DetailedKpiModel,
    RevenueDateChanged
  ) {
    TopbarService.setName("Total Revenue");
    $scope.context = new DetailedKpiModel(DateModel.startDate, DateModel.endDate, "Total Revenue");
      
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

    $scope.updateChart("Total Revenue");

    $scope.updateOnChangedDate = function() {
      updateChartData();
      updateTotalValues();
    };
    
    $scope.$on(RevenueDateChanged, function(ev, data) {
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
      $scope.updateChart("Total Revenue");
    };
}]);
