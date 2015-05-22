settingsServices.factory('BootstrapSettingsService', [
  '$http',
  '$q',
  'ApplicationStateService',
  function (
    $http,
    $q,
    ApplicationStateService
  ) {
    var service = {};
    service.execute = function () {
      var request = $http({
        url: '/dashboard/settings/bootstrap/' + ApplicationStateService.getApplicationName(),
        method: 'GET'
      });

      var deferred = $q.defer();
      deferred.resolve(request);
      return deferred.promise;
    };

    return service;
  }
]);

