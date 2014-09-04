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
      $state.go(url.value);
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

    //current page related
    $scope.page = "Dashboard";

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