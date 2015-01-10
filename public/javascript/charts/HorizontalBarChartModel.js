'use strict';

wazzaCharts.factory("HorizontalBarChartModel", function(){
  function HorizontalBarChartModel(xpto) {
    this.options = {
      chart: {
        type: 'multiBarHorizontalChart',
        height: 250,
        x: function(d){return d.label;},
        y: function(d){return d.value;},
        showControls: true,
        showValues: true,
        transitionDuration: 500,
        xAxis: {
          showMaxMin: false
        },
        yAxis: {
          tickFormat: function(d){
            return d3.format(',.2f')(d);
          }
        },
        showLegend: false
      }
    };

    this.data = [
      {
        "key": "Android",
        "color": "#2CA02C",
        "values": [
          {
            "label" : "Android" ,
            "value" : 18.746444827653
          }
        ]
      },
      {
        "key": "iOS",
        "color": "#2980b9",
        "values": [
          {
            "label" : "iOS" ,
            "value" : 25.307646510375
          }
        ]
      },
      {
        "key": "Total",
        "color": "#DC7F11",
        "values": [
          {
            "label" : "Total" ,
            "value" : 40.307646510375
          }
        ]
      }
    ];
  };

  HorizontalBarChartModel.prototype = {
    updateChartData: function(chartData, platforms) {
      console.log("updateChartData");
      console.log(chartData);
      console.log(platforms);
    }
  };
    
  return HorizontalBarChartModel;
});
