dashboardServices.factory('GetKPIService', ['$http', '$q',
    function($http, $q) {
      var buildUrl = function(companyName, applicationName, urlType, subType, startDate, endDate) {
        var url = ('/analytics/' +
         urlType + '/' +
         subType + '/' +
         companyName + '/' +
         applicationName + '/'+
         startDate + '/' +
         endDate).replace(" ", "%20");
        return url;
      };

      this.getTotalKpiData = function(companyName, applicationName, start, end, kpiName, platforms) {
        var request = $http({
          url: buildUrl(companyName, applicationName, kpiName, "total", start, end),
          method: 'GET',
          headers: {
            "X-Platforms": platforms
          }
        });

        var deferred = $q.defer();
        deferred.resolve(request);
        return deferred.promise;
      };

      this.getDetailedKPIData = function(companyName, applicationName, start, end, kpiName, platforms, paymentSystems) {
        var request = $http({
          url: buildUrl(companyName, applicationName, kpiName, "detail", start, end),
          method: 'GET',
          headers: {
            "X-Platforms": platforms,
            "X-PaymentSystems": paymentSystems
          }
        });

        var deferred = $q.defer();
        deferred.resolve(request);
        return deferred.promise;
      };

      return this;
}])
