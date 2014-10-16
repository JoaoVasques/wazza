dashboardServices.factory('GetMainKPIsService', ['$http','$q',
  function($http,$q) {
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

      service.getOverviewKpis = function(companyName, applicationName, startDate, endDate) {

        var revUrl = buildUrl(companyName, applicationName, 'revenue', 'total', startDate, endDate);
        var totalRevenue = $http({
            url: revUrl,
            method: 'GET'
        });

        var arpuUrl = buildUrl(companyName, applicationName, 'arpu', 'total', startDate, endDate);
        var totalARPU = $http({
            url: arpuUrl,
            method: 'GET'
        });

        return $q.all([totalRevenue, totalARPU]);
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

