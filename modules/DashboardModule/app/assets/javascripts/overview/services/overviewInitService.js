'use strict';

overviewServices.factory('overviewInitService', ['$http', '$q',
  function($http, $q) {
    function overviewInitService() {

    };

    overviewInitService.prototype = {
      getApplications: function() {
        var deferred = $.defer();
        return deferred.resolve($http({
          url: '/dashboard/overview',
          method: 'GET'
        })).promise;
      }
    };

    return overviewInitService;
}]);
