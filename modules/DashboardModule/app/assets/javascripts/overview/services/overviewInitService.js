'use strict';

overviewServices.factory('OverviewInitService', ['$http', '$q',
  function($http, $q) {

    var service = {};
    service.getApplications = function() {
      var deferred = $q.defer();
      deferred.resolve($http({
        url: '/dashboard/overview/bootstrap',
        method: 'GET'
      }));
      return deferred.promise;
    };

    return service;
}]);
