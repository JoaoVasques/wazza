'use strict';

var dashboard = angular.module('DashboardModule', [
    'ItemModule.services',
    'DashboardModule.services',
    'DashboardModule.controllers',
    'chartjs-directive'
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

dashboard.factory("KpiModel", function() {
  function KpiModel(name, link) {
    this.name = name;
    this.link = link;
    this.delta = 0;
    this.value = 0;
    this.unitType = "€";
    this.css = "kpi-delta";
    this.icon = "glyphicon glyphicon-minus";
  };

  KpiModel.prototype = {
    updateKpiValue: function(value, delta) {
      this.value = value;
      this.delta = delta;
      if(this.value > 0) {
        this.css = "kpi-delta-positive";
        this.icon = "glyphicon glyphicon-arrow-up";
      } else if(this.value < 0){
        this.css = "kpi-delta-negative";
        this.icon = "glyphicon glyphicon-arrow-down";
      } else {
        this.css = "kpi-delta";
        this.icon = "glyphicon glyphicon-minus";
      }
    },
    updateUnitType: function(newType) {
      this.unitType = newType;
    }
  };

  return KpiModel;
});

dashboard.controller('DashboardController', [
  '$scope',
  '$location',
  '$rootScope',
  "$anchorScroll",
  'FetchItemsService',
  'BootstrapDashboardService',
  'DeleteItemService',
  'ApplicationStateService',
  'ItemSearchService',
  'TopbarService',
  'GetMainKPIsService',
  'KpiData',
  "DashboardModel",
  "KpiModel",
  "AnchorSmoothScroll",
  function (
    $scope,
    $location,
    $rootScope,
    $anchorScroll,
    FetchItemsService,
    BootstrapDashboardService,
    DeleteItemService,
    ApplicationStateService,
    ItemSearchService,
    TopbarService,
    GetMainKPIsService,
    KpiData,
    DashboardModel,
    KpiModel,
    AnchorSmoothScroll
  ) {
    $scope.logout = function(){
      LoginLogoutService.logout();
    };

    $rootScope.$on('ChangeDashboardSection', function(event, newSection) {
      var eId = newSection.section;
      $location.hash(eId);
      AnchorSmoothScroll.scrollTo(eId);
    });
    
    /** General KPIs **/
    $scope.totalRevenue = new KpiModel("Total Revenue", "/revenue");
    $scope.arpu = new KpiModel("Avg Revenue Per User", "/arpu");
    $scope.avgRevSession = new KpiModel("Avg Revenue per Session", "#");
    
    /** User KPIs **/
    $scope.ltv = new KpiModel("Life Time Value", "#");
    $scope.payingUsers = new KpiModel("% Paying Users", "#");
    $scope.todo = new KpiModel("Paying Users Growth", "#");

    /** Session KPIs **/
    $scope.purchasesPerSession = new KpiModel("Purchases per Session", "#");
    $scope.avgTimeFirstPurchase = new KpiModel("Avg Time 1st Purchase", "#");
    $scope.avgTimeBetweenPurchases = new KpiModel("Avg Time Bet. Purchases", "#");


      $scope.format = 'dd-MMMM-yyyy';

        $scope.today = function() {
          DashboardModel.initDateInterval();
          $scope.beginDate = DashboardModel.startDate;
          $scope.endDate = DashboardModel.endDate;
        };
        $scope.today();

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

        $scope.updateKPIs = function(){
          GetMainKPIsService.execute(
            ApplicationStateService.companyName,
            ApplicationStateService.applicationName,
            DashboardModel.formatDate($scope.beginDate),
            DashboardModel.formatDate($scope.endDate)
            )
            .then(function(results) {
              /** Total Revenue **/
              $scope.totalRevenue.updateKpiValue(results[0].data.value, results[0].data.delta);
              /** ARPU **/
              $scope.arpu.updateKpiValue(results[1].data.value, results[1].data.delta);
            });
        };

        var bootstrapSuccessCallback = function (data) {
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
        };

        var bootstrapFailureCallback = function (errorData) {
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
                    bootstrapSuccessCallback,
                    bootstrapFailureCallback);
        };

        $scope.switchDetailedView = function(url) {
          $location.path(url);
        };

        $scope.bootstrapModule();

    }]
);
