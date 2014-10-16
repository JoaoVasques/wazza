dashboard.controller('DashboardController', [
    '$scope',
    '$location',
    '$rootScope',
    "$anchorScroll",
    "$state",
    "$document",
    'FetchItemsService',
    'BootstrapDashboardService',
    'DeleteItemService',
    'ApplicationStateService',
    'ItemSearchService',
    'GetMainKPIsService',
    "DateModel",
    "KpiModel",
    "$q",
    function (
        $scope,
        $location,
        $rootScope,
        $anchorScroll,
        $state,
        $document,
        FetchItemsService,
        BootstrapDashboardService,
        DeleteItemService,
        ApplicationStateService,
        ItemSearchService,
        GetMainKPIsService,
        DateModel,
        KpiModel,
        $q
        ) {

        /** General KPIs **/
        $scope.totalRevenue = new KpiModel("Total Revenue", "analytics.revenue");
        $scope.arpu = new KpiModel("Avg Revenue Per User", "analytics.arpu");
        $scope.avgRevSession = new KpiModel("Avg Revenue per Session", "analytics.avgRevenueSession");

        /** User KPIs **/
        $scope.ltv = new KpiModel("Life Time Value", "analytics.ltv");
        $scope.payingUsers = new KpiModel("Paying Users", "analytics.payingUsers");
        $scope.churn = new KpiModel("Churn Rate", "analytics.churn");

        /** Session KPIs **/
        $scope.purchasesPerSession = new KpiModel("Purchases per Session", "analytics.purchasesPerSession");
        $scope.avgTimeFirstPurchase = new KpiModel("Avg Time 1st Purchase", "analytics.avgTime1stPurchase");
        $scope.avgTimeBetweenPurchases = new KpiModel("Avg Time Bet. Purchases", "analytics.avgTimebetweenPurchase");

        $scope.updateKPIs = function(){
          var companyName = ApplicationStateService.getCompanyName();
          var app = ApplicationStateService.getApplicationName();
          var begin = DateModel.formatDate(DateModel.startDate);
          var end = DateModel.formatDate(DateModel.endDate);

          $q.all([
            GetMainKPIsService.getTotalKpiData(companyName, app, begin, end, "revenue"),
            GetMainKPIsService.getTotalKpiData(companyName, app, begin, end, "ltv"),
            GetMainKPIsService.getTotalKpiData(companyName, app, begin, end, "arpu"),
            GetMainKPIsService.getTotalKpiData(companyName, app, begin, end, "churn"),
            GetMainKPIsService.getTotalKpiData(companyName, app, begin, end, "avgTimeBetweenPurchases"),
            GetMainKPIsService.getTotalKpiData(companyName, app, begin, end, "payingUsers"),
            GetMainKPIsService.getTotalKpiData(companyName, app, begin, end, "avgTime1stPurchase"),
            GetMainKPIsService.getTotalKpiData(companyName, app, begin, end, "avgRevenueSession"),
            GetMainKPIsService.getTotalKpiData(companyName, app, begin, end, "purchasesPerSession")
            ]).then(function(res) {
              var extractValue = function(index, _type) {
                return (_type == 'value') ?  res[index].data.value : res[index].data.delta;
            };

                $scope.totalRevenue.updateKpiValue(extractValue(0, 'value'), extractValue(0, 'delta'))
                $scope.ltv.updateKpiValue(extractValue(1, 'value'), extractValue(1, 'delta'))
                $scope.arpu.updateKpiValue(extractValue(2, 'value'), extractValue(2, 'delta'))
                $scope.churn.updateKpiValue(extractValue(3, 'value'), extractValue(3, 'delta'))
                $scope.avgTimeBetweenPurchases.updateKpiValue(extractValue(4, 'value'), extractValue(4, 'delta'))
                $scope.payingUsers.updateKpiValue(extractValue(5, 'value'), extractValue(5, 'delta'))
                $scope.avgTimeFirstPurchase.updateKpiValue(extractValue(6, 'value'), extractValue(6, 'delta'))
                $scope.avgRevSession.updateKpiValue(extractValue(7, 'value'), extractValue(7, 'delta'))
                $scope.purchasesPerSession.updateKpiValue(extractValue(8, 'value'), extractValue(8, 'delta'))
            });
        };

        $scope.switchDetailedView = function(state) {
            $state.go(state);
            $document.scrollTop(-50, 500); //hack
        };

        var bootstrapSuccessCallback = function (data) {

            angular.extend($scope.credentials, data.data.credentials);
            ApplicationStateService.updateUserInfo(data.data.userInfo);
            ApplicationStateService.setPath("Dashboard");

            $scope.updateKPIs();
        };

        var bootstrapFailureCallback = function (errorData) {
            console.log(errorData);
        };


        $scope.credentials = {};

        BootstrapDashboardService.execute()
            .then(
                bootstrapSuccessCallback,
                bootstrapFailureCallback);

}]);
