application.controller('NavBarController', [
  '$scope',
  'LoginLogoutService',
  'ApplicationStateService',
  '$state',
  '$stateParams',
  function(
    $scope,
    LoginLogoutService,
    ApplicationStateService,
    $state,
    $stateParams
  ) {

    $scope.status = {
      isopen: false
    };
    $scope.userName = ApplicationStateService.userInfo.name;
    $scope.logoutClick = function() {
      LoginLogoutService.logout();
    };

    $scope.appName = "";
    $scope.$on("APPLICATION_NAME_UPDATED", function(){
      $scope.appName = ApplicationStateService.getApplicationName();
    });

    $scope.changeApp = function(app) {
      oldName = ApplicationStateService.getApplicationName();
      ApplicationStateService.updateApplicationName(app);
      var appInfo = _.find(ApplicationStateService.apps, function(a) {
        return app == a.name;
      });
  
      ApplicationStateService.resetPlatforms();
      _.each(appInfo.platforms, function(platform){
        ApplicationStateService.addPlatforms(platform);
      });

      if($state.current.name === "analytics.dashboard" && oldName !== app.name){
        $state.transitionTo($state.current, $stateParams, {
            reload: true,
            inherit: false,
            notify: true
        });
      } else
        $state.go("analytics.dashboard");
    };

    $scope.newApp = function() {
      $state.go('analytics.newapp');
    };
  }
]);
