
angular.module('Wazza.controllers', [
    'ApplicationModule',
    'Wazza.services',
    'ItemModule',
    'ngCookies',
    'SecurityModule',
    'DashboardModule',
    'ui.bootstrap'
])

.controller('LoginController',[
  '$scope',
  '$location',
  'submitLoginCredentialsService',
  'cookiesManagerService',
  '$rootScope',
  'redirectToDashboardService',
  'LoginLogoutService',
  'ApplicationStateService',
  function (
    $scope,
    $location,
    submitLoginCredentialsService,
    cookiesManagerService,
    $rootScope,
    redirectToDashboardService,
    LoginLogoutService,
    ApplicationStateService
    ) {

  $scope.canRedirectToDashboard = function(){
    redirectToDashboardService.execute().
      then(
        function(){
          LoginLogoutService.login();
          $location.path("/home");
        }
      );
  };

  $scope.handleLoginSuccess = function(success){
    cookiesManagerService.set('PLAY2AUTH_SESS_ID', success.data.authToken);
    ApplicationStateService.updateUserInfo({
        name: success.data.userName,
        email: success.data.userId
    });
    ApplicationStateService.updateCompanyName(success.data.companyName);
    LoginLogoutService.login();
    $location.path(success.data.url);
  };

  $scope.handleLoginFailure = function(error){
    $scope.errors.content = error.data.errors;
    $scope.errors.show = true;
  };

  $scope.signIn = function(){
    submitLoginCredentialsService.execute($scope.loginForm).
      then(
        $scope.handleLoginSuccess,
        $scope.handleLoginFailure
      );
  };

  $scope.loginForm = {
    "email": "",
    "password": ""
  };
  $scope.errors = {
    "content": "",
    "show": false
  };

  $scope.canRedirectToDashboard();
}])

.controller('NavBarController',[
  '$scope',
  'LoginLogoutService',
  'GetMainKPIsService',
  function (
    $scope,
    LoginLogoutService,
    GetMainKPIsService
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
    };
    $scope.toggleMin();

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

    $scope.updateMetrics = function(){
      console.log("update metrics");
    };


  $scope.initDate = $scope.today;
}])

.controller('AppController', [
  '$scope',
  'cookiesManagerService',
  '$http',
  '$location',
  '$rootScope',
  'LoginLogoutService',
  'ItemSearchService',
  'ApplicationStateService',
  'TopbarService',
  function (
    $scope,
    cookiesManagerService,
    $http,
    $location,
    $rootScope,
    LoginLogoutService,
    ItemSearchService,
    ApplicationStateService,
    TopbarService
  ) {

    //auth related
    $scope.authOK = false;

    $scope.$on("LOGIN_SUCCESS", function(event, data){
      document.body.className = "skin-blue";
      $scope.authOK = true;
    });

    $scope.$on("LOGOUT_SUCCESS", function(event, url){
      document.body.className = "skin-blue login-screen";
      $scope.authOK = false;
      $location.path(url.value);
    });

    //app related
    $scope.applicationName = "";
    $scope.applicationsList = [];

    $scope.$on("APPLICATION_NAME_UPDATED", function(){
      $scope.applicationName = ApplicationStateService.applicationName;
    });

    $scope.$on("APPLICATIONS_LIST_UPDATED", function() {
      $scope.applicationsList = ApplicationStateService.applicationsList;
    });

    //current page related
    $scope.page = "Dashboard";

    $scope.$on("PAGE_UPDATED", function(){
      $scope.page = TopbarService.getName();
    });

    //user related
    $scope.userInfo = {
        name: "",
        email: ""
    };

    $scope.$on("USER_INFO_UPDATED", function(){
        $scope.userInfo.name = ApplicationStateService.userInfo.name;
        $scope.userInfo.email = ApplicationStateService.userInfo.email;
    });

}])

;
