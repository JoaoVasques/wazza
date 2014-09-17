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
    updateChartData: function(chartData) {
      var _this = this;
      this.labels = [];
      this.values = [];
      _.each(chartData.data, function(el) {
        _this.labels.push(el.day);
        _this.values.push(el.val);
      });
    }
  };
    
  return ArpuModel;
}]);
