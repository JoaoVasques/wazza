'use strict';

angular.module('SecurityModule', [])

.factory('cookiesManagerService', [
  '$cookieStore', '$cookies',
  function ($cookieStore, $cookies) {
  return {
    set: function(cookieName, value) {
      $cookies[cookieName] = value;
      $cookieStore.put(cookieName, value);
    },
    get: function(cookieName){
      return $cookies[cookieName];
    }
  };
}])
;
