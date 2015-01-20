'use strict';

dashboardServices.factory('DashboardCache', [
  'localStorageService',
  'ApplicationStateService',
  function(
    localStorageService,
    ApplicationStateService
  ) {

    function ChartCacheData(name, dates, data) {
      this.name = name;
      this.dates = dates;
      this.data = data;
    };

    var saveChartData = function(key, data) {
      // get service to fetch current dates
      var values = [];
      return localStorageService.set(key, new ChartCacheData(0, data));
    };
      
    var getChartData = function(name){
      var value = localStorageService.get(name);
      return (value === undefined) ? null : value.data;
    };

    var getTimeByKey = function(key) {
      var value = localStorageService.get(key);
      return (value === undefined) ? null : value.dates;
    };

    var deleteChartData = function(name) {
      return localStorageService.remove(name);
    };

    return {
      save: saveChartData,
      getChartData: getChartData,
      getDates: getTimeByKey,
      remove: deleteChartData
    };
}]);

