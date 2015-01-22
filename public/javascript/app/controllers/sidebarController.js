application.controller('SidebarController', [
  '$scope',
  '$rootScope',
  '$state',
  '$document',
  'ApplicationStateService',
  '$cookieStore',
  function(
    $scope,
    $rootScope,
    $state,
    $document,
    ApplicationStateService,
    $cookieStore
    ) {

    $scope.barState = true;
    $scope.toggleSidebar = function() {
      $scope.barState = !$scope.barState;
      $rootScope.$broadcast('sidebar');
    };
      
    $scope.selectDashboardSection = function(sectionId) {
      $scope.followLink("analytics.dashboard");
      //scroll to top
      var someElement = angular.element(document.getElementById(sectionId));
      $document.scrollToElement(someElement, 0, 500);
    };

    $scope.followLink = function(state){
      if(ApplicationStateService.getApplicationName() === ""){
        swal("No Application Selected", "Please select an application")
        $state.go("analytics.overview");
      }
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
        confirmButtonText: "Yes" },
        function(){
          $scope.followLink(state); //BUG: this function gets called correctly but swal call (l23) doesn't run.
        });
    }

  }]);
