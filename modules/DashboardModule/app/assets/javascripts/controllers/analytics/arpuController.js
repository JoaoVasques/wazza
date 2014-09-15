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
  function (
    $scope,
    $location,
    $rootScope,
    ApplicationStateService,
    TopbarService,
    GetKPIService,
    LineChartConfiguration,
    DateModel
  ) {
    TopbarService.setName("ARPU - Details");

    $scope.beginDate = DateModel.startDate;
    $scope.endDate = DateModel.endDate;

    var KpiId = "arpu";
      
    $scope.updateChart = function(name, labels, values) {
      $scope.chart = {
        "data": {
          "labels": labels,
          "datasets": [
            {
              fillColor : "rgba(151,187,205,0.5)",
              strokeColor : "rgba(151,187,205,1)",
              pointColor : "rgba(151,187,205,1)",
              pointStrokeColor : "#fff",
              data : values
            }
          ]
        },
        "options": {"width": '100%'}
      };
    };
    $scope.updateChart("Average Revenue Per User", [],[]);
      
    GetKPIService.getDetailedKPIData(
      ApplicationStateService.companyName,
      ApplicationStateService.applicationName,
      DateModel.formatDate($scope.beginDate),
      DateModel.formatDate($scope.endDate),
      KpiId
    ).then(function(results) {
      kpiDataSuccessHandler(results);
    },function(err) {console.log(err);}
    );
      
    var kpiDataSuccessHandler = function(data) {
      var labels = [];
      var values = [];
      _.each(data.data, function(element) {
        labels.push(element.day);
        values.push(element.val);
      });

      $scope.updateChart("Average Revenue Per User", labels, values);
    };
}]);
