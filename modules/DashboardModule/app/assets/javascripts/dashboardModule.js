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
    ApplicationStateService.updateApplicationName(_.first(data.data.applications).name);
    ApplicationStateService.updateUserInfo(data.data.userInfo);
  }

  $scope.bootstrapFailureCallback = function(errorData){
    console.log(errorData);
  }
/*
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
*/
  $scope.open = function () {
/*
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
  */
  };

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

.factory('FetchItemsService', ['$http','$q', function ($http, $q) {
  var service = {};

  service.execute = function(appName, offset){
    var request = $http({
      url: '/app/api/item/get/' + appName + '/' + offset,
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

.factory('DeleteItemService', ['$http','$q', function ($http, $q) {
  var service = function(id, name, imageName){
    var request = $http.post("/app/item/delete/" + id, {
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
  service.applicationName = "";
  service.applicationsList = [];
  service.userInfo = {
      name: "",
      email: ""
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


.controller('AnalyticsController', [
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


var dummyData = {
  "Users": 
    {"totalMoneySpent": {
      "UserLevel0": 0,
      "UserLevel1": 0,
      "UserLevel2": 20,
      "UserLevel3": 10,
      "UserLevel4": 10,
      "UserLevel5": 40,
      "UserLevel6": 67,
      "UserLevel7": 80,
      "UserLevel8": 80,
      "UserLevel9": 90
    },
    "deviceInfo": {
        "pieChart" :{
            "appVersion":{
                "2": 10000,
                "2.1": 45000,
                "3": 4000000
            },
            "OS":{
                "Android 2.3": 10000,
                "Android 4.0": 20000,
                "iOS 6": 4025000
            },
            "Screen Resolution": {
                "240p": 5000,
                "480p": 40000,
                "720p": 2000000,
                "1080p": 2000000
            }
        },
        "map" : {
            "lat": "38",
            "long": "34"
        }
    },
    "moneySpentLines" : {
        
    }
    
    },
  "boolea": true,
  "null": null,
  "number": 123,
  "object": {
    "a": "b",
    "c": "d",
    "e": "f"
  },
  "string": "Hello World"
}


var rows = [];

for (var key in dummyData.Users.totalMoneySpent) {
  if (dummyData.Users.totalMoneySpent.hasOwnProperty(key)) {
    rows.push(dummyData.Users.totalMoneySpent[key]);
  }
}


$(function () { 
          $('#totalMoneySpent').highcharts({
              chart: {
                  type: 'column'
              },
              title: {
                  text: 'Total Money Spent'
              },
              xAxis: {
                type: 'category',
                labels: {
                    rotation: -45,
                    align: 'right',
                    style: {
                        fontSize: '13px',
                        fontFamily: 'Verdana, sans-serif'
                    }
                }
            },
            legend: {
                enabled: false
            },
            tooltip: {
                pointFormat: "Total Money Spent in Q1 2014: <b>{point.y:.1f} millions</b>"
            },
              yAxis: {
                  title: {
                      text: 'in Million USD'
                  }
              },
              series: [{
                name: 'Population',
                data: rows,
                dataLabels: {
                    enabled: true,
                    rotation: -90,
                    color: '#FFFFFF',
                    align: 'right',
                    x: 4,
                    y: 10,
                    style: {
                        fontSize: '13px',
                        fontFamily: 'Verdana, sans-serif',
                        textShadow: '0 0 3px black'
                    }
                }
            }]
          });
      });
        
  }])
;

