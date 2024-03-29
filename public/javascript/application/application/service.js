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
      var companyName = ApplicationStateService.getCompanyName();
      var request = $http.post("/app/new/" + companyName, data);
      var deferred = $q.defer();
      deferred.resolve(request);
      return deferred.promise;
    };

    return service;
  }])

  .factory('uploadAppImageService', ['$upload', '$q', function ($upload, $q) {
    var service = {};

    service.execute = function(file){
      var request = $upload.upload({
        url: '/app/new/uploadimage',
        method: 'POST',
        file: file
      });

      var deferred = $q.defer();
      deferred.resolve(request);
      return deferred.promise;
    };

    return service;
  }])
;
