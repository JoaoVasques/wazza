dashboardServices.factory('GetKPIService', ['$http', '$q',
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
