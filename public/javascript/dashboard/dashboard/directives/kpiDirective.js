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
          $state.go(state);
        };
      },
      templateUrl: '/dashboard/kpi'
    };
}]);
