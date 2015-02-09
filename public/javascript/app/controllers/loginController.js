application.controller('LoginController',[
  '$scope',
  '$state',
  'submitLoginCredentialsService',
  'cookiesManagerService',
  '$rootScope',
  'redirectToDashboardService',
  'LoginLogoutService',
  'ApplicationStateService',
  function (
    $scope,
    $state,
    submitLoginCredentialsService,
    cookiesManagerService,
    $rootScope,
    redirectToDashboardService,
    LoginLogoutService,
    ApplicationStateService
    ) {

    $scope.canRedirectToDashboard = function(){
      //TODO: I don't think this is the proper way of doing this..
      redirectToDashboardService.execute().
        then(
          function(){
            $state.go("analytics.overview");
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
      mixpanel.identify({"company": success.data.companyName});
      mixpanel.people.set({
        "$email": success.data.userId,
        "$name": success.data.userName,
        "$last_login": new Date(),
      });
      mixpanel.track("Login");
      $state.go(success.data.url);
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
