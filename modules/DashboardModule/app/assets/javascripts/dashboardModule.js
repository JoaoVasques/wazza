'use strict';

angular.module('DashboardModule', ['ui.bootstrap'])

.controller('DashboardController', [
  '$scope', '$location', '$rootScope','FetchItemsService', 'BootstrapDashboardService', '$modal', 'DeleteItemService',
  function ($scope, $location, $rootScope, FetchItemsService, BootstrapDashboardService, $modal, DeleteItemService) {

  $scope.bootstrapSuccessCallback = function(data){
    angular.extend($scope.credentials, data.data.credentials);
    _.each(data.data.virtualCurrencies, function(vc){
      $scope.virtualCurrencies.push(vc);
    });
    _.each(data.data.items, function(i){
      $scope.items.push(i);
    });
  }

  $scope.bootstrapFailureCallback = function(errorData){
    console.log(errorData);
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

  $scope.bootstrapModule = function(){
    $scope.applicationName = "hello world"; //TODO
    $scope.credentials = {};
    $scope.virtualCurrencies = [];
    $scope.items = [];
    $scope.isCollapsed = true;
    $rootScope.$broadcast("UPDATED_APPLICATION_NAME", {value: $scope.applicationName});
    $scope.$on("ITEM_SEARCH_EVENT", function(event, data){
      $scope.itemSearch = data.name;
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
      function(data){$scope.itemDeleteSucessCallback(data)},
      function(data){$scope.itemDeleteFailureCallback(data)}
    );
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

.factory('DeleteItemService', ['$http','$q', function ($http, $q) {
  return function(id, name, imageName){
    var request = $http.post("/app/item/delete/" + id, {
      appName: name,
      image: imageName
    });

    var deferred = $q.defer();
    deferred.resolve(request);
    return deferred.promise;
  };
}])
;
