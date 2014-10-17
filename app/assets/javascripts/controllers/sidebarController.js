application.controller('SidebarController', [
  '$scope',
  '$rootScope',
  '$state',
  '$document',
  'ApplicationStateService',
  function(
    $scope,
    $rootScope,
    $state,
    $document,
    ApplicationStateService
    ) {

    $scope.selectDashboardSection = function(sectionId) {
      $scope.followLink("analytics.dashboard");
      var someElement = angular.element(document.getElementById(sectionId));
      $document.scrollToElement(someElement, 0, 500);
    };

    $scope.followLink = function(state){
      if(ApplicationStateService.applicationName === "")
        swal("Which Application?", "You should definitively choose one first!")
      else if(state !== $state.current.name){
        $state.go(state);
        $document.scrollTop(-50, 500); //hack
      }
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
          $scope.followLink(state);
        });
    }

  }]);
