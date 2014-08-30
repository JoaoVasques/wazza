'use strict';

dashboard.value("LineChartConfiguration", {
  fillColor: "rgba(220,220,220,0.2)",
  strokeColor: "rgba(220,220,220,1)",
  pointColor: "rgba(220,220,220,1)",
  pointStrokeColor: "#fff",
  pointHighlightFill: "#fff",
  pointHighlightStroke: "rgba(220,220,220,1)"
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
  function (
    $scope,
    $location,
    $rootScope,
    ApplicationStateService,
    TopbarService,
    GetKPIService,
    LineChartConfiguration
  ) {
    TopbarService.setName("ARPU - Details");

    $scope.chartData = {
      labels: [
        '20 Aug',
        '21 Aug',
        '22 Aug',
        '23 Aug',
        '24 Aug',
        '25 Aug',
        '26 Aug',
        '27 Aug'
      ],
      datasets: [
        {
          label: "BUAA",
          fillColor: "rgba(220,220,220,0.2)",
          strokeColor: "rgba(220,220,220,1)",
          pointColor: "rgba(220,220,220,1)",
          pointStrokeColor: "#fff",
          pointHighlightFill: "#fff",
          pointHighlightStroke: "rgba(220,220,220,1)",
          data: [
            20,
            50,
            10,
            70,
            25,
            61,
            40,
            100
          ]
      }
      ]
    };
      
    /**$scope.chartData = {
      labels: [],
      datasets: [
        {
          label: "BUAA",
          fillColor: "rgba(220,220,220,0.2)",
          strokeColor: "rgba(220,220,220,1)",
          pointColor: "rgba(220,220,220,1)",
          pointStrokeColor: "#fff",
          pointHighlightFill: "#fff",
          pointHighlightStroke: "rgba(220,220,220,1)",
          data: []
        }
      ]
    };**/
/**
  
    /*GetKPIService.getDetailedKPIData(
      ApplicationStateService.companyName,
      ApplicationStateService.applicationName,
      "20-08-2014",
      "27-08-2014",
      "arpu"  
    ).then(function(results) {
      kpiDataSuccessHandler(results);
    },function(err) {console.log(err);}
    );*/

    /**
    $rootScope.$on('$routeChangeStart', function () {
      alert('refresh');
    });**/
   
    var kpiDataSuccessHandler = function(data) {
      _.each(data.data, function(element) {
        $scope.chartData.labels.push(element.day);
        $scope.chartData.datasets[0].data.push(element.val);
      });
      console.log($scope.chartData);
    };
      
}]);
