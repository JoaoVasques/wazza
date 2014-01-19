
angular.module('Wazza.controllers', ['ApplicationModule', 'ItemModule', 'Wazza.services'])

.controller('LoginController', ['$scope', 'submitLoginCredentialsService', function ($scope, submitLoginCredentialsService) {
  console.log("login controller");

  $scope.bootstrapModule = function(){
    $scope.loginForm = {
      "email": "",
      "password": ""
    };
  };

  $scope.handleLoginSuccess = function(success){
    console.log("success");
    console.log(success);
  };

  $scope.handleLoginFailure = function(error){
    console.log("error");
    console.log(error);
  };

  $scope.signIn = function(){
    console.log("sign in...");
    submitLoginCredentialsService.execute($scope.loginForm).
      then(
        function(success){
          $scope.handleLoginSuccess(success);
        },
        function(error){
          $scope.handleLoginSuccess(error);
        }
      );
  };
}])
;
