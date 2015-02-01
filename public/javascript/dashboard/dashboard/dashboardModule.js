'use strict';

var dashboard = angular.module('DashboardModule', [
    'ItemModule.services',
    'DashboardModule.services',
    'DashboardModule.controllers',
    'OverviewServices',
    'InventoryServices',
    'SettingsServices'
]);

dashboard.factory("KpiModel", ['HorizontalBarChartModel',function(HorizontalBarChartModel) {
  function KpiModel(name, link) {
    this.isCollapsed = true;
    this.name = name;
    this.link = link;
    this.delta = 0;
    this.previous = 0;
    this.value = 0;
    this.unitType = "€";
    this.css = "kpi-delta";
    this.icon = "fa fa-minus";
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
      var previous = data.previous;
      var DecimalPlaces = 2
      this.value = parseFloat(value.toFixed(DecimalPlaces));
      this.previous = parseFloat(previous.toFixed(DecimalPlaces));
      this.delta = parseFloat(delta.toFixed(DecimalPlaces));
      this.platforms = _.map(data.platforms, function(p) {
        p.value = parseFloat(p.value.toFixed(DecimalPlaces));
        p.previous = parseFloat(p.previous.toFixed(DecimalPlaces));
        p.delta = parseFloat(p.delta.toFixed(DecimalPlaces));
        return p;
      });
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

