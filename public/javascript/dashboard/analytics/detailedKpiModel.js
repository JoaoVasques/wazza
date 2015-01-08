'use strict';

dashboard.factory('DetailedKpiModel', ['KpiModel', function(KpiModel) {
     
  function DetailedKpiModel(beginDate, endDate, name) {
    this.beginDate = beginDate;
    this.endDate = endDate;
    this.chart = {
      options: {
        chart: {
          type: 'cumulativeLineChart',
          height: 500,
          margin : {
            top: 20,
            right: 10,
            bottom: 60,
            left: 65
          },
          x: function(d){ return d[0]; },
          y: function(d){ return d[1]/100; },
          color: ["#DC7F11", "#2980b9", "#2CA02C"],//Colors: [Total, iOS, Android],
          transitionDuration: 300,
          useInteractiveGuideline: true,
          clipVoronoi: false,

          xAxis: {
            axisLabel: 'Days',
            tickFormat: function(d) {
              return d3.time.format('%m/%d/%y')(new Date(d))
            },
            showMaxMin: true,
            staggerLabels: true
          },

          yAxis: {
            axisLabel: name,
            tickFormat: function(d){
              return d3.format(',.1')(d);
            },
            axisLabelDistance: 20,
            showMaxMin: false
          },
          forceY: [0, 1]
        }
      },
      data: []
    };
    this.model = new KpiModel(name, "");
  };

  DetailedKpiModel.prototype = {
    updateDates: function(begin, end) {
      this.beginDate = begin;
      this.endDate = end;
    },
    updateChartData: function(chartData, platforms) {
      var _this = this;
      var seriesExist = function(key, arr) {
        if(_.isEmpty(arr)) {
          return false;
        } else {
          var result = _.find(arr, function(el){
            return el.key == key;
          });
          return result === undefined ? false : true;
        }
      };

      var addDataToChart = function(data){
        var day = data.day;
        var updateEntry = function(dataKey, isTotal){
          var getPlatformValue = function(platform) {
            return _.find(data.platforms, function(p){
              p.platform == platform;
            }).value;
          };
          // If the series does not exist in D3 data, create it
          if(!seriesExist(dataKey, _this.chart.data)) {
            var vals = [];
            isTotal ? vals.push([day, data.value]) : vals.push([day, getPlatformValue(dataKey)]);
            var obj = {
              key: dataKey,
              values: vals,
            };
            _this.chart.data.push(obj); // update D3 chart data model
          } else {
            // find and update existing serie's values
            var getDataValues = function(key) {
              return _.find(_this.chart.data, function(el){
                  return el.key == key;
              }).values;
            };
            isTotal ? getDataValues(dataKey).push([day, data.value]) : getDataValues(dataKey).push([day, getPlatformValue(dataKey)]);
          }
        };
        updateEntry("Total", true);
        _.each(platforms, function(p) {
          updateEntry(p, true);
        });
      };

      _.each(chartData.data, addDataToChart);
    }
  };
    
  return DetailedKpiModel;
}]);
