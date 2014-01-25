angular.module('Wazza.services', []).
  factory('submitLoginCredentialsService', ['$http', '$q', function($http, $q) {
    return {
      execute: function(loginData){
        var request = $http.post("/login", loginData);
        var deferred = $q.defer();
        deferred.resolve(request);
        return deferred.promise;
      }
    };
  }]).

  factory('redirectToDashboardService', ['$http', '$q', function($http, $q) {
    return {
      execute: function(){
        var request = $http.get("/dashboard");
        var deferred = $q.defer();
        deferred.resolve(request);
        return deferred.promise;
      }
    };
  }])
;
