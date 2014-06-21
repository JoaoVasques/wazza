
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
          //document.getElementById("page-wrapper").className = "page-wrapper";
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
    ApplicationStateService.updateUserInfo({
        name: success.data.userName,
        email: success.data.userId
    });
    ApplicationStateService.updateCompanyName(success.data.companyName);
    LoginLogoutService.login();
    //document.getElementById("page-wrapper").className = "page-wrapper";
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

    $scope.page = "Dashboard";

    $scope.bootstrapModule = function(){
      $scope.sessionOn = false;
      $scope.showNavBar = false;
      $scope.applicationName = "";
      $scope.userInfo = {
          name: "",
          email: ""
      };
      $scope.applicationsList = [];
      $scope.$on("APPLICATION_NAME_UPDATED", function(){
        $scope.applicationName = ApplicationStateService.applicationName;
      });

      $scope.$on("APPLICATIONS_LIST_UPDATED", function() {
        $scope.applicationsList = ApplicationStateService.applicationsList;
      });
        
      $scope.$on("LOGIN_SUCCESS", function(data){
        $scope.sessionOn = true;
        $scope.showNavBar = true;
      });
        
      $scope.$on("LOGOUT_SUCCESS", function(event, url){
        //document.getElementById("page-wrapper").className = "";
        $scope.sessionOn = false;
        $scope.showNavBar = false;
        $location.path(url.value);
      });
       
      $scope.$on("USER_INFO_UPDATED", function(){
          $scope.userInfo.name = ApplicationStateService.userInfo.name;
          $scope.userInfo.email = ApplicationStateService.userInfo.email; 
      });

      $scope.$on("PAGE_UPDATED", function(){
        $scope.page = TopbarService.getName();
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
        //document.getElementById("page-wrapper").className = "";
        $scope.showSideBar = false;
      });

      $scope.$on("APPLICATION_NAME_UPDATED", function(){
        $scope.applicationName = ApplicationStateService.applicationName;
      });
    };
    $scope.bootstrapModule();
}])

;
