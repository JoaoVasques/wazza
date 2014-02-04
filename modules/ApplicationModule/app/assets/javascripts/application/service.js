// application module

angular.module('ApplicationModule.services', [])
  .factory('createNewApplicationService', ['$http', '$q', function($http, $q) {
    var service = {};

    service.validate = function(formData){
      if (formData.name != "" &&
          formData.url != "" &&
          formData.appType != ""
      ) {
        if (formData.appType == "Android" && formData.androidData.packageName != "") {
          return false;
        } else {
          // TODO: iOS stuff
          return true;
        }
      } else {
        return true;
      }
    };

    service.send = function(data){
      var request = $http.post("/app/new", data);
      var deferred = $q.defer();
      deferred.resolve(request);
      return deferred.promise;
    };

    return service;
  }])
;
