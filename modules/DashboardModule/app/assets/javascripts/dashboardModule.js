'use strict';

var dashboard = angular.module('DashboardModule', [
    'ItemModule.services',
    'DashboardModule.services',
    'DashboardModule.controllers',
    'chartjs-directive'
]);

dashboard.value('KpiData', [
  {
    name: "Total Revenue",
    link: "/revenue",
    delta: 0,
    unitType: "€"
  },
  {
    name: "Average Revenue Per User",
    link: "/arpu",
    delta: 0,
    unitType: "€"
  }

  //TODO: all other metrics

]);

dashboard.factory("KpiModel", function() {
  function KpiModel(name, link) {
    this.name = name;
    this.link = link;
    this.delta = 0;
    this.value = 0;
    this.unitType = "€";
    this.css = "kpi-delta";
    this.icon = "glyphicon glyphicon-minus";
  };

  KpiModel.prototype = {
    updateKpiValue: function(value, delta) {
      this.value = value;
      this.delta = delta;
      if(this.value > 0) {
        this.css = "kpi-delta-positive";
        this.icon = "glyphicon glyphicon-arrow-up";
      } else if(this.value < 0){
        this.css = "kpi-delta-negative";
        this.icon = "glyphicon glyphicon-arrow-down";
      } else {
        this.css = "kpi-delta";
        this.icon = "glyphicon glyphicon-minus";
      }
    },
    updateUnitType: function(newType) {
      this.unitType = newType;
    }
  };

  return KpiModel;
});

