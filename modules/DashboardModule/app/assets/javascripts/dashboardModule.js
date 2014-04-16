'use strict';

angular.module('DashboardModule', ['ItemModule.services'])

.controller('DashboardController', [
  '$scope',
  '$location',
  '$rootScope',
  'FetchItemsService',
  'BootstrapDashboardService',
  'DeleteItemService',
  'ApplicationStateService',
  'ItemSearchService',
  function (
    $scope,
    $location,
    $rootScope,
    FetchItemsService,
    BootstrapDashboardService,
    DeleteItemService,
    ApplicationStateService,
    ItemSearchService
    ) {
      
  $scope.bootstrapSuccessCallback = function(data){
    var push = function(origin, destination) {
      _.each(origin, function(el){
        destination.push(el);
      });
    };

    angular.extend($scope.credentials, data.data.credentials);
    push(data.data.virtualCurrencies, $scope.virtualCurrencies);
    push(data.data.items, $scope.items);
    push(
      _.map(data.data.applications, function(element){
        return element.name;
      }),
      $scope.applications
    );
    
    ApplicationStateService.updateCompanyName(data.data.companyName);
    ApplicationStateService.updateApplicationName(_.first(data.data.applications).name);
    ApplicationStateService.updateUserInfo(data.data.userInfo);
  }

  $scope.bootstrapFailureCallback = function(errorData){
    console.log(errorData);
  }

  $scope.bootstrapModule = function(){
    $scope.applicationName = "";
    $scope.applications = [];
    $scope.credentials = {};
    $scope.virtualCurrencies = [];
    $scope.items = [];
    $scope.isCollapsed = true;
    $scope.$on("ITEM_SEARCH_EVENT", function(){
      $scope.itemSearch = ItemSearchService.searchData
    });
    $scope.$on("APPLICATION_NAME_UPDATED", function(){
      $scope.applicationName = ApplicationStateService.applicationName;
    });
    BootstrapDashboardService.execute()
    .then(
      $scope.bootstrapSuccessCallback,
      $scope.bootstrapFailureCallback);
  };
  $scope.bootstrapModule();

  $scope.addItem = function(){
    $location.path("/item/create");
  };

  $scope.itemDeleteSucessCallback = function(data){
    $scope.items = _.without($scope.items, _.findWhere($scope.items, {_id: data.data}));
  }

  /** TODO: show error message **/
  $scope.itemDeleteFailureCallback = function(data){
  }

  $scope.deleteItem = function(id, image){
    DeleteItemService(id, $scope.applicationName, image)
    .then(
      $scope.itemDeleteSucessCallback,
      $scope.itemDeleteFailureCallback
    );
  };

}])

.factory('FetchItemsService', [
  '$http',
  '$q',
  'ApplicationStateService',
  function (
    $http,
    $q,
    ApplicationStateService
  ) {
  var service = {};

  service.execute = function(appName, offset){
    var baseUrl = '/app/api/item/get/';
    var requestUrl = baseUrl +
          ApplicationStateService.companyName + '/' +
          appName + '/' +
          offset;
      
    var request = $http({
      url: requestUrl,
      method: 'GET'
    });

    var deferred = $q.defer();
    deferred.resolve(request);
    return deferred.promise;
  };

  return service;
}])

.factory('BootstrapDashboardService', ['$http','$q', function ($http, $q) {
  var service = {};

  service.execute = function(){
    var request = $http({
      url: '/dashboard/bootstrap',
      method: 'GET'
    });

    var deferred = $q.defer();
    deferred.resolve(request);
    return deferred.promise;
  };

  return service;
}])

.factory('DeleteItemService', [
  '$http',
  '$q',
  'ApplicationStateService',
  function (
    $http,
    $q,
    ApplicationStateService
  ) {
    var service = function(id, name, imageName){
      var baseUrl = '/app/item/delete/';
      var requestUrl = baseUrl +
            ApplicationStateService.companyName +
            '/' + ApplicationStateService.applicationName +
            '/' + id;

      var request = $http.post(requestUrl, {
        appName: name,
        image: imageName
      });

      var deferred = $q.defer();
        deferred.resolve(request);
      return deferred.promise;
    };

    return service;
}])

.factory('ApplicationStateService', ['$rootScope', function ($rootScope) {
  var service = {};
  service.companyName = "";
  service.applicationName = "";
  service.applicationsList = [];
  service.userInfo = {
      name: "",
      email: ""
  };

  service.updateCompanyName = function(newName){
    service.companyName = newName;
    $rootScope.$broadcast("COMPANY_NAME_UPDATED");
  };
    
  service.updateApplicationName = function(newName){
    service.applicationName = newName;
    $rootScope.$broadcast("APPLICATION_NAME_UPDATED");
  };

  service.updateApplicationsList = function(newList){
    service.appplicationsList = newList;
    $rootScope.$broadcast("APPLICATIONS_LIST_UPDATED");
  };

  service.updateUserInfo = function(newInfo) {
    service.userInfo = newInfo;
    $rootScope.$broadcast("USER_INFO_UPDATED");
  };

  return service;
}])
;

