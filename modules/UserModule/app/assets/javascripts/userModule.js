'use strict';

angular.module('UserModule', ['UserModule.services', 'UserModule.directives', 'SecurityModule', 'Wazza.services'])

//TODO: refactor this -> move to user's controllers module
.controller('UserRegistrationController',
  ['$scope',
  '$location',
  'createNewUserAccountService',
  'cookiesManagerService',
  '$rootScope',
  'LoginLogoutService',
  function (
    $scope,
    $location,
    createNewUserAccountService,
    cookiesManagerService,
    $rootScope,
    LoginLogoutService
  ) {
  
  $scope.bootstrapModule = function(){
    $scope.userForm = {
      "name": "",
      "email": "",
      "password": "",
      "company": ""
    };
    $scope.passwordConfirmation = "";
    $scope.errors = {
      "content": "",
      "show": false,
      "css": "has-error"
    };
    $scope.passwordErrors = {
      "content": "Password do not match",
      "show": false,
      "css": "has-error"
    };
    $scope.passwordCss = "form-group";
    $scope.emailCss = "form-group";
  };
  $scope.bootstrapModule();

  $scope.handleUserCreationSuccess = function(success) {
    cookiesManagerService.set('PLAY2AUTH_SESS_ID', success.data.authToken);
    LoginLogoutService.login();
    $location.path(success.data.url);
  };

  $scope.handleUserCreationFailure = function(error){
    $scope.errors.content = error.data.errors;
    $scope.errors.show = true;
    $scope.emailCss = $scope.emailCss + ' ' + $scope.errors.css
  };

  $scope.checkPasswords = function() {
    if ($scope.userForm.password == $scope.passwordConfirmation) {
      $scope.passwordErrors.show = false;
      $scope.passwordCss = "form-group";
      return true;
    } else {
      $scope.passwordErrors.show = true;
      $scope.passwordCss = $scope.passwordCss + ' ' + $scope.passwordErrors.css
      return false;
    }
  };

  $scope.createUserAccount = function(){
    if($scope.checkPasswords()){
      createNewUserAccountService.execute($scope.userForm)
      .then(
        function(success){
          $scope.handleUserCreationSuccess(success)
        },
        function(errors){
          $scope.handleUserCreationFailure(errors);
        }
      );
    }
  };
}])
;
