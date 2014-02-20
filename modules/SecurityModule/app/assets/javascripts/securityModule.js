'use strict';

angular.module('SecurityModule', [])

.factory('cookiesManagerService', [
  '$cookieStore', '$cookies',
  function ($cookieStore, $cookies) {
    var service = {};

    service.set = function(cookieName, value) {
      $cookies[cookieName] = value;
      $cookieStore.put(cookieName, value);
    };

    service.get = function(cookieName){
      return $cookies[cookieName];
    };

    return service;
}])
;
