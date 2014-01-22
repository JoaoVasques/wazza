
angular.module('Wazza.controllers', ['ApplicationModule', 'Wazza.services', 'ItemModule'])

.controller('LoginController',
  ['$scope', '$location', 'submitLoginCredentialsService',
  function ($scope, $location, submitLoginCredentialsService) {
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
    console.log("success");
    console.log(success);
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
