'use strict';

angular.module('DashboardModule.services', [])

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


.factory('GetKPIService', ['$http', '$q',
    function($http, $q) {
      var service = {};

      service.execute = function(companyName, applicationName, startDate, endDate, metric) {
        var buildUrl = function(urlType, subType) {
          return '/analytics/' + urlType + '/' + subType +'/' + companyName + '/' + applicationName + '/'+ startDate +'/' + endDate;
        };

        var getTotal = $http({
            url: buildUrl(metric, 'total'),
            method: 'GET'
        });

		var getDetailed = $http({
            url: buildUrl(metric, 'detail'),
            method: 'GET'
        });

        return $q.all([getTotal, getDetailed]);
      };

      return service;
}])


//TODO: refactor this to use GetKPIService instead
.factory('GetMainKPIsService', ['$http', '$q',
    function($http, $q) {
      var service = {};

      service.execute = function(companyName, applicationName, startDate, endDate) {
        var buildUrl = function(urlType, subType) {
          return '/analytics/' + urlType + '/' + subType +'/' + companyName + '/' + applicationName + '/'+ startDate +'/' + endDate;
        };

        var revUrl = buildUrl('revenue', 'total');
        var totalRevenue = $http({
            url: revUrl,
            method: 'GET'
        });

        var arpuUrl = buildUrl('arpu', 'total');
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
}]);
