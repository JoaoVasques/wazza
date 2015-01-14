'use strict';

wazzaCharts.factory("HorizontalBarDataValue", function(){
  function HorizontalBarDataValue(key, color, value) {
    this.key = key;
    this.color = color;
    this.values = [
      {
        "label": key,
        "value": value
      }
    ];
  };

  return HorizontalBarDataValue;
});

wazzaCharts.factory("HorizontalBarColors", function(){
  function Color(type, color) {
    this.type = type;
    this.color = color;
  }

  var colorsFactory = {
    colors: []
  };

  colorsFactory.colors.push(new Color("Total", "#DC7F11"));
  colorsFactory.colors.push(new Color("Android", "#2CA02C"));
  colorsFactory.colors.push(new Color("iOS", "#2980b9"));
  
  colorsFactory.getColor = function(type) {
    return _.find(colorsFactory.colors, function(t) {
      return t.type == type;
    });
  };
    
    
  return colorsFactory;
});

wazzaCharts.factory("HorizontalBarChartModel", [
  "HorizontalBarDataValue",
  "HorizontalBarColors",
  function(
    HorizontalBarDataValue,
    HorizontalBarColors
  ) {
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
        showLegend: false,
        showControls: false
      }
    };

    this.data = [];
  };

  HorizontalBarChartModel.prototype = {
    updateChartData: function(chartData, platforms) {
      var results = [];
      _.each(platforms, function(platform) {
        var value = _.find(chartData.platforms, function(p) {return p.platform == platform; }).value;
         results.push(new HorizontalBarDataValue(platform, HorizontalBarColors.getColor(platform), value));
      });
      results.push(new HorizontalBarDataValue("Total", HorizontalBarColors.getColor("Total"), chartData.value));
      this.data = results;
    }
  };
    
  return HorizontalBarChartModel;
}]);
