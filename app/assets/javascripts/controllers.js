
angular.module('Wazza.controllers', ['ApplicationModule', 'Wazza.services', 'ItemModule', 'ngCookies', 'SecurityModule', 'DashboardModule'])

//evil, horrendous and quite broken hack. do not try this at home!
.controller('RedirectController',[
  '$scope',
  '$location',
  function (
    $scope,
    $location
    ) {
    document.getElementById("navbar").css("ng-hide", "");
    document.getElementById("sidebar").css("ng-hide", "");
    $scope.showNavBar = $scope.showSideBar = $scope.sessionOn = true;
    console.log($scope);
    $location.path("/home");
  
}])

.controller('LoginController',[
  '$scope',
  '$location',
  'submitLoginCredentialsService',
  'cookiesManagerService',
  '$rootScope',
  'redirectToDashboardService',
  'LoginLogoutService',
  function (
    $scope,
    $location,
    submitLoginCredentialsService,
    cookiesManagerService,
    $rootScope, 
    redirectToDashboardService,
    LoginLogoutService
    ) {

  $scope.canRedirectToDashboard = function(){
    redirectToDashboardService.execute().
      then(
        function(){
          LoginLogoutService.login();
          document.getElementById("page-wrapper").className = "page-wrapper";
          $location.path("/home");
        }
      );
  };

  $scope.bootstrapModule = function(){
    $scope.loginForm = {
      "email": "",
      "password": "",
      "css": "form-group"
    };
    $scope.errors = {
      "content": "",
      "show": false,
      "css": "has-error"
    };
    $scope.canRedirectToDashboard();
  };
  $scope.bootstrapModule();

  $scope.handleLoginSuccess = function(success){
    cookiesManagerService.set('PLAY2AUTH_SESS_ID', success.data.authToken);
    LoginLogoutService.login();
    $location.path(success.data.url);
  };

  $scope.handleLoginFailure = function(error){
    $scope.errors.content = error.data.errors;
    $scope.errors.show = true;
    $scope.loginForm.css = $scope.loginForm.css + " " + $scope.errors.css;
  };

  $scope.signIn = function(){
    submitLoginCredentialsService.execute($scope.loginForm).
      then(
        $scope.handleLoginSuccess,
        $scope.handleLoginFailure
      );
  };
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
  function (
    $scope,
    cookiesManagerService,
    $http,
    $location,
    $rootScope,
    LoginLogoutService,
    ItemSearchService,
    ApplicationStateService
  ) {

    $scope.bootstrapModule = function(){
      $scope.sessionOn = false;
      $scope.showNavBar = false;
      $scope.applicationName = "";
      $scope.$on("APPLICATION_NAME_UPDATED", function(){
        $scope.applicationName = ApplicationStateService.applicationName;
      });
      $scope.$on("LOGIN_SUCCESS", function(event, data){
        $scope.sessionOn = true;
        $scope.showNavBar = true;
      });

      $scope.$on("LOGOUT_SUCCESS", function(event, url){
        $scope.sessionOn = false;
        $scope.showNavBar = false;
        $location.path(url.value);
      });
    };
    $scope.bootstrapModule();

    $scope.sendItemSearchEvent = function(){
      ItemSearchService.updateSearchData($scope.itemName);
    };

    $scope.logout = function(){
      LoginLogoutService.logout();
    };
}])

.controller('SideBarController', [
  '$scope',
  '$location',
  'ApplicationStateService',
  function (
    $scope,
    $location,
    ApplicationStateService
  ) {
    $scope.showSideBar = false;
    $scope.applicationName = ""

    $scope.bootstrapModule = function(){
      $scope.$on("LOGIN_SUCCESS", function(event, data){
        $scope.showSideBar = true;
      });

      $scope.$on("LOGOUT_SUCCESS", function(event, data){
        $scope.showSideBar = false;
      });

      $scope.$on("APPLICATION_NAME_UPDATED", function(){
        $scope.applicationName = ApplicationStateService.applicationName;
      });
    };
    $scope.bootstrapModule();
}])

;
