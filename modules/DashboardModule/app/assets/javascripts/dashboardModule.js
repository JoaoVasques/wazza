'use strict';

var dashboard = angular.module('DashboardModule', ['ItemModule.services', 'DashboardModule.services']);

dashboard.value('KpiData', [
  {
    name: "Total Revenue",
    link: "revenue link",
    unitType: "€"
  },
  {
    name: "Average Revenue Per User",
    link: "arpu link",
    unitType: "€"
  }
]);

dashboard.controller('DashboardController', [
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
  'KpiData',
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
        GetMainKPIsService,
        KpiData
    ) {

        $scope.logout = function(){
          LoginLogoutService.logout();
        };

        $scope.format = 'dd-MMMM-yyyy';

        $scope.today = function() {
          $scope.beginDate = new Date();
          $scope.endDate = new Date();
        };
        $scope.today();

        $scope.initDateInterval = function(){
          $scope.beginDate = new Date(moment().subtract('days', 7));
          $scope.endDate = new Date;
        };
        $scope.initDateInterval();

        // Disable weekend selection
        $scope.disabled = function(date, mode) {
          return ( mode === 'day' && ( date.getDay() === 0 || date.getDay() === 6 ) );
        };

        $scope.toggleMin = function() {
          $scope.minDate = moment().subtract('years', 1).format('d-M-YYYY');
          $scope.endDateMin = $scope.beginDate;
        };
        $scope.toggleMin();

        $scope.updateEndDateMin = function(){
          $scope.endDateMin = $scope.beginDate;
        };

        $scope.maxDate = new Date();

        $scope.openBeginDate = function($event) {
          $event.preventDefault();
          $event.stopPropagation();

          $scope.beginDateOpened = true;
        };

        $scope.openEndDate = function($event) {
          $event.preventDefault();
          $event.stopPropagation();

          $scope.endDateOpened = true;
        };

        $scope.initDate = $scope.today;

        var KpiContext = function(name, value, unit, link){
          this.name = name;
          this.value = value;
          this.unit = unit;
          this.link = link;
        };

        $scope.kpis = [];

        $scope.updateKPIs = function(){
          $scope.kpis = [];
          GetMainKPIsService.execute(
            ApplicationStateService.companyName,
            ApplicationStateService.applicationName,
            moment($scope.beginDate).format('DD-MM-YYYY'),
            moment($scope.endDate).format('DD-MM-YYYY')
            )
            .then(function(results) {
                _.each(results, function(value, i) {
                  $scope.kpis.push(new KpiContext(KpiData[i].name, value.data.value, KpiData[i].unitType, KpiData[i].link))
              });
            });
        };

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

            ApplicationStateService.updateCompanyName(data.data.companyName);
            TopbarService.setName("Dashboard");

            $scope.updateKPIs();

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
        };

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
        };
        $scope.bootstrapModule();

        $scope.switchDetailedView = function(url) {
          //TODO
          console.log(url);
        };
    }]
);

dashboard.factory('BootstrapDashboardService', ['$http', '$q',
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
}]);

dashboard.factory('GetMainKPIsService', ['$http', '$q',
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
}]);

dashboard.factory('ApplicationStateService', ['$rootScope',
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
}]);

dashboard.factory('FetchItemsService', ['$http', '$q',
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
}]);

dashboard.factory('DeleteItemService', ['$http', '$q',
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
