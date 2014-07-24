'use strict';

var dashboard = angular.module('DashboardModule', ['ItemModule.services', 'DashboardModule.services']);

dashboard.value('KpiData', [
  {
    name: "Total Revenue",
    link: "/revenue",
    unitType: "€"
  },
  {
    name: "Average Revenue Per User",
    link: "/arpu",
    unitType: "€"
  }
  //TODO: all other metrics
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

        $scope.switchDetailedView = function(url) {
          $location.path(url);
          console.log(url);
        };

        $scope.bootstrapModule();

    }]
);
