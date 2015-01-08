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

dashboard.value("LineChartConfiguration", {
  fillColor : "rgba(151,187,205,0.5)",
  strokeColor : "rgba(151,187,205,1)",
  pointColor : "rgba(151,187,205,1)",
  pointStrokeColor : "#fff",
  data: []
});


dashboard.factory("KpiModel", function() {
  function KpiModel(name, link) {
    this.name = name;
    this.link = link;
    this.delta = 0;
    this.value = 0;
    this.unitType = "€";
    this.css = "kpi-delta";
    this.icon = "glyphicon glyphicon-minus";
    this.platforms = [];
    this.multiPlatform = true;
  };

  KpiModel.prototype = {
    updateKpiValue: function(data) {
      var value = data.value;
      var delta = data.delta;
      var DecimalPlaces = 2
      this.value = value.toFixed(DecimalPlaces);
      this.delta = delta;
      this.platforms = data.platforms;
      // if(this.value > 0) {
      //   this.css = "kpi-delta-positive";
      //   this.icon = "glyphicon glyphicon-arrow-up";
      // } else if(this.value < 0){
      //   this.css = "kpi-delta-negative";
      //   this.icon = "glyphicon glyphicon-arrow-down";
      // } else {
      //   this.css = "kpi-delta";
      //   this.icon = "glyphicon glyphicon-minus";
      // }
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
    }
  };

  return KpiModel;
});

