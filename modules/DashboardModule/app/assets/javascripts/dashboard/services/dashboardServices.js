var dashboardServices = angular.module('DashboardModule.services', [])

.factory('BootstrapDashboardService', ['$http', '$q',
    function ($http, $q) {
        var service = {};

        service.execute = function () {
            var request = $http({
                url: '/dashboard/bootstrap',
                method: 'GET'
            });

            var deferred = $q.defer();
            deferred.resolve(request);
            return deferred.promise;
        };

        return service;
}])

.factory('GetKPIService', ['$http', '$q',
    function($http, $q) {
      var service = {};

      service.execute = function(companyName, applicationName, startDate, endDate, metric) {
        var buildUrl = function(urlType, subType) {
          return '/analytics/' + urlType + '/' + subType +'/' + companyName + '/' + applicationName + '/'+ startDate +'/' + endDate;
        };

        var getTotal = $http({
            url: buildUrl(metric, 'total'),
            method: 'GET'
        });

        var getDetailed = $http({
            url: buildUrl(metric, 'detail'),
            method: 'GET'
        });

        return $q.all([getTotal, getDetailed]);
      };

      return service;
}])

.factory('GetMainKPIsService', ['$http','$q',
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


//item related. leftover.
.factory('FetchItemsService', ['$http', '$q',
    function ($http, $q) {
        var service = {};

        service.execute = function (appName, offset) {
            var request = $http({
                url: '/app/api/item/get/' + appName + '/' + offset,
                method: 'GET'
            });

            var deferred = $q.defer();
            deferred.resolve(request);
            return deferred.promise;
        };

        return service;
}])

.factory('DeleteItemService', ['$http', '$q',
    function ($http, $q) {
        var service = function (id, name, imageName) {
            var request = $http.post("/app/item/delete/" + id, {
                appName: name,
                image: imageName
            });

            var deferred = $q.defer();
            deferred.resolve(request);
            return deferred.promise;
        };

        return service;
}]);
