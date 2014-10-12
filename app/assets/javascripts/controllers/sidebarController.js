application.controller('SidebarController', [
  '$scope',
  '$rootScope',
  '$state',
  'ApplicationStateService',
  function(
    $scope,
    $rootScope,
    $state,
    ApplicationStateService
    ) {
    
    $scope.selectDashboardSection = function(sectionId) {
      $scope.followLink("analytics.dashboard");
      $rootScope.$broadcast('ChangeDashboardSection', {section: sectionId});
    };

    $scope.followLink = function(state){
      if(ApplicationStateService.applicationName === ""){
        swal("Which Application?", "You should definitively choose one first!")
      }
      else
        $state.go(state);
    }

    $scope.experimental = function(state){
      swal({
        title: "Are you sure?",
        text: "This feature is experimental by now",
        type: "warning",
        showCancelButton: true,
        confirmButtonColor: "#DD6B55",
        confirmButtonText: "Yes, I have no fear!" },
        function(){
          $state.go(state);
        });
    }

  }]);
