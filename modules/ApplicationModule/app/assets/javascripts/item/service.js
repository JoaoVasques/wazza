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
  }]).

  factory('GetLanguagesService', [function () {
    var service = {};
    service.languages = {
      "Portuguese": "pt_PT",
      "Chinese":    "zh_TW",
      "Italian":    "it_IT",
      "Czech":      "cs_CZ",
      "Japanese":   "ja_JP",
      "Danish":     "da_DK",
      "Korean":     "ko_KR",
      "Dutch":      "nl_NL",
      "Norwegian":  "no_NO",
      "English":    "en_US",
      "Polish":     "pl_PL",
      "French":     "fr_FR",
      "Finnish":    "fi_FI",
      "Russian":    "ru_RU",
      "German":     "de_DE",
      "Spanish":    "es_ES",
      "Hebrew":     "iw_IL",
      "Swedish":    "sv_SE",
      "Hindi":      "hi_IN"
    };

    service.languageOptions = function(){
      return _.map(service.languages, function(value, key){
        return key;
      });
    };

    return service;
  }])
;
