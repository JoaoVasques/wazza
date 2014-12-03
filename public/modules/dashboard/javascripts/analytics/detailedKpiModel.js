'use strict';

dashboard.factory('DetailedKpiModel', ['KpiModel', function(KpiModel) {
  function DetailedKpiModel(beginDate, endDate, name) {
    this.beginDate = beginDate;
    this.endDate = endDate;
    this.labels = [];
    this.values = [];
    this.model = new KpiModel(name, "");
  };

  DetailedKpiModel.prototype = {
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
        _this.values.push(el.value);
      });
    }
  };
    
  return DetailedKpiModel;
}]);
