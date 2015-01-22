application.controller('NavBarController', [
  '$scope',
  'LoginLogoutService',
  'ApplicationStateService',
  '$state',
  function(
    $scope,
    LoginLogoutService,
    ApplicationStateService,
    $state
  ) {

    $scope.status = {
      isopen: false
    };
    $scope.userName = ApplicationStateService.userInfo.name;
    $scope.logoutClick = function() {
      LoginLogoutService.logout();
    };

    $scope.changeApp = function() {

    };

    $scope.newApp = function() {
      $state.go('analytics.newapp');
    };
  }
]);
