// item module

angular.module('ItemModule.services', []).
  factory('createNewItemService', ['$upload', '$q', function ($upload, $q) {
    var service = {};

    service.send = function(formData, file){
      var request = $upload.upload({
          url: '/app/item/new/' + formData.applicationName,
          method: 'POST',
          data: formData,
          file: file
      });

      var deferred = $q.defer();
      deferred.resolve(request);
      return deferred.promise;
    };

    return service;
  }]).

  factory('uploadPhotoService', ['$upload', '$q', function ($upload, $q) {
    var service = {};

    service.execute = function(file){
      var request = $upload.upload({
        url: '/app/item/uploadimage ',
        method: 'POST',
        file: file
      });

      var deferred = $q.defer();
      deferred.resolve(request);
      return deferred.promise;
    };

    return service;
  }]).

  factory('getVirtualCurrenciesService', ['$http','$q', function ($http, $q) {
    var service = {};

    service.execute = function(applicationName){
      var request = $http({
        url: '/app/api/virtualcurrencies/all/' + applicationName,
        method: 'GET'
      });

      var deferred = $q.defer();
      deferred.resolve(request);
      return deferred.promise;
    };

    return service;
  }]).

  factory('ItemSearchService', ['$rootScope', function ($rootScope) {
    var service = {};  
    service.searchData = "";

    service.updateSearchData = function(newData){
      service.searchData = newData;
      $rootScope.$broadcast("ITEM_SEARCH_EVENT");
    };

    return service;
  }])
;
