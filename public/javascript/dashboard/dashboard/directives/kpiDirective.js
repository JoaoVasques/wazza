dashboard.directive('kpi', ['$state', function($state) {
    return {
      restrict: 'E',
      scope: {
        kpi: '=info',
        v: '=view',
        showPlatforms: '=details'
      },
      controller: function($scope) {
        $scope.switchDetailedView = function(state) {
          var name = state.split(".");
          mixpanel.track("Detailed View", {
            "kpi": name[1]
          });
          $state.go(state);
          $state.go(state);
        };
      },
      templateUrl: '/dashboard/kpi'
    };
}]);
