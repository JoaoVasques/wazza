dashboard.controller('DashboardController', [
    '$scope',
    "$anchorScroll",
    "$state",
    "$document",
    'ApplicationStateService',
    'GetKPIService',
    "DateModel",
    "KpiModel",
    "$q",
    "SelectedPlatformsChange",
    "DashboardCache",
    function (
        $scope,
        $anchorScroll,
        $state,
        $document,
        ApplicationStateService,
        GetKPIService,
        DateModel,
        KpiModel,
        $q,
        SelectedPlatformsChange,
        DashboardCache
        ) {

        /** Revenue KPIs **/
        $scope.totalRevenue = new KpiModel("Total Revenue", "analytics.revenue");
        $scope.arpu = new KpiModel("Avg Revenue Per User", "analytics.arpu");
        $scope.avgRevSession = new KpiModel("Avg Revenue per Session", "analytics.avgRevenueSession");

        /** User KPIs **/
        $scope.ltv = new KpiModel("Life Time Value", "analytics.ltv");
        $scope.payingUsers = new KpiModel("Paying Users", "analytics.payingUsers");
        $scope.avgPurchasesUser = new KpiModel("Avg Purchases Per User", "analytics.avgPurchasesUser");

        /** Session KPIs **/
        $scope.purchasesPerSession = new KpiModel("Purchases per Session", "analytics.purchasesPerSession");
        $scope.avgTimeFirstPurchase = new KpiModel("Avg Time 1st Purchase", "analytics.avgTime1stPurchase");
        $scope.avgTimeBetweenPurchases = new KpiModel("Avg Time Bet. Purchases", "analytics.avgTimebetweenPurchase");

        $scope.platforms = ApplicationStateService.selectedPlatforms;
        $scope.$on(SelectedPlatformsChange, function(event, args){
            $scope.platforms = ApplicationStateService.selectedPlatforms
            $scope.updateKPIs();
        });

        var getDataFromServer = function() {
          var companyName = ApplicationStateService.getCompanyName();
          var app = ApplicationStateService.getApplicationName();
          var begin = DateModel.formatDate(DateModel.startDate);
          var end = DateModel.formatDate(DateModel.endDate);

          $q.all([
            GetKPIService.getTotalKpiData(companyName, app, begin, end, "revenue", $scope.platforms),
            GetKPIService.getTotalKpiData(companyName, app, begin, end, "arpu", $scope.platforms),
            GetKPIService.getTotalKpiData(companyName, app, begin, end, "avgRevenueSession", $scope.platforms),
            GetKPIService.getTotalKpiData(companyName, app, begin, end, "ltv", $scope.platforms),
            GetKPIService.getTotalKpiData(companyName, app, begin, end, "payingUsers", $scope.platforms),
            GetKPIService.getTotalKpiData(companyName, app, begin, end, "avgPurchasesUser", $scope.platforms),
            GetKPIService.getTotalKpiData(companyName, app, begin, end, "purchasesPerSession", $scope.platforms),
            GetKPIService.getTotalKpiData(companyName, app, begin, end, "avgTimeBetweenPurchases", $scope.platforms),
              /*
            
            GetKPIService.getTotalKpiData(companyName, app, begin, end, "avgTime1stPurchase"),
            */
            ]).then(function(res) {
              $scope.totalRevenue.updateKpiValue(res[0].data);
              $scope.arpu.updateKpiValue(res[1].data);
              $scope.avgRevSession.updateKpiValue(res[2].data);
              $scope.ltv.updateKpiValue(res[3].data);
              $scope.payingUsers.updateKpiValue(res[4].data);
              $scope.avgPurchasesUser.updateKpiValue(res[5].data);
              $scope.purchasesPerSession.updateKpiValue(res[5].data);
              $scope.avgTimeBetweenPurchases.updateKpiValue(res[6].data);
              /*  $scope.avgTimeFirstPurchase.updateKpiValue(extractValue(6, 'value'), extractValue(6, 'delta'))
                */
            });
        };
        
        $scope.updateKPIs = function(){
          getDataFromServer();
        };

        $scope.switchDetailedView = function(state) {
            $state.go(state);
            $document.scrollTop(-50, 500); //hack
        };

        ApplicationStateService.setPath("Dashboard");

        $scope.updateKPIs();

}]);
