'use strict';

dashboard.value("LineChartConfiguration", {
  fillColor : "rgba(151,187,205,0.5)",
  strokeColor : "rgba(151,187,205,1)",
  pointColor : "rgba(151,187,205,1)",
  pointStrokeColor : "#fff",
  data: []
});

dashboard
.controller('ArpuController', [
  '$scope',
  '$location',
  '$rootScope',
  'ApplicationStateService',
  'TopbarService',
  'GetMainKPIsService',
  'LineChartConfiguration',
  'DateModel',
  'ArpuModel',
  function (
    $scope,
    $location,
    $rootScope,
    ApplicationStateService,
    TopbarService,
    GetMainKPIsService,
    LineChartConfiguration,
    DateModel,
    ArpuModel
  ) {
    TopbarService.setName("Average Revenue Per User");
    $scope.context = new ArpuModel(DateModel.startDate, DateModel.endDate);
    
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
        DashboardModel.formatDate($scope.beginDate),
        DashboardModel.formatDate($scope.endDate),
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
        DashboardModel.formatDate($scope.beginDate),
        DashboardModel.formatDate($scope.endDate),
        KpiId
      ).then(function(results) {
        totalValueHandler(results);
      },function(err) {console.log(err);}
      );
    };

    updateChartData();
    updateTotalValues();

    var totalValueHandler = function(data) {
      $scope.context.model.value = data.data.value;
      $scope.context.model.delta = data.data.delta;
    };
      
    var kpiDataSuccessHandler = function(data) {
      $scope.context.updateChartData(data);
      $scope.updateChart("Average Revenue Per User");
    };
}]);
