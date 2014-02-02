'use strict';

angular.module('DashboardModule', ['ui.bootstrap'])

.controller('DashboardController', [
  '$scope', '$location', '$rootScope','FetchItemsService', 'BootstrapDashboardService', '$modal',
  function ($scope, $location, $rootScope, FetchItemsService, BootstrapDashboardService, $modal) {

  $scope.bootstrapSuccessCallback = function(data){
    angular.extend($scope.credentials, data.data.credentials);
    _.each(data.data.virtualCurrencies, function(vc){
      $scope.virtualCurrencies.push(vc);
    });
    _.each(data.data.items, function(i){
      $scope.items.push(i);
    });
  }

  var ModalInstanceCtrl = function ($scope, $modalInstance, items) {
    $scope.items = items;
    $scope.selected = {
      item: $scope.items[0]
    };

    $scope.ok = function () {
      $modalInstance.close($scope.selected.item);
    };

    $scope.cancel = function () {
      $modalInstance.dismiss('cancel');
    };
  };

  $scope.open = function () {

    var modalInstance = $modal.open({
      templateUrl: 'myModalContent.html',
      controller: ModalInstanceCtrl,
      resolve: {
        items: function () {
          return $scope.items;
        }
      }
    });

    modalInstance.result.then(function (selectedItem) {
      $scope.selected = selectedItem;
    }, function () {});
  };

  //TODO
  $scope.bootstrapFailureCallback = function(errorData){
    console.log(errorData);
  }

  $scope.init = function(){
    BootstrapDashboardService.execute()
    .then(
      $scope.bootstrapSuccessCallback,
      $scope.bootstrapFailureCallback);
  };

  $scope.bootstrapModule = function(){
    $scope.applicationName = "hello world"; //TODO
    $scope.credentials = {};
    $scope.virtualCurrencies = [];
    $scope.items = [];
    $scope.isCollapsed = true;
    $rootScope.$broadcast("UPDATED_APPLICATION_NAME", {value: $scope.applicationName});
    $scope.init();
  };
  $scope.bootstrapModule();

  $scope.addItem = function(){
    $location.path("/item/create");
  };
}])

.factory('FetchItemsService', ['$http','$q', function ($http, $q) {
  return {
    execute: function(appName, offset){
      var request = $http({
        url: '/app/api/item/get/' + appName + '/' + offset,
        method: 'GET'
      });

      var deferred = $q.defer();
      deferred.resolve(request);
      return deferred.promise;
    }
  };
}])

.factory('BootstrapDashboardService', ['$http','$q', function ($http, $q) {
  return {
    execute: function(){
      var request = $http({
        url: '/dashboard/bootstrap',
        method: 'GET'
      });

      var deferred = $q.defer();
      deferred.resolve(request);
      return deferred.promise;
    }
  };
}])

;
