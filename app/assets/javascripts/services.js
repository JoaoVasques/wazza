angular.module('Wazza.services', []).
  factory('submitLoginCredentialsService', ['$http', '$q', function($http, $q) {
    return {
      execute: function(loginData){
        console.log("TODO");
        console.log(loginData);
        var request = $http.post("/login", loginData);
        var deferred = $q.defer();
        deferred.resolve(request);
        return deferred.promise;
      }
    };
  }])
;
