application.controller('SidebarController', [
  '$scope',
  '$rootScope',
  '$state',
  '$document',
  'ApplicationStateService',
  '$cookieStore',
  function(
    $scope,
    $rootScope,
    $state,
    $document,
    ApplicationStateService,
    $cookieStore
    ) {

    $scope.barState = true;
    $scope.toggleSidebar = function() {
      $scope.barState = !$scope.barState;
      $rootScope.$broadcast('sidebar');
    };
      
    $scope.selectDashboardSection = function(sectionId) {
      $scope.followLink("analytics.dashboard");
      //scroll to top
      var someElement = angular.element(document.getElementById(sectionId));
      $document.scrollToElement(someElement, 0, 500);
    };

    $scope.followLink = function(state){
      if(ApplicationStateService.getApplicationName() === ""){
        swal("No Application Selected", "Please select an application")
        $state.go("analytics.overview");
      }
      else if(state !== $state.current.name){
        $state.go(state);
        $document.scrollTop(-50, 500); //hack
      }
    };

    function SidebarOptions(name, link, css) {
      this.name = name;
      this.link = link;
      this.css = css;
    };

    $scope.dashboardOptions = [];
    $scope.dashboardOptions.push(new SidebarOptions("Revenue", "analytics.dashboard", ""));
    $scope.dashboardOptions.push(new SidebarOptions("Users", "analytics.dashboard", ""));
    $scope.dashboardOptions.push(new SidebarOptions("Sessions", "analytics.dashboard", ""));

    $scope.analyticsOptions = [];
    /** Revenue **/
    $scope.analyticsOptions.push(new SidebarOptions("Revenue", "analytics.dashboard", "childIcon fa fa-shopping-cart"));
    $scope.analyticsOptions.push(new SidebarOptions("Total Revenue", "analytics.revenue", ""));
    $scope.analyticsOptions.push(new SidebarOptions("Avg Revenue Per User", "analytics.arpu", ""));
    $scope.analyticsOptions.push(new SidebarOptions("Avg Revenue Per Session", "analytics.avgRevenueSession", ""));

    /** Users **/
    $scope.analyticsOptions.push(new SidebarOptions("Users", "analytics.dashboard", "childIcon fa fa-user"));
    $scope.analyticsOptions.push(new SidebarOptions("Life Time Value", "analytics.ltv", ""));
    $scope.analyticsOptions.push(new SidebarOptions("Paying Users", "analytics.payingUsers", ""));
    $scope.analyticsOptions.push(new SidebarOptions("Avg Purchases Per User", "analytics.avgPurchasesUser", ""));

    /** Sessions **/
    $scope.analyticsOptions.push(new SidebarOptions("Sessions", "analytics.dashboard", "childIcon fa fa-clock-o"));
    $scope.analyticsOptions.push(new SidebarOptions("Purchases per Session", "analytics.purchasesPerSession", ""));
    $scope.analyticsOptions.push(new SidebarOptions("Time 1st Purchase", "analytics.avgTime1stPurchase", ""));
    $scope.analyticsOptions.push(new SidebarOptions("Time Bet. Purchases", "analytics.avgTimebetweenPurchase", ""));
      
    $scope.onDashboardClick = function(){
      $scope.showDashboardOptions = !$scope.showDashboardOptions;
      $scope.followLink('analytics.dashboard');
    };

    $scope.onAnalyticsClick = function(){
      $scope.showAnalyticsOptions = !$scope.showAnalyticsOptions;
    };
      
    $scope.experimental = function(state){
      swal({
        title: "Are you sure?",
        text: "This feature is experimental by now",
        type: "warning",
        showCancelButton: true,
        confirmButtonColor: "#DD6B55",
        confirmButtonText: "Yes" },
        function(){
          $scope.followLink(state); //BUG: this function gets called correctly but swal call (l23) doesn't run.
        });
    }

  }]);
