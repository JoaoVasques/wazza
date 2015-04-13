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

    /** ANDROID COLORS **/
    var androidIAPColor = {
      fillColor: 'rgba(39, 174, 96, 0.2)',
      strokeColor: 'rgba(39, 174, 96,1.0)',
      pointColor: 'rgba(39, 174, 96,1.0)',
      pointStrokeColor: '#fff',
      pointHighlightFill: '#fff',
      pointHighlightStroke: 'rgba(39, 174, 96, 0.8)'
    };
    
    var androidPayPalColor = {
      fillColor: 'rgba(24, 104, 57, 0.2)',
      strokeColor: 'rgba(24, 104, 57, 1.0)',
      pointColor: 'rgba(24, 104, 57, 1.0)',
      pointStrokeColor: '#fff',
      pointHighlightFill: '#fff',
      pointHighlightStroke: 'rgba(24, 104, 57, 0.8)'
    };

    var androidColors = {
      1: androidIAPColor,
      2: androidPayPalColor
    };

    /** IOS COLORS **/
      
    /** In-App Purchase Color **/
    var iOSIAPColor = {
      fillColor: 'rgba(79, 162, 216, 0.2)',
      strokeColor: 'rgba(79, 162, 216, 1.0)',
      pointColor: 'rgba(79, 162, 216, 1.0)',
      pointStrokeColor: '#fff',
      pointHighlightFill: '#fff',
      pointHighlightStroke: 'rgba(79, 162, 216, 0.8)'
    };

    /** PayPal Color **/
    var iOSPayPalColor = {
      fillColor: 'rgba(10, 81, 127, 0.2)',
      strokeColor: 'rgba(10, 81, 127, 1.0)',
      pointColor: 'rgba(10, 81, 127, 1.0)',
      pointStrokeColor: '#fff',
      pointHighlightFill: '#fff',
      pointHighlightStroke: 'rgba(10, 81, 127, 0.8)'
    };

    var iOSColors = {
      1: iOSIAPColor,
      2: iOSPayPalColor
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
      "Android": androidColors,
      "iOS": iOSColors
    };

    this._paymentSystems = {
        1: "In-App Purchases",
        2: "PayPal"
    };
  };

  LineChartModel.prototype = {
    updateColours: function(platform, paymentSystem) {
      if(paymentSystem == null) {
          // total color
        if(!_.contains(this.colours, platform)) {
          this.colours.push(this._colours[platform]);
        }
      } else {
        // a platform's payment system
        var key = "[" + platform + "] " + this._paymentSystems[paymentSystem];
        if(!_.contains(this.colours, key)) {
          this.colours.push(this._colours[platform][paymentSystem]);
        }
      }
    },
    updateChartData: function(chartData, platforms) {
      //console.log(chartData);
      //console.log(platforms);
      var _this = this;
      _this.data = [];
      _this.series = [];
      _this.labels = [];
      _this.colours = [];

      /** Create array of arrays **/
      var nrPaymentSystems = chartData.data[0].platforms[0].paymentSystems.length;
      var nrPlatforms = chartData.data[0].platforms.length;
      var nrSeries = (nrPlatforms * nrPaymentSystems) + 1;
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
            switch(nrPlatforms) {
              case 1: /** Only one platform **/
                  index = 1;
                break;
              case 2:  /** Multiple platform **/
                  var androidIndex = 1;
                  var iOSIndex = androidIndex + nrPaymentSystems;
                  index = (platform == "Android") ? androidIndex : iOSIndex;              
                break;
            }
            /** Payment Systems values **/
            _.find(platformInfo.paymentSystems, function(systemsInfo) {
                resultsArray[index+systemsInfo.system-1].push(Math.round(platformInfo.value * 100) / 100);
                var key = "[" + dataKey + "] " + _this._paymentSystems[systemsInfo.system];
                if(!_.contains(_this.series, key)) {
                    _this.series.push(key);
                }
              _this.updateColours(platform, systemsInfo.system)
            });
          };
          
          isTotal ? resultsArray[0].push(Math.round(data.value * 100) / 100) : getPlatformValue(dataKey);
          if(!_.contains(_this.series, dataKey) && isTotal) {
            _this.series.push(dataKey);
            _this.updateColours(dataKey, null)
          }
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

