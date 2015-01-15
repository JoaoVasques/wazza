'use strict';

var dashboard = angular.module('DashboardModule', [
    'ItemModule.services',
    'DashboardModule.services',
    'DashboardModule.controllers',
    'chartjs-directive',
    'OverviewServices',
    'InventoryServices',
    'SettingsServices'
]);

dashboard.factory("KpiModel", ['HorizontalBarChartModel',function(HorizontalBarChartModel) {
    function KpiModel(name, link) {
    this.name = name;
    this.link = link;
    this.delta = 0;
    this.previous = 0;
    this.value = 0;
    this.unitType = "€";
    this.css = "kpi-delta";
    this.icon = "glyphicon glyphicon-minus";
    this.platforms = [];
    this.multiPlatform = true;
    this.chart = new HorizontalBarChartModel(1)
  };

  KpiModel.prototype = {
    updateChartData: function(chartData, platforms) {
      this.chart.updateChartData(chartData, platforms);
    },  
    updateKpiValue: function(data) {
      var value = data.value;
      var delta = data.delta;
      var DecimalPlaces = 2
      this.value = value.toFixed(DecimalPlaces);
      this.delta = delta;
      this.platforms = data.platforms;
      this.updateChartData(data, _.map(data.platforms, function(p) {return p.platform;}));
    },
    updateUnitType: function(newType) {
      this.unitType = newType;
    },
    addPlatform: function(p) {
      if(!_.contains(this.platforms, p)) {
        this.platforms.push(p);
      }
    },
    removePlatform: function(p) {
      this.platforms = _.without(this.platforms, p);
    },
    
  };

  return KpiModel;
}]);

