'use strict';

dashboard
.controller('ArpuController', [
  '$scope',
  '$location',
  '$rootScope',
  'ApplicationStateService',
  'TopbarService',
  'GetMainKPIsService',
  'DateModel',
  'DetailedKpiModel',
  function (
    $scope,
    $location,
    $rootScope,
    ApplicationStateService,
    TopbarService,
    GetMainKPIsService,
    DateModel,
    DetailedKpiModel
  ) {
    TopbarService.setName("Average Revenue Per User");
    $scope.context = new DetailedKpiModel(DateModel.startDate, DateModel.endDate, "Average Revenue Per User");
    
    $scope.format = 'dd-MMMM-yyyy';
    $scope.today = function() {
      $scope.beginDate = $scope.context.beginDate;
      $scope.endDate = $scope.context.endDate;
    };
    $scope.today();
    
    $scope.toggleMin = function() {
      $scope.minDate = moment().subtract('years', 1).format('d-M-YYYY');
      $scope.endDateMin = $scope.beginDate;
    };
    $scope.toggleMin();

    $scope.updateEndDateMin = function(){
      $scope.endDateMin = $scope.beginDate;
    };
    
    $scope.maxDate = new Date();

    $scope.openBeginDate = function($event) {
      $event.preventDefault();
      $event.stopPropagation();
      $scope.beginDateOpened = true;
    };

    $scope.openEndDate = function($event) {
      $event.preventDefault();
      $event.stopPropagation();  
      $scope.endDateOpened = true;
    };

    $scope.initDate = $scope.today;
      
    var KpiId = "arpu";
      
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

    $scope.updateChart("Average Revenue Per User");

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
      $scope.updateChart("Average Revenue Per User");
    };
}]);
