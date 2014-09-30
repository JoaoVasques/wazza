'use strict';

dashboard.controller('DashboardController', [
  '$scope',
  '$location',
  '$rootScope',
  "$anchorScroll",
  "$state",
  'FetchItemsService',
  'BootstrapDashboardService',
  'DeleteItemService',
  'ApplicationStateService',
  'ItemSearchService',
  'TopbarService',
  'GetMainKPIsService',
  "DateModel",
  "KpiModel",
  "AnchorSmoothScroll",
  "$q",
  function (
    $scope,
    $location,
    $rootScope,
    $anchorScroll,
    $state,
    FetchItemsService,
    BootstrapDashboardService,
    DeleteItemService,
    ApplicationStateService,
    ItemSearchService,
    TopbarService,
    GetMainKPIsService,
    DateModel,
    KpiModel,
    AnchorSmoothScroll,
    $q
  ) {

    $rootScope.$on('ChangeDashboardSection', function(event, newSection) {
      var eId = newSection.section;
      $location.hash(eId);
      AnchorSmoothScroll.scrollTo(eId);
    });
    
    /** General KPIs **/
    $scope.totalRevenue = new KpiModel("Total Revenue", "analytics.revenue");
    $scope.arpu = new KpiModel("Avg Revenue Per User", "analytics.arpu");
    $scope.avgRevSession = new KpiModel("Avg Revenue per Session", "analytics.avgRevenueSession");
    
    /** User KPIs **/
    $scope.ltv = new KpiModel("Life Time Value", "analytics.ltv");
    $scope.payingUsers = new KpiModel("% Paying Users", "analytics.payingUsers");
    $scope.churn = new KpiModel("Churn Rate", "analytics.churn");

    /** Session KPIs **/
    $scope.purchasesPerSession = new KpiModel("Purchases per Session", "analytics.purchases");
    $scope.avgTimeFirstPurchase = new KpiModel("Avg Time 1st Purchase", "analytics.avgTime1stPurchase");
    $scope.avgTimeBetweenPurchases = new KpiModel("Avg Time Bet. Purchases", "analytics.avgTimebetweenPurchase");

    $scope.updateKPIs = function(){
      var companyName = ApplicationStateService.companyName;
      var app = ApplicationStateService.applicationName
      var begin = DateModel.formatDate(DateModel.beginDate);
      var end = DateModel.formatDate(DateModel.endDate);
        $q.all([
          GetMainKPIsService.getTotalKpiData(companyName, app, begin, end, "revenue"),
          GetMainKPIsService.getTotalKpiData(companyName, app, begin, end, "ltv"),
          GetMainKPIsService.getTotalKpiData(companyName, app, begin, end, "arpu"),
          GetMainKPIsService.getTotalKpiData(companyName, app, begin, end, "churn")
      ]).then(function(res) {
        var extractValue = function(index, _type) {
          return (_type == 'value') ?  res[index].data.value : res[index].data.delta;
        };
        $scope.totalRevenue.updateKpiValue(extractValue(0, 'value'), extractValue(0, 'delta'))
        $scope.ltv.updateKpiValue(extractValue(1, 'value'), extractValue(1, 'delta'))
        $scope.arpu.updateKpiValue(extractValue(2, 'value'), extractValue(2, 'delta'))
        $scope.churn.updateKpiValue(extractValue(3, 'value'), extractValue(3, 'delta'))
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

        $scope.switchDetailedView = function(state) {
          $state.go(state);
        };

        $scope.bootstrapModule();

    }]
);
