dashboardServices.factory('GetKPIService', ['$http', '$q',
    function($http, $q) {
      var service = {};

      var buildUrl = function(companyName, applicationName, urlType, subType, startDate, endDate) {
        return '/analytics/' +
         urlType + '/' +
         subType + '/' +
         companyName + '/' +
         applicationName + '/'+
         startDate + '/' +
         endDate;
      };

      service.getTotalKpiData = function(companyName, applicationName, start, end, kpiName) {
        var request = $http({
          url: buildUrl(companyName, applicationName, kpiName, "total", start, end),
          method: 'GET'
        });

        var deferred = $q.defer();
        deferred.resolve(request);
        return deferred.promise;
      };

      service.getDetailedKPIData = function(companyName, applicationName, start, end, kpiName) {
        var request = $http({
          url: buildUrl(companyName, applicationName, kpiName, "detail", start, end),
          method: 'GET'
        });

        var deferred = $q.defer();
        deferred.resolve(request);
        return deferred.promise;
      };

      return service;
}])
