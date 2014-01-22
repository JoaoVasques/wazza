
angular.module('Wazza.controllers', ['ApplicationModule', 'Wazza.services', 'ItemModule', 'ngCookies'])

.controller('LoginController',
  ['$scope', '$location', '$cookieStore', '$cookies', 'submitLoginCredentialsService',
  function ($scope, $location, $cookieStore, $cookies, submitLoginCredentialsService) {
  console.log("login controller");

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
  };
  $scope.bootstrapModule();

  $scope.handleLoginSuccess = function(success){
    $cookies['PLAY2AUTH_SESS_ID'] = success.data.authToken;
    $cookieStore.put('PLAY2AUTH_SESS_ID', success.data.authToken);
    $location.path("/xpto");
  };

  $scope.handleLoginFailure = function(error){
    $scope.errors.content = error.data.errors;
    $scope.errors.show = true;
    $scope.loginForm.css = $scope.loginForm.css + " " + $scope.errors.css;
  };

  $scope.signIn = function(){
    console.log("sign in...");
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
;
