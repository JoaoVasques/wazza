'use strict';

dashboard.factory('ArpuModel', ['KpiModel', function(KpiModel) {
  function ArpuModel(beginDate, endDate) {
    this.beginDate = beginDate;
    this.endDate = endDate;
    this.labels = [];
    this.values = [];
    this.model = new KpiModel("Average Revenue Per User", "");
  };

  ArpuModel.prototype = {
    updateDates: function(begin, end) {
      this.beginDate = begin;
      this.endDate = end;
    },
    updateChartData: function(newLabels, newValues) {
      this.labels = newLabels;
      this.values = newValues;
    }
  };
    
  return ArpuModel;
}]);
