application.controller('AppController', [
  '$scope',
  'cookiesManagerService',
  '$http',
  '$state',
  '$rootScope',
  'LoginLogoutService',
  'ItemSearchService',
  'ApplicationStateService',
  'TopbarService',
  function (
    $scope,
    cookiesManagerService,
    $http,
    $state,
    $rootScope,
    LoginLogoutService,
    ItemSearchService,
    ApplicationStateService,
    TopbarService
  ) {

    //auth related
    $scope.authOK = false;

    $scope.$on("LOGIN_SUCCESS", function(event, data){
      document.body.className = "skin-blue";
      $scope.authOK = true;
    });

    $scope.$on("LOGOUT_SUCCESS", function(event, url){
      document.body.className = "skin-blue login-screen";
      $scope.authOK = false;
      $state.go("webframe.login");
      //$state.go(url.value);      //TODO: fix this. url.value returns the relative url instead of the state
    });

    //app related
    $scope.applicationName = "";
    $scope.applicationsList = [];

    $scope.$on("APPLICATION_NAME_UPDATED", function(){
      $scope.applicationName = ApplicationStateService.applicationName;
    });

    $scope.$on("APPLICATIONS_LIST_UPDATED", function() {
      $scope.applicationsList = ApplicationStateService.applicationsList;
    });

    $scope.chooseApplication = function(app){
      ApplicationStateService.updateApplicationName(app);
      $state.go("analytics.dashboard");
    }

    //current page related
    $scope.page = "Overview";
    TopbarService.setName("Overview");

    $scope.$on("PAGE_UPDATED", function(){
      $scope.page = TopbarService.getName();
    });

    //user related
    $scope.userInfo = {
        name: "",
        email: ""
    };

    $scope.$on("USER_INFO_UPDATED", function(){
        $scope.userInfo.name = ApplicationStateService.userInfo.name;
        $scope.userInfo.email = ApplicationStateService.userInfo.email;
    });

}])
