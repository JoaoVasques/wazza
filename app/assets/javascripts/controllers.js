
angular.module('Wazza.controllers', ['ApplicationModule', 'Wazza.services', 'ItemModule', 'ngCookies', 'SecurityModule'])

.controller('LoginController',[
  '$scope', '$location', 'submitLoginCredentialsService', 'cookiesManagerService', '$rootScope', 'redirectToDashboardService',
  function ($scope, $location, submitLoginCredentialsService, cookiesManagerService, $rootScope, redirectToDashboardService) {

  $scope.canRedirectToDashboard = function(){
    redirectToDashboardService.execute().
      then(
        function(){
          $rootScope.$broadcast("LoginSuccess", {});
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
    $rootScope.$broadcast("LoginSuccess", {});
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
        function(success){
          $scope.handleLoginSuccess(success);
        },
        function(error){
          $scope.handleLoginFailure(error);
        }
      );
  };
}])

.controller('NavBarController',[
  '$scope', 'cookiesManagerService', '$http', '$location', '$rootScope',
  function ($scope, cookiesManagerService, $http, $location, $rootScope) {

    $scope.bootstrapModule = function(){
      $scope.sessionOn = false;
      $scope.showNavBar = false;
      $scope.currentApplication = "";
      $scope.$on("UPDATED_APPLICATION_NAME", function(event, data){
        $scope.currentApplication = data.value.textContent;
      })
    };
    $scope.bootstrapModule();

    $scope.$on("LoginSuccess", function(event, data){
      $scope.sessionOn = true;
      $scope.showNavBar = true;
    });

    $scope.$on("LOGOUT_EVENT", function(event, data){
      $scope.sessionOn = false;
      $scope.showNavBar = false;
      $location.path(data.data);
    });

    $scope.handleLogoutSuccess = function(data){
      $rootScope.$broadcast('LOGOUT_EVENT', {data: data.data});
    };

    $scope.handleLogoutFailure = function(error){
      console.log("logout failure..");
      /** todo **/
    };

    $scope.logout = function(){
      $http.post("/logout")
      .then(
        function(success){
          $scope.handleLogoutSuccess(success);
        },
        function(error){
          $scope.handleLogoutFailure(error);
        }
      );
    };
}])

.controller('SideBarController', [
  '$scope',
  function ($scope) {
    $scope.showSideBar = false;

    /** Add watchers **/
    $scope.$on("LoginSuccess", function(event, data){
      $scope.showSideBar = true;
    });

    $scope.$on("LOGOUT_EVENT", function(event, data){
      $scope.showSideBar = false;
    });
}])

;
