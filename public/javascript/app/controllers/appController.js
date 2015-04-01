application.controller('AppController', [
  '$scope',
  'cookiesManagerService',
  '$http',
  '$state',
  '$rootScope',
  'LoginLogoutService',
  'ItemSearchService',
  'ApplicationStateService',
  '$stateParams',
  'UserVoiceService',
  function (
    $scope,
    cookiesManagerService,
    $http,
    $state,
    $rootScope,
    LoginLogoutService,
    ItemSearchService,
    ApplicationStateService,
    $stateParams,
    UserVoiceService
  ) {

    UserVoiceService.bootstrap();

    $scope.logout = function(){
      LoginLogoutService.logout();
    };

    $scope.$on("LOGOUT_SUCCESS", function(event, url){
      //cleanup!
      $scope.applicationName = "";
      $scope.applicationsList = [];
      $scope.userInfo = {
        name: "",
        email: ""
      };

      ApplicationStateService.cleanup();
      $state.go("login");
    });

    //app related
    $scope.applicationsList = [];
    $scope.$on("APPLICATIONS_LIST_UPDATED", function() {
      $scope.applicationsList = ApplicationStateService.applicationsList;
    });

    $scope.chooseApplication = function(app){
      console.log(app);
      oldName = ApplicationStateService.getApplicationName();
      ApplicationStateService.updateApplicationName(app.name);
      _.each(app.platforms, function(platform){
        ApplicationStateService.addPlatforms(platform);
      });

      ApplicationStateService.currentApplication = app;
      
      mixpanel.register({"application": app});

      if($state.current.name === "analytics.dashboard" && oldName !== app.name){
        $state.transitionTo($state.current, $stateParams, {
            reload: true,
            inherit: false,
            notify: true
        });
      } else
        $state.go("analytics.dashboard");
    }

    //current page related
    $scope.$on("PAGE_UPDATED", function(){
      $scope.page = ApplicationStateService.getPath();
    });

    //user related
    user = ApplicationStateService.getUserInfo();
    $scope.userInfo = (user === null)? {name : "", email : ""} : user;

    $scope.$on("USER_INFO_UPDATED", function(){
        $scope.userInfo.name = ApplicationStateService.userInfo.name;
        $scope.userInfo.email = ApplicationStateService.userInfo.email;
    });

}]);
