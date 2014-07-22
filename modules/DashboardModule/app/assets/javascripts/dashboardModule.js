'use strict';

var dashboard = angular.module('DashboardModule', ['ItemModule.services'])

.controller('DashboardController', [
  '$scope',
  '$location',
  '$rootScope',
  'FetchItemsService',
  'BootstrapDashboardService',
  'DeleteItemService',
  'ApplicationStateService',
  'ItemSearchService',
  'TopbarService',
  'GetMainKPIsService',
  function (
        $scope,
        $location,
        $rootScope,
        FetchItemsService,
        BootstrapDashboardService,
        DeleteItemService,
        ApplicationStateService,
        ItemSearchService,
        TopbarService,
        GetMainKPIsService
    ) {

        $scope.bootstrapSuccessCallback = function (data) {
            var push = function (origin, destination) {
                _.each(origin, function (el) {
                    destination.push(el);
                });
            };

            angular.extend($scope.credentials, data.data.credentials);
            push(data.data.virtualCurrencies, $scope.virtualCurrencies);
            push(data.data.items, $scope.items);
            push(
                _.map(data.data.applications, function (element) {
                    return element.name;
                }),
                $scope.applications
            );
            ApplicationStateService.updateApplicationName(_.first(data.data.applications).name);
            ApplicationStateService.updateUserInfo(data.data.userInfo);

            ApplicationStateService.updateApplicationsList(
              _.map(data.data.applications, function(app) {
                  return app.name;
              })
            );
            
            TopbarService.setName("Dashboard");

            $scope.options = {
              axes: {
                x: {key: 'x', labelFunction: function(value) {return value;}, type: 'linear'},
                y: {type: 'linear', min: 0, max: 100},
                y2: {type: 'linear', min: 0, max: 100}
              },
              series: [
                {y: 'value', color: 'steelblue', thickness: '2px', type: 'area', striped: true, label: 'ARPU'},
                {y: 'otherValue', axis: 'y2', color: 'lightsteelblue', visible: false, drawDots: true}
              ],
              lineMode: 'linear',
              tension: 0.7,
              tooltip: {mode: 'scrubber', formatter: function(x, y, series) {return 'pouet';}},
              drawLegend: true,
              drawDots: true,
              columnsHGap: 5
            }

            $scope.arpu = [
              {x: 0, value: 4, otherValue: 14},
              {x: 1, value: 8, otherValue: 1},
              {x: 2, value: 15, otherValue: 11},
              {x: 3, value: 16, otherValue: 147},
              {x: 4, value: 23, otherValue: 87},
              {x: 5, value: 42, otherValue: 45}
            ];

        }

        $scope.bootstrapFailureCallback = function (errorData) {
            console.log(errorData);
        }

        $scope.bootstrapModule = function () {
            $scope.applicationName = "";
            $scope.applications = [];
            $scope.credentials = {};
            $scope.virtualCurrencies = [];
            $scope.items = [];
            $scope.isCollapsed = true;
            $scope.$on("ITEM_SEARCH_EVENT", function () {
                $scope.itemSearch = ItemSearchService.searchData
            });
            $scope.$on("APPLICATION_NAME_UPDATED", function () {
                $scope.applicationName = ApplicationStateService.applicationName;
            });

            $scope.$on("APPLICATIONS_LIST_UPDATED", function() {
                $scope.applications = ApplicationStateService.applicationsList;
            });
            
            BootstrapDashboardService.execute()
                .then(
                    $scope.bootstrapSuccessCallback,
                    $scope.bootstrapFailureCallback);
            GetMainKPIsService.execute("CompanyTest", "RecTestApp", "2014-07-22", "2014-07-22")
                .then(function(results) {
                    console.log(results);
                });
        };
        $scope.bootstrapModule();

    }])

.factory('BootstrapDashboardService', ['$http', '$q',
    function ($http, $q) {
        var service = {};

        service.execute = function () {
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

.factory('GetMainKPIsService', ['$http', '$q',
    function($http, $q) {
      var service = {};

      service.execute = function(companyName, applicationName, startDate, endDate) {
        var revUrl = '/analytics/revenue/total/' + companyName + '/' + applicationName + '/'+ startDate +'/' + endDate;
        var totalRevenue = $http({
            url: revUrl,
            method: 'GET'
        });

        var arpuUrl = '/analytics/arpu/total/' + companyName + '/' + applicationName + '/'+ startDate +'/' + endDate;
        var totalARPU = $http({
            url: arpuUrl,
            method: 'GET'
        });

        return $q.all([totalRevenue, totalARPU]);
      };

      return service;
}])

.factory('ApplicationStateService', ['$rootScope',
    function ($rootScope) {
        var service = {};
        service.applicationName = "";
        service.companyName = "";
        service.applicationsList = [];
        service.userInfo = {
            name: "",
            email: ""
        };

        service.updateApplicationName = function (newName) {
            service.applicationName = newName;
            $rootScope.$broadcast("APPLICATION_NAME_UPDATED");
        };

        service.updateCompanyName = function(newName) {
            service.companyName = newName;
            $rootScope.$broadcast("COMPANY_NAME_UPDATED");
        };
        
        service.updateApplicationsList = function (newList) {
            service.applicationsList = newList.slice(0);
            $rootScope.$broadcast("APPLICATIONS_LIST_UPDATED");
        };

        service.updateUserInfo = function (newInfo) {
            service.userInfo = newInfo;
            $rootScope.$broadcast("USER_INFO_UPDATED");
        };

        return service;
}])

.factory('FetchItemsService', ['$http', '$q',
    function ($http, $q) {
        var service = {};

        service.execute = function (appName, offset) {
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

.factory('DeleteItemService', ['$http', '$q',
    function ($http, $q) {
        var service = function (id, name, imageName) {
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

;
