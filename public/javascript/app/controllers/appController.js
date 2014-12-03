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
      
    //auth related
    $scope.authOK = false;

    $scope.logout = function(){
      LoginLogoutService.logout();
    };

    $scope.$on("LOGIN_SUCCESS", function(event, data){
      document.body.className = "skin-blue";
      $scope.authOK = true;
    });

    $scope.$on("LOGOUT_SUCCESS", function(event, url){
      //cleanup!
      $scope.applicationName = "";
      $scope.applicationsList = [];
      $scope.userInfo = {
        name: "",
        email: ""
      };

      ApplicationStateService.cleanup();
      $scope.authOK = false;

      document.body.className = "skin-blue";
      $state.go("webframe.login");
      //$state.go(url.value);      //TODO: fix this. url.value returns the relative url instead of the state
    });

    //app related
    $scope.applicationName = "";
    $scope.applicationsList = [];
      
    $scope.$on("APPLICATION_NAME_UPDATED", function(){
      $scope.applicationName = ApplicationStateService.getApplicationName();
    });

    $scope.$on("APPLICATIONS_LIST_UPDATED", function() {
      $scope.applicationsList = ApplicationStateService.applicationsList;
    });

    $scope.chooseApplication = function(app){
      oldName = ApplicationStateService.getApplicationName();
      ApplicationStateService.updateApplicationName(app);
      if($state.current.name === "analytics.dashboard" && oldName !== app){
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
    $scope.userInfo = ApplicationStateService.getUserInfo();

    $scope.$on("USER_INFO_UPDATED", function(){
        $scope.userInfo.name = ApplicationStateService.userInfo.name;
        $scope.userInfo.email = ApplicationStateService.userInfo.email;
    });

}]);
