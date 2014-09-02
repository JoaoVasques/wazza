
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
  '$state',
  'submitLoginCredentialsService',
  'cookiesManagerService',
  '$rootScope',
  'redirectToDashboardService',
  'LoginLogoutService',
  'ApplicationStateService',
  function (
    $scope,
    $state,
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
          $state.go("home.dashboard");
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
    var url = success.data.url;
    url = url.substring(1, url.length) //HACK: ditch out the / from the response
    $state.go(url);
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

}])

.controller('SidebarController', ['$scope','$rootScope', function($scope, $rootScope) {
    $scope.selectDashboardSection = function(sectionId) {
      $rootScope.$broadcast('ChangeDashboardSection', {section: sectionId});
    };
}])

.controller('AppController', [
  '$scope',
  'cookiesManagerService',
  '$http',
  '$state',
  '$rootScope',
  'LoginLogoutService',
  'ItemSearchService',
  'ApplicationStateService',
  'TopbarService',
  function (
    $scope,
    cookiesManagerService,
    $http,
    $state,
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
      $state.go(url.value);
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
