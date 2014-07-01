
angular.module('Wazza.controllers', [
    'ApplicationModule',
    'Wazza.services',
    'ItemModule',
    'ngCookies',
    'SecurityModule',
    'DashboardModule'
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

    $scope.userInfo = {
        name: "",
        email: ""
    };
    $scope.applicationsList = [];

    $scope.$on("APPLICATIONS_LIST_UPDATED", function() {
      $scope.applicationsList = ApplicationStateService.applicationsList;
    });

    $scope.$on("USER_INFO_UPDATED", function(){
        $scope.userInfo.name = ApplicationStateService.userInfo.name;
        $scope.userInfo.email = ApplicationStateService.userInfo.email; 
    });

    $scope.sendItemSearchEvent = function(){
      ItemSearchService.updateSearchData($scope.itemName);
    };

    $scope.logout = function(){
      LoginLogoutService.logout();
    };
}])

.controller('AppController', [
  '$scope',
  'ApplicationStateService',
  'TopbarService',
  function (
    $scope,
    ApplicationStateService,
    TopbarService
  ) {

    $scope.applicationName = "";
    $scope.authOK = false;

    $scope.$on("LOGIN_SUCCESS", function(event, data){
      document.body.className = "skin-blue";
      $scope.authOK = true;
    });

    $scope.$on("LOGOUT_SUCCESS", function(event, data){
      document.body.className = "skin-blue login-screen";
      $scope.authOK = false;
      $location.path(url.value);
    });

    $scope.$on("APPLICATION_NAME_UPDATED", function(){
      $scope.applicationName = ApplicationStateService.applicationName;
    });

    $scope.$on("PAGE_UPDATED", function(){
      $scope.page = TopbarService.getName();
    });

}])

;
