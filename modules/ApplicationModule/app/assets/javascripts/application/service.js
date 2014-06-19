// application module

angular.module('ApplicationModule.services', ['DashboardModule'])
  .factory('createNewApplicationService', [
    '$http',
    '$q',
    'ApplicationStateService',
    function(
      $http,
      $q,
      ApplicationStateService
    ) {
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
      console.log("creating new app");
      var companyName = ApplicationStateService.companyName;
      console.log("companyName: " + companyName);
      var request = $http.post("/app/new/" + companyName, data);
      var deferred = $q.defer();
      deferred.resolve(request);
      return deferred.promise;
    };

    return service;
  }])
;
