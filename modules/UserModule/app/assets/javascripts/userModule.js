'use strict';

angular.module('UserModule', ['UserModule.services', 'UserModule.directives'])

//TODO: refactor this -> move to user's controllers module
.controller('UserRegistrationController',
  ['$scope', 'createNewUserAccountService',
  function ($scope, createNewUserAccountService) {
  
  $scope.bootstrapModule = function(){
    $scope.userForm = {
      "name": "",
      "email": "",
      "password": "",
      "company": ""
    };
    //TODO: needs some refactoring here..
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
    //TODO
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
