'use strict';

var dashboard = angular.module('DashboardModule', [
    'ItemModule.services',
    'DashboardModule.services',
    'DashboardModule.controllers',
    'chartjs'
]);

dashboard.value('KpiData', [
  {
    name: "Total Revenue",
    link: "/revenue",
    delta: 0,
    unitType: "€"
  },
  {
    name: "Average Revenue Per User",
    link: "/arpu",
    delta: 0,
    unitType: "€"
  }
  //TODO: all other metrics
]);

dashboard.value("KpiDelta",{css: "kpi-delta", icon: "glyphicon glyphicon-minus"});
dashboard.value("KpiPositiveDelta", {css: "kpi-delta-positive", icon: "glyphicon glyphicon-arrow-up"});
dashboard.value("KpiNegativeDelta", {css: "kpi-delta-negative", icon: "glyphicon glyphicon-arrow-down"});

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
  "KpiDelta",
  "KpiPositiveDelta",
  "KpiNegativeDelta",
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
    KpiData,
    KpiDelta,
    KpiPositiveDelta,
    KpiNegativeDelta
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

        var KpiContext = function(name, value, delta, unit, link, css, icon) {
          this.name = name;
          this.value = value;
          this.delta = delta;
          this.unit = unit;
          this.link = link;
          this.css = css;
          this.icon = icon;
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
                  var delta = value.data.delta
                  var css = (value.data.delta > 0) ? KpiPositiveDelta.css :
                        ((value.data.delta < 0) ? KpiNegativeDelta.css : KpiDelta.css);

                  var icon = (value.data.delta > 0) ? KpiPositiveDelta.icon :
                        ((value.data.delta < 0) ? KpiNegativeDelta.icon : KpiDelta.icon);

                  $scope.kpis.push(
                      new KpiContext(
                          KpiData[i].name,
                          value.data.value,
                          delta,
                          KpiData[i].unitType,
                          KpiData[i].link,
                          css,
                          icon
                      )
                  )
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
        };

        $scope.bootstrapModule();

    }]
);
