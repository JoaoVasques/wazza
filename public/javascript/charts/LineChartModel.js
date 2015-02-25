'use strict';

wazzaCharts.factory('LineChartModel', function(){

  function LineChartModel(name) {
    var totalColor = {
      fillColor: 'rgba(211, 84, 0, 0.2)',
      strokeColor: 'rgba(211, 84, 0,1.0)',
      pointColor: 'rgba(211, 84, 0,1.0)',
      pointStrokeColor: '#fff',
      pointHighlightFill: '#fff',
      pointHighlightStroke: 'rgba(211, 84, 0, 0.8)'
    };
      
    var androidColor = {
      fillColor: 'rgba(39, 174, 96, 0.2)',
      strokeColor: 'rgba(39, 174, 96,1.0)',
      pointColor: 'rgba(39, 174, 96,1.0)',
      pointStrokeColor: '#fff',
      pointHighlightFill: '#fff',
      pointHighlightStroke: 'rgba(39, 174, 96, 0.8)'
    };

    var iOSColor = {
      fillColor: 'rgba(41, 128, 185, 0.2)',
      strokeColor: 'rgba(41, 128, 185,1.0)',
      pointColor: 'rgba(41, 128, 185,1.0)',
      pointStrokeColor: '#fff',
      pointHighlightFill: '#fff',
      pointHighlightStroke: 'rgba(41, 128, 185, 0.8)'
    };
      
    this.name = name;
    this.labels = [];
    this.series = [];
    this.data = [];
    this.colours = [];
    this.options = {
      bezierCurve : false
    },
    this._colours = {
      "Total": totalColor,
      "Android": androidColor,
      "iOS": iOSColor
    }
  };

  LineChartModel.prototype = {
    updateColours: function(colorID) {
      if(!_.contains(this._colours, colorID)) {
        this.colours.push(this._colours[colorID]);
      }
    },
    updateChartData: function(chartData, platforms) {
      var _this = this;
      _this.data = [];
      _this.series = [];
      _this.labels = [];
      _this.colours = [];
        
      /** Create array of arrays **/
      var nrSeries = chartData.data[0].platforms.length + 1;
      var resultsArray = [];
      for(var i = 0; i < nrSeries; i++) {
        resultsArray[i] = [];
      }
      
      var addDataToChart = function(data){
        if(!_.contains(_this.labels, data.day)) {
          var day = moment(data.day).format("Do MMM");
          _this.labels.push(day);
        }
        var updateEntry = function(dataKey, isTotal){
          var getPlatformValue = function(platform) {
            var platformInfo = _.find(data.platforms, function(p){
              return p.platform == platform;
            });
            var index = -1;
            switch(nrSeries) {
              case 2: /** Only one platform **/
                  index = 1;
                break;
              case 3:  /** Multiple platform **/
                  var androidIndex = 1;
                  var iOSIndex = 2;
                  index = (platform == "Android") ? androidIndex : iOSIndex;              
                break;
            }
            resultsArray[index].push(platformInfo.value);
          };
          
          isTotal ? resultsArray[0].push(data.value) : getPlatformValue(dataKey);
          if(!_.contains(_this.series, dataKey)) {
            _this.series.push(dataKey);
          }
          _this.updateColours(dataKey);
        };
        updateEntry("Total", true);
        _.each(platforms, function(p) {
          updateEntry(p, false);
        });
      };
      _.each(chartData.data, addDataToChart);
      _this.data = resultsArray;
    },
    removeSeries: function(k) {
      if(seriesExist(k, this.data)) {
        var element = _.findWhere(this.data, {key: k});
        var arr = _.without(this.data, element);
        this.data = _.without(this.data, _.findWhere(this.data, {key: k}));
      }
    }
  };

  return LineChartModel;
});

