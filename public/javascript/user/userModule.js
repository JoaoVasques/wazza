'use strict';

angular.module('UserModule', ['UserModule.services', 'UserModule.directives', 'SecurityModule', 'Wazza.services'])

//TODO: refactor this -> move to user's controllers module
.controller('UserRegistrationController',
  ['$scope',
  '$state',
  'createNewUserAccountService',
  'cookiesManagerService',
  '$rootScope',
  'LoginLogoutService',
  'ApplicationStateService',
  function (
    $scope,
    $state,
    createNewUserAccountService,
    cookiesManagerService,
    $rootScope,
    LoginLogoutService,
    ApplicationStateService
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
      "content": "Password does not match",
      "show": false,
      "css": "has-error"
    };
    $scope.passwordCss = "form-group";
    $scope.emailCss = "form-group";
  };
  $scope.bootstrapModule();

  $scope.handleUserCreationSuccess = function(success) {
    cookiesManagerService.set('PLAY2AUTH_SESS_ID', success.data.authToken);
    mixpanel.identify({"company": $scope.userForm.company});
    mixpanel.people.set({
      "$email": $scope.userForm.email,
      "$name": $scope.userForm.name,
      "$last_login": new Date(),
    });
    mixpanel.track("New Account");
    mixpanel.track("Login");
    ApplicationStateService.updateUserInfo({
        name: $scope.userForm.name,
        email: $scope.userForm.email
      });
    ApplicationStateService.updateCompanyName($scope.userForm.company);
    $state.go(success.data.url);
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
