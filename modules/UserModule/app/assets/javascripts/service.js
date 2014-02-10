'use strict';

angular.module('UserModule.services', [])

.factory('createNewUserAccountService', ['$http', '$q', function($http, $q) {  
  return {
    execute: function(formData){
      var request = $http.post("/user/register", formData);
      var deferred = $q.defer();
      deferred.resolve(request);
      return deferred.promise;
    }
  };
}])
;
